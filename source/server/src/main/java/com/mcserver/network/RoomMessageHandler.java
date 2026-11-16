package com.mcserver.network;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.mcserver.dto.response.RoomUpdateEvent;
import com.mcserver.service.room.RoomException;
import com.mcserver.service.room.RoomService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RoomMessageHandler implements TypeHandler {
    private final RoomService roomService;

    @Override
    public Set<String> supportedTypes() {
        return Set.of("LIST_ROOMS", "CREATE_ROOM", "JOIN_ROOM", "LEAVE_ROOM");
    }

    @Override
    public Message handle(String type, String requestId, JsonNode data, ClientSession session) {
        try {
            identify(data, session);
            session.setLobbySubscribed(true); // đăng ký TRƯỚC khi tạo/vào để chính mình cũng nhận sự kiện đẩy
            long userId = session.getUserId();
            String username = session.getUsername();
            RoomUpdateEvent event = switch (type) {
                case "LIST_ROOMS" -> roomService.listRooms();
                case "CREATE_ROOM" -> roomService.createRoom(userId, username);
                case "JOIN_ROOM" -> roomService.joinRoom(requiredText(data, "roomId"), userId, username);
                default -> roomService.leaveRoom(userId); // LEAVE_ROOM
            };
            return Message.ok("ROOM_UPDATE", requestId, event);
        } catch (RoomException e) {
            return Message.error("ROOM_ERROR", requestId, e.getCode(), e.getMessage());
        }
    }

    /**
     * DEV ONLY: chưa có login nên tin userId/username từ client.
     * XÓA hàm này khi Người 1 gọi session.authenticate(...) lúc đăng nhập thành công.
     */
    private void identify(JsonNode data, ClientSession s) {
        if (s.isAuthenticated()) {
            return;
        }
        JsonNode id = data.get("userId");
        JsonNode name = data.get("username");
        if (id == null || !id.isIntegralNumber() || name == null || !name.isTextual() || name.asText().isBlank()) {
            throw new RoomException("UNAUTHENTICATED", "Bạn cần đăng nhập trước");
        }
        s.authenticate(id.longValue(), name.asText().trim());
    }

    private String requiredText(JsonNode data, String field) {
        JsonNode v = data.get(field);
        if (v == null || !v.isTextual() || v.asText().isBlank()) {
            throw new IllegalArgumentException("Field '" + field + "' must be a non-empty string");
        }
        return v.asText();
    }
}