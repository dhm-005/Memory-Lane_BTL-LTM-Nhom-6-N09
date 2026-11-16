package com.mcserver.network;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class SessionRegistry {
    private final Set<ClientSession> sessions = ConcurrentHashMap.newKeySet();

    public void add(ClientSession s) { sessions.add(s); }

    public void remove(ClientSession s) { sessions.remove(s); }

    public void broadcastToLobby(Object message) {
        for (ClientSession s : sessions) {
            if (s.isLobbySubscribed()) {
                s.send(message);
            }
        }
    }
}