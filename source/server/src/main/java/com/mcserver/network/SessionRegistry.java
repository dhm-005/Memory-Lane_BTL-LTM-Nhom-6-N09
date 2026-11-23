package com.mcserver.network;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class SessionRegistry {
    private final Set<ClientSession> sessions = ConcurrentHashMap.newKeySet();
    private final Map<Long, ClientSession> authenticatedSessions = new ConcurrentHashMap<>();

    public void add(ClientSession s) {
        sessions.add(s);
    }

    public void registerUser(long userId, ClientSession s) {
        authenticatedSessions.put(userId, s);
    }

    public void remove(ClientSession s) {
        sessions.remove(s);
        if (s.getUserId() != null) {
            authenticatedSessions.remove(s.getUserId(), s);
        }
    }

    public boolean sendToUser(long userId, ObjectNode message) {
        ClientSession s = authenticatedSessions.get(userId);
        return s != null && s.send(message);
    }

    public void broadcastToLobby(ObjectNode message) {
        for (ClientSession s : sessions) {
            if (s.isLobbySubscribed()) {
                s.send(message);
            }
        }
    }
}