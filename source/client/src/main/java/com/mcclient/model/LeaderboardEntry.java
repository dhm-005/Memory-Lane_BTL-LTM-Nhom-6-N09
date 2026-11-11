package com.mcclient.model;

public class LeaderboardEntry {

    private int rank;
    private long playerId;
    private String username;
    private int totalMatches;
    private int wins;
    private int totalScore;
    private double winRate;

    public LeaderboardEntry() {
    }

    public LeaderboardEntry(int rank, long playerId, String username, int totalMatches,
            int wins, int totalScore, double winRate) {
        this.rank = rank;
        this.playerId = playerId;
        this.username = username;
        this.totalMatches = totalMatches;
        this.wins = wins;
        this.totalScore = totalScore;
        this.winRate = winRate;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getTotalMatches() {
        return totalMatches;
    }

    public void setTotalMatches(int totalMatches) {
        this.totalMatches = totalMatches;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public double getWinRate() {
        return winRate;
    }

    public void setWinRate(double winRate) {
        this.winRate = winRate;
    }
}
