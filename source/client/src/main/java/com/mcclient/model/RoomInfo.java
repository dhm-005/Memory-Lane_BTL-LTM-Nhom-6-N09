package com.mcclient.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RoomInfo {

    private String id;
    private long hostId;
    private String status;
    private LocalDateTime createdAt;
    private int playerCount;
    private String hostName;
    private int maxPlayers = 2;
    private List<RoomPlayer> players = new ArrayList<>();

    public RoomInfo() {
    }

    public RoomInfo(String id, long hostId, String status, LocalDateTime createdAt, int playerCount) {
        this.id = id;
        this.hostId = hostId;
        this.status = status;
        this.createdAt = createdAt;
        this.playerCount = playerCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getHostId() {
        return hostId;
    }

    public void setHostId(long hostId) {
        this.hostId = hostId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public List<RoomPlayer> getPlayers() {
        return players;
    }

    public void setPlayers(List<RoomPlayer> players) {
        this.players = players;
    }

    public boolean hasPlayer(long userId) {
        return players.stream().anyMatch(p -> p.getUserId() == userId);
    }
}