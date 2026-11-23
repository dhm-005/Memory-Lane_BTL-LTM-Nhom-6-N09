package com.mcserver.network;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mcserver.util.JsonUtils;

public class ClientSession {
    private final BufferedWriter out;
    private final ReentrantLock writeLock = new ReentrantLock();
    private volatile Long userId;
    private volatile String username;
    private volatile boolean lobbySubscribed;
    private volatile boolean open = true;

    public ClientSession(BufferedWriter out) {
        this.out = out;
    }

    public boolean send(ObjectNode message) {
        if (!open || message == null) {
            return false;
        }
        writeLock.lock();
        try {
            out.write(JsonUtils.toJson(message));
            out.newLine();
            out.flush();
            return true;
        } catch (IOException e) {
            open = false;
            return false;
        } finally {
            writeLock.unlock();
        }
    }

    public void authenticate(long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public boolean isAuthenticated() { return userId != null; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public boolean isLobbySubscribed() { return lobbySubscribed; }
    public void setLobbySubscribed(boolean v) { this.lobbySubscribed = v; }
    
    private volatile int soloProgressLevel = 0;

    public int getSoloProgressLevel() { return soloProgressLevel; }
    public void setSoloProgressLevel(int soloProgressLevel) { this.soloProgressLevel = soloProgressLevel; }
}