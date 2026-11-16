package com.mcserver.dto.response;

import java.util.List;

/** event: SNAPSHOT | ROOM_CREATED | PLAYER_JOINED | PLAYER_LEFT | HOST_CHANGED | ROOM_CLOSED | ROOM_STARTED */
public record RoomUpdateEvent(String event, String roomId, long version, List<RoomDTO> rooms) {}
