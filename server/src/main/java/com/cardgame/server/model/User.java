package com.cardgame.server.model;

import java.time.LocalDateTime;

/**
 * Represents a registered player account.
 *
 * The password hash is used only by the server and must never be sent to the client.
 */
public class User {
    private int id;
    private String username;
    private String passwordHash;
    private int coins;
    private int rankingPoints;
    private LocalDateTime createdAt;

    public User(int id, String username, String passwordHash,
                int coins, int rankingPoints, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.coins = coins;
        this.rankingPoints = rankingPoints;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public int getRankingPoints() {
        return rankingPoints;
    }

    public void setRankingPoints(int rankingPoints) {
        this.rankingPoints = rankingPoints;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
