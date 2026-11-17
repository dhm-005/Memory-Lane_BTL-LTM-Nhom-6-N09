package com.mcclient.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.mcclient.model.RoomInfo;
import com.mcclient.model.User;
import com.mcclient.network.NetworkClient;
import com.mcclient.network.ServerException;
import com.mcclient.util.Json;
import com.mcclient.view.LobbyFrame;

public class LobbyController {
    private final NetworkClient client = NetworkClient.getInstance();
    private final User me;
    private final LobbyFrame view;
    private long lastVersion = -1;
    private List<RoomInfo> lastRooms = List.of();
    private String currentRoomId;

    public LobbyController(User me, LobbyFrame view) {
        this.me = me;
        this.view = view;
        client.addPushListener(this::onPush);
        view.onCreate(this::createRoom);
        view.onJoin(this::joinRoom);
        view.onLeave(this::leaveRoom);
        view.onRefresh(this::loadRooms);
    }

    public void start() {
        view.setVisible(true);
        loadRooms();
    }

    // ---- hành động người dùng ----
    private void loadRooms() {
        call("LIST_ROOMS", identity(), d -> {
            applySnapshot(d);
            syncRoomView();
        });
    }

    private void createRoom() {
        call("CREATE_ROOM", identity(), this::enteredRoom);
    }

    private void joinRoom(String roomId) {
        Map<String, Object> p = identity();
        p.put("roomId", roomId);
        call("JOIN_ROOM", p, this::enteredRoom);
    }

    private void leaveRoom() {
        view.setBusy(true);
        client.request("LEAVE_ROOM", identity()).whenComplete((d, ex) -> SwingUtilities.invokeLater(() -> {
            view.setBusy(false);
            currentRoomId = null; // lỗi NOT_IN_ROOM cũng đưa về sảnh để không kẹt giao diện
            view.showLobby();
            loadRooms();
        }));
    }

    // ---- helper ----
    /** Sau khi có login thật, server bỏ qua 2 field này; để lại cũng không hại. */
    private Map<String, Object> identity() {
        Map<String, Object> m = new HashMap<>();
        m.put("userId", me.getId());
        m.put("username", me.getUsername());
        return m;
    }

    private void call(String type, Map<String, Object> data, Consumer<JsonNode> onOk) {
        view.setBusy(true);
        client.request(type, data).whenComplete((resp, ex) -> SwingUtilities.invokeLater(() -> {
            view.setBusy(false);
            if (ex != null) {
                view.showError(ServerException.unwrap(ex).getMessage());
                return;
            }
            onOk.accept(resp);
        }));
    }

    private void enteredRoom(JsonNode data) {
        currentRoomId = data.path("roomId").asText();
        applySnapshot(data); // có thể bị bỏ qua vì sự kiện đẩy cùng version đã tới trước
        syncRoomView();
    }

    // ---- sự kiện đẩy ----
    private void onPush(String type, JsonNode data) { // chạy trên luồng đọc, KHÔNG phải EDT
        if ("ROOM_UPDATE".equals(type)) {
            SwingUtilities.invokeLater(() -> {
                applySnapshot(data);
                syncRoomView();
            });
        } else if ("DISCONNECTED".equals(type)) {
            SwingUtilities.invokeLater(() -> {
                lastVersion = -1; // server restart sẽ đếm version lại từ đầu
                view.showError("Mất kết nối tới server");
            });
        }
    }

    /** Cập nhật danh sách phòng nếu version mới hơn. */
    private void applySnapshot(JsonNode data) {
        long version = data.path("version").asLong();
        if (version <= lastVersion) {
            return;
        }
        lastVersion = version;
        lastRooms = Json.MAPPER.convertValue(data.path("rooms"), new TypeReference<List<RoomInfo>>() { });
        view.showRooms(lastRooms);
    }

    /** Đang ở phòng thì hiển thị phòng; nếu danh sách mới không còn mình trong phòng đó thì về sảnh. */
    private void syncRoomView() {
        if (currentRoomId == null) {
            return;
        }
        RoomInfo mine = lastRooms.stream()
                .filter(r -> r.getId().equals(currentRoomId) && r.hasPlayer(me.getId()))
                .findFirst().orElse(null);
        if (mine == null) {
            currentRoomId = null;
            view.showLobby();
        } else {
            view.showRoom(mine, me.getId());
        }
    }
}