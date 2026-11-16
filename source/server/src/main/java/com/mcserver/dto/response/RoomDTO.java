package com.mcserver.dto.response;

import java.util.List;

public record RoomDTO(
        String id,
        long hostId,
        String hostName,
        String status,
        String createdAt,
        int playerCount,
        int maxPlayers,
        List<RoomPlayerDTO> players) {
}
