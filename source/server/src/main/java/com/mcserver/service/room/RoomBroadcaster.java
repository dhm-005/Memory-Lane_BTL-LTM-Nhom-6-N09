package com.mcserver.service.room;

import com.mcserver.dto.response.RoomUpdateEvent;

/** Tách khỏi socket để RoomService test được mà không cần mạng. */
public interface RoomBroadcaster {
    void broadcast(RoomUpdateEvent event);
}