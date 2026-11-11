package com.mcclient.model;

import java.time.LocalDateTime;

public class RoomInfo {

    private long id;
    private long hostId;
    private String status;
    private LocalDateTime createdAt;
    private int playerCount;

    public RoomInfo() {
    }

    public RoomInfo(long id, long hostId, String status, LocalDateTime createdAt, int playerCount) {
        this.id = id;
        this.hostId = hostId;
        this.status = status;
        this.createdAt = createdAt;
        this.playerCount = playerCount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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
}
