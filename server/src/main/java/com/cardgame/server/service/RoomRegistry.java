package com.cardgame.server.service;

import com.cardgame.server.model.Room;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/** In-memory registry for active and waiting rooms. */
public final class RoomRegistry {
    private static final Map<String, Room> ROOMS = new ConcurrentHashMap<>();
    private RoomRegistry() {
    }
    public static Map<String, Room> all() {
        return ROOMS;
    }
}
