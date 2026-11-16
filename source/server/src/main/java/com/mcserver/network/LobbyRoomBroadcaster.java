package com.mcserver.network;

import org.springframework.stereotype.Component;

import com.mcserver.dto.response.RoomUpdateEvent;
import com.mcserver.service.room.RoomBroadcaster;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LobbyRoomBroadcaster implements RoomBroadcaster {
    private final SessionRegistry sessionRegistry;

    @Override
    public void broadcast(RoomUpdateEvent event) {
        sessionRegistry.broadcastToLobby(Message.push("ROOM_UPDATE", event));
    }
}