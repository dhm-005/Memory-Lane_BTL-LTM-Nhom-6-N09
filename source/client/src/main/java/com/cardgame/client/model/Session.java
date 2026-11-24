package com.cardgame.client.model;

/** Client-side snapshot of the authenticated account and current room. */
public final class Session {
    public String username = "";
    public String roomId = "";
    public boolean loggedIn;
    public int coins = 50;
    public int rankingPoints;
    public void clear() {
        loggedIn = false;
        username = "";
        roomId = "";
        coins = 50;
        rankingPoints = 0;
    }
}
