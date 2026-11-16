package com.mcserver.service.room;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.mcserver.dto.response.RoomDTO;
import com.mcserver.dto.response.RoomPlayerDTO;
import com.mcserver.dto.response.RoomUpdateEvent;
import com.mcserver.model.room.Room;
import com.mcserver.model.room.RoomPlayer;
import com.mcserver.model.room.RoomStatus;

/** Quản lý phòng trong RAM. Mọi thay đổi nằm trong synchronized; broadcast (I/O) làm SAU khi nhả khoá. */
@Service
public class RoomService {
    private final Map<String, Room> rooms = new LinkedHashMap<>(); // guarded by this
    private final Map<Long, String> userRoom = new HashMap<>();    // userId -> roomId, guarded by this
    private final AtomicLong roomSeq = new AtomicLong();
    private long version; // guarded by this
    private final RoomBroadcaster broadcaster;

    public RoomService(RoomBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    public RoomUpdateEvent createRoom(long userId, String username) {
        RoomUpdateEvent event;
        synchronized (this) {
            requireNotInRoom(userId);
            Room room = new Room(String.format("R%03d", roomSeq.incrementAndGet()), new RoomPlayer(userId, username));
            rooms.put(room.getId(), room);
            userRoom.put(userId, room.getId());
            event = snapshot("ROOM_CREATED", room.getId());
        }
        broadcaster.broadcast(event);
        return event;
    }

    public RoomUpdateEvent joinRoom(String roomId, long userId, String username) {
        RoomUpdateEvent event;
        synchronized (this) {
            requireNotInRoom(userId);
            Room room = rooms.get(roomId);
            if (room == null) throw new RoomException("ROOM_NOT_FOUND", "Phòng không tồn tại");
            if (room.getStatus() == RoomStatus.PLAYING) throw new RoomException("ROOM_IN_PROGRESS", "Phòng đang chơi");
            if (room.isFull()) throw new RoomException("ROOM_FULL", "Phòng đã đủ 2 người");
            room.addPlayer(new RoomPlayer(userId, username));
            userRoom.put(userId, roomId);
            event = snapshot("PLAYER_JOINED", roomId);
        }
        broadcaster.broadcast(event);
        return event;
    }

    public RoomUpdateEvent leaveRoom(long userId) {
        RoomUpdateEvent event;
        synchronized (this) {
            String roomId = userRoom.remove(userId);
            if (roomId == null) throw new RoomException("NOT_IN_ROOM", "Bạn không ở trong phòng nào");
            Room room = rooms.get(roomId);
            boolean wasHost = room.getHostId() == userId;
            room.removePlayer(userId);
            String name;
            if (room.isEmpty()) { rooms.remove(roomId); name = "ROOM_CLOSED"; }
            else name = wasHost ? "HOST_CHANGED" : "PLAYER_LEFT";
            event = snapshot(name, roomId);
        }
        broadcaster.broadcast(event);
        return event;
    }

    /** Socket đóng đột ngột -> tự rời phòng, không ném lỗi. */
    public void handleDisconnect(long userId) {
        try { leaveRoom(userId); } catch (RoomException ignored) { }
    }

    public synchronized RoomUpdateEvent listRooms() { return snapshot("SNAPSHOT", ""); }

    /** Cho Người 3 (game): bắt đầu ván -> khoá phòng. */
    public RoomUpdateEvent markPlaying(String roomId) {
        RoomUpdateEvent event;
        synchronized (this) {
            Room room = rooms.get(roomId);
            if (room == null) throw new RoomException("ROOM_NOT_FOUND", "Phòng không tồn tại");
            if (!room.isFull()) throw new RoomException("ROOM_NOT_READY", "Cần đủ 2 người");
            room.markPlaying();
            event = snapshot("ROOM_STARTED", roomId);
        }
        broadcaster.broadcast(event);
        return event;
    }

    // ---- chỉ gọi khi đang giữ khoá ----
    private void requireNotInRoom(long userId) {
        if (userRoom.containsKey(userId)) throw new RoomException("ALREADY_IN_ROOM", "Bạn đang ở trong một phòng khác");
    }

    private RoomUpdateEvent snapshot(String event, String roomId) {
        List<RoomDTO> list = rooms.values().stream().map(this::toDto).toList();
        return new RoomUpdateEvent(event, roomId, ++version, list);
    }

    private RoomDTO toDto(Room r) {
        return new RoomDTO(r.getId(), r.getHostId(), r.getHostName(), r.getStatus().name(),
                r.getCreatedAt().toString(), r.getPlayers().size(), Room.MAX_PLAYERS,
                r.getPlayers().stream().map(p -> new RoomPlayerDTO(p.userId(), p.username())).toList());
    }
}