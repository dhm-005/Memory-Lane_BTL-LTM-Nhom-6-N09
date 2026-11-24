package com.cardgame.client.model;

public record RoomInfo(String roomId, String host, int playerCount) {
    public String displayText() {
        return "Phòng " + roomId + "     ·     " + host + "     ·     " + playerCount + "/2 người";
    }
}
