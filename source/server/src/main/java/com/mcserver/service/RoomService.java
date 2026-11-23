package com.mcserver.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import com.mcserver.model.ramOnly.Room;
import com.mcserver.model.ramOnly.Room.RoomPlayer;
import com.mcserver.model.ramOnly.Room.RoomStatus;

public class RoomService {
    private static final Map<String, Room> ROOMS = new ConcurrentHashMap<>();

    public static Room getRoomById(String roomId) {
        return roomId != null ? ROOMS.get(roomId) : null;
    }

    public static Room getRoomByUserId(Long userId) {
        if (userId == null) return null;
        for (Room room : ROOMS.values()) {
            synchronized (room) {
                if (room.getPlayers().stream().anyMatch(p -> p.userId() == userId)) {
                    return room;
                }
            }
        }
        return null;
    }

    public List<Room> listRooms() {
        List<Room> available = new ArrayList<>();
        for (Room r : ROOMS.values()) {
            synchronized (r) {
                if (r.getStatus() == RoomStatus.WAITING && !r.getPlayers().isEmpty()) {
                    available.add(r);
                }
            }
        }
        return available;
    }

    public Room createRoom(long userId, String username) {
        // Nếu đang ở phòng khác thì rời phòng cũ trước
        leaveRoom(userId);

        String id;
        do {
            id = String.format("%04d", ThreadLocalRandom.current().nextInt(1000, 10000));
        } while (ROOMS.containsKey(id));

        Room room = new Room(id, new RoomPlayer(userId, username));
        ROOMS.put(id, room);
        return room;
    }

    public Room joinRoom(String roomId, long userId, String username) {
        Room room = ROOMS.get(roomId);
        if (room == null) {
            throw new IllegalArgumentException("Không tìm thấy phòng " + roomId);
        }

        synchronized (room) {
            if (hasPlayer(room, userId)) {
                return room;
            }
            if (room.getStatus() != RoomStatus.WAITING || isFull(room)) {
                throw new IllegalArgumentException("Phòng đã đủ người hoặc đang trong trận.");
            }

            leaveRoom(userId);
            addPlayer(room, new RoomPlayer(userId, username));
            return room;
        }
    }

    public void leaveRoom(long userId) {
        Room room = getRoomByUserId(userId);
        if (room == null) return;

        synchronized (room) {
            removePlayer(room, userId);
            if (isEmpty(room)) {
                ROOMS.remove(room.getId());
            }
        }
    }

    public void handleDisconnect(long userId) {
        leaveRoom(userId);
    }

    // ==================== HELPER TRẠNG THÁI PHÒNG ====================

    public boolean isFull(Room room) {
        return room.getPlayers().size() >= Room.MAX_PLAYERS;
    }

    public boolean isEmpty(Room room) {
        return room.getPlayers().isEmpty();
    }

    public boolean hasPlayer(Room room, long userId) {
        return room.getPlayers().stream().anyMatch(p -> p.userId() == userId);
    }

    public void addPlayer(Room room, RoomPlayer player) {
        room.getPlayers().add(player);
        if (room.getStatus() != RoomStatus.PLAYING) {
            room.setStatus(isFull(room) ? RoomStatus.FULL : RoomStatus.WAITING);
        }
    }

    public void removePlayer(Room room, long userId) {
        room.getPlayers().removeIf(p -> p.userId() == userId);
        if (!room.getPlayers().isEmpty() && room.getHostId() == userId) {
            room.setHostId(room.getPlayers().get(0).userId());
        }
        if (room.getStatus() != RoomStatus.PLAYING) {
            room.setStatus(RoomStatus.WAITING);
        }
    }

    public void markPlaying(Room room) {
        room.setStatus(RoomStatus.PLAYING);
    }

    public String getHostName(Room room) {
        return room.getPlayers().stream()
                .filter(p -> p.userId() == room.getHostId())
                .map(RoomPlayer::username)
                .findFirst()
                .orElse("?");
    }
}