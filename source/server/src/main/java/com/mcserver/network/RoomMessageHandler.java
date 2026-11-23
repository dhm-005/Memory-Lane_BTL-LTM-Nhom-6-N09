package com.mcserver.network;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mcserver.model.ramOnly.Room;
import com.mcserver.service.RoomService;
import com.mcserver.util.JsonTemplates;
import com.mcserver.util.JsonUtils;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class RoomMessageHandler implements TypeHandler {
    private final RoomService roomService;
    private final SessionRegistry sessionRegistry;

    
    @Override
    public Set<String> supportedTypes() {
        return Set.of("LIST_ROOMS", "CREATE_ROOM", "JOIN_ROOM", "LEAVE_ROOM");
    }

    @Override
    public ObjectNode handle(String type, String requestId, JsonNode data, ClientSession session) {
        try {
            if (!session.isAuthenticated()) {
                return JsonTemplates.error("ROOM_ERROR", requestId, "UNAUTHENTICATED", "Bạn cần đăng nhập trước");
            }
            session.setLobbySubscribed(true);
            long userId = session.getUserId();
            String username = session.getUsername();

            return switch (type) {
                case "LIST_ROOMS" -> JsonTemplates.roomListResponse(requestId, roomService.listRooms());
                case "CREATE_ROOM" -> {
                    Room created = roomService.createRoom(userId, username);
                    sessionRegistry.broadcastToLobby(JsonTemplates.roomsChangedPush(roomService.listRooms()));
                    yield JsonTemplates.roomResponse(type, requestId, created);
                }
                case "JOIN_ROOM" -> {
                    String roomId = JsonUtils.getString(data, "roomId");
                    if (roomId.isBlank()) {
                        throw new IllegalArgumentException("Field 'roomId' must be a non-empty string");
                    }
                    Room joined = roomService.joinRoom(roomId, userId, username);
                    sessionRegistry.broadcastToLobby(JsonTemplates.roomsChangedPush(roomService.listRooms()));
                    yield JsonTemplates.roomResponse(type, requestId, joined);
                }
                default -> { // LEAVE_ROOM
                    roomService.leaveRoom(userId);
                    List<Room> rooms = roomService.listRooms();
                    sessionRegistry.broadcastToLobby(JsonTemplates.roomsChangedPush(rooms));
                    yield JsonTemplates.ok(type, requestId, rooms);
                }
            };
        } catch (IllegalArgumentException e) {
            return JsonTemplates.error("ROOM_ERROR", requestId, "INVALID_ROOM_ACTION", e.getMessage());
        }
    }
}