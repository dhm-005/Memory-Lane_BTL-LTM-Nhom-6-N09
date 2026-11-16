package com.mcserver.network;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import com.fasterxml.jackson.databind.ObjectMapper;

/** Một kết nối client. Dùng ReentrantLock (không dùng synchronized) vì server chạy virtual thread. */
public class ClientSession {
    private final BufferedWriter out;
    private final ObjectMapper mapper;
    private final ReentrantLock writeLock = new ReentrantLock();
    private volatile Long userId;
    private volatile String username;
    private volatile boolean lobbySubscribed;
    private volatile boolean open = true;

    public ClientSession(BufferedWriter out, ObjectMapper mapper) {
        this.out = out;
        this.mapper = mapper;
    }

    /** Thread-safe: phản hồi (luồng của chính client) và sự kiện đẩy (luồng khác) cùng gọi được. */
    public boolean send(Object message) {
        if (!open || message == null) {
            return false;
        }
        writeLock.lock();
        try {
            out.write(mapper.writeValueAsString(message));
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
        this.username = username;
        this.userId = userId;
    }

    public boolean isAuthenticated() { return userId != null; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public boolean isLobbySubscribed() { return lobbySubscribed; }
    public void setLobbySubscribed(boolean v) { this.lobbySubscribed = v; }
}