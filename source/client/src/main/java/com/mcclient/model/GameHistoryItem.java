package com.mcclient.model;

import java.time.LocalDateTime;

public class GameHistoryItem {

    private long matchId;
    private String gameMode;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Long winnerId;
    private int playerScore;

    public GameHistoryItem() {
    }

    public GameHistoryItem(long matchId, String gameMode, LocalDateTime startedAt,
            LocalDateTime endedAt, Long winnerId, int playerScore) {
        this.matchId = matchId;
        this.gameMode = gameMode;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.winnerId = winnerId;
        this.playerScore = playerScore;
    }

    public long getMatchId() {
        return matchId;
    }

    public void setMatchId(long matchId) {
        this.matchId = matchId;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public Long getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(Long winnerId) {
        this.winnerId = winnerId;
    }

    public int getPlayerScore() {
        return playerScore;
    }

    public void setPlayerScore(int playerScore) {
        this.playerScore = playerScore;
    }
}
