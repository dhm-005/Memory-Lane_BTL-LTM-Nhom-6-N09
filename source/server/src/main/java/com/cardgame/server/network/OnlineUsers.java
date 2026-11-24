package com.cardgame.server.network;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/** Registry of authenticated TCP sessions, indexed by account ID. */
public final class OnlineUsers {
    private static final Map<Integer, ClientHandler> USERS = new ConcurrentHashMap<>();
    private OnlineUsers() {
    }
    public static Map<Integer, ClientHandler> all() {
        return USERS;
    }
}
