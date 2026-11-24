package com.cardgame.server.model;

import java.time.LocalDateTime;

/** Immutable data required to settle and persist one PvP match. */
public record GameResult(
        String roomId,
        int player1Id,
        int player2Id,
        int player1Score,
        int player2Score,
        String status,
        String cancelReason,
        int player1CoinDelta,
        int player2CoinDelta,
        int player1CoinChangeBeforeSettlement,
        int player2CoinChangeBeforeSettlement,
        int player1RankChange,
        int player2RankChange,
        LocalDateTime startedAt) {
}
