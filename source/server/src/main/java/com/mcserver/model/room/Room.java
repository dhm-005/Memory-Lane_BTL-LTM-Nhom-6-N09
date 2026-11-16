package com.mcserver.model.room;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Phòng chơi trong RAM. KHÔNG thread-safe: chỉ được truy cập trong khối synchronized của RoomService. */
public class Room {
    public static final int MAX_PLAYERS = 2;

    private final String id;
    private final LocalDateTime createdAt = LocalDateTime.now();
    private final List<RoomPlayer> players = new ArrayList<>();
    private long hostId;
    private RoomStatus status = RoomStatus.WAITING;

    public Room(String id, RoomPlayer host) {
        this.id = id;
        this.hostId = host.userId();
        this.players.add(host);
    }

    public boolean isFull() { return players.size() >= MAX_PLAYERS; }
    public boolean isEmpty() { return players.isEmpty(); }
    public boolean hasPlayer(long userId) { return players.stream().anyMatch(p -> p.userId() == userId); }

    public void addPlayer(RoomPlayer p) {
        players.add(p);
        if (status != RoomStatus.PLAYING) status = isFull() ? RoomStatus.FULL : RoomStatus.WAITING;
    }

    /** Xoá người chơi; nếu là host và phòng còn người thì chuyển host cho người còn lại. */
    public void removePlayer(long userId) {
        players.removeIf(p -> p.userId() == userId);
        if (!players.isEmpty() && hostId == userId) hostId = players.get(0).userId();
        status = RoomStatus.WAITING; // ván đang chơi mà có người thoát -> module game tự xử lý xử thua
    }

    public void markPlaying() { status = RoomStatus.PLAYING; }

    public String getId() { return id; }
    public long getHostId() { return hostId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public RoomStatus getStatus() { return status; }
    public List<RoomPlayer> getPlayers() { return List.copyOf(players); }
    public String getHostName() {
        return players.stream().filter(p -> p.userId() == hostId).map(RoomPlayer::username).findFirst().orElse("?");
    }
}