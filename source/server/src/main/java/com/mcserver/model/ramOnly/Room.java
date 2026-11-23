package com.mcserver.model.ramOnly;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    public enum RoomStatus { WAITING, FULL, PLAYING }
    public record RoomPlayer(long userId, String username) {}

    public static final int MAX_PLAYERS = 2;

    private String id;
    private LocalDateTime createdAt = LocalDateTime.now();
    private List<RoomPlayer> players = new ArrayList<>();
    private long hostId;
    private RoomStatus status = RoomStatus.WAITING;

    public Room(String id, RoomPlayer host) {
        this.id = id;
        this.hostId = host.userId();
        this.players.add(host);
    }
}