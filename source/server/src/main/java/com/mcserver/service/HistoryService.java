package com.mcserver.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mcserver.model.ActionCard;
import com.mcserver.model.GameMatch;
import com.mcserver.network.ClientSession;
import com.mcserver.repository.CardRepository;
import com.mcserver.repository.GameMatchRepository;
import com.mcserver.repository.SoloRecordRepository;
import com.mcserver.repository.UserRepository;
import com.mcserver.util.JsonTemplates;

public final class HistoryService {
    private HistoryService() {}

    public static ObjectNode getSoloBest(String requestId, ClientSession session) {
        if (!session.isAuthenticated()) {
            return JsonTemplates.error("SOLO_BEST", requestId, "UNAUTHENTICATED", "Bạn cần đăng nhập trước");
        }
        try {
            GameMatch best = SoloRecordRepository.findBestMatch(session.getUserId());
            if (best != null && best.getGameType() != null) {
                session.setSoloProgressLevel(best.getGameType().getLevel());
            }
            return JsonTemplates.ok("SOLO_BEST", requestId, best);
        } catch (SQLException e) {
            return JsonTemplates.error("SOLO_BEST", requestId, "DB_ERROR", "Không tải được thành tích chơi đơn: " + e.getMessage());
        }
    }

    public static ObjectNode completeSoloLevel(String requestId, ClientSession session, int level, long timeMs, int score) {
        if (!session.isAuthenticated()) {
            return JsonTemplates.error("SOLO_LEVEL_COMPLETE", requestId, "UNAUTHENTICATED", "Bạn cần đăng nhập trước");
        }
        long userId = session.getUserId();
        if (level != session.getSoloProgressLevel() + 1 || level < 1 || level > 5) {
            return JsonTemplates.error("SOLO_LEVEL_COMPLETE", requestId, "INVALID_LEVEL", "Thứ tự màn chơi không hợp lệ");
        }

        try {
            session.setSoloProgressLevel(level);
            int coinsEarned = (level == 4 || level == 5) ? 20 : 0;

            LocalDateTime endedAt = LocalDateTime.now();
            LocalDateTime startedAt = (level == 5 && timeMs >= 0) ? endedAt.minusNanos(timeMs * 1_000_000L) : endedAt;

            SoloRecordRepository.save(userId, level, startedAt, level == 5 ? endedAt : null, score, coinsEarned);

            if (coinsEarned > 0) {
                UserRepository.addCoins(userId, coinsEarned);
                int currentCoins = UserRepository.getCoins(userId);
                ObjectNode payload = JsonTemplates.ok("SOLO_REWARD_OK", requestId, null);
                ObjectNode data = payload.putObject("data");
                data.put("coins", currentCoins);
                data.put("amount", coinsEarned);
                data.put("level", level);
                return payload;
            }
            ObjectNode ok = JsonTemplates.ok("SOLO_LEVEL_COMPLETE", requestId, null);
            ok.putObject("data").put("level", level);
            return ok;
        } catch (SQLException e) {
            return JsonTemplates.error("SOLO_LEVEL_COMPLETE", requestId, "DB_ERROR", "Không cập nhật được tiến độ chơi đơn: " + e.getMessage());
        }
    }

    public static ObjectNode getMatchHistory(String requestId, ClientSession session) {
        if (!session.isAuthenticated()) {
            return JsonTemplates.error("MATCH_HISTORY", requestId, "UNAUTHENTICATED", "Bạn cần đăng nhập trước");
        }
        try {
            var history = GameMatchRepository.findHistory(session.getUserId());
            return JsonTemplates.matchHistoryResponse(requestId, history);
        } catch (SQLException e) {
            return JsonTemplates.error("MATCH_HISTORY", requestId, "DB_ERROR", "Không tải được lịch sử: " + e.getMessage());
        }
    }

    public static ObjectNode getLeaderboard(String requestId) {
        try {
            var leaderboard = GameMatchRepository.findLeaderboard();
            return JsonTemplates.leaderboardResponse(requestId, leaderboard);
        } catch (SQLException e) {
            return JsonTemplates.error("LEADERBOARD", requestId, "DB_ERROR", "Không tải được bảng xếp hạng: " + e.getMessage());
        }
    }

    public static ObjectNode getSoloLeaderboard(String requestId) {
        try {
            List<GameMatch> leaderboard = SoloRecordRepository.findLeaderboard();
            return JsonTemplates.ok("SOLO_LEADERBOARD", requestId, leaderboard);
        } catch (SQLException e) {
            return JsonTemplates.error("SOLO_LEADERBOARD", requestId, "DB_ERROR", "Không tải được BXH Solo: " + e.getMessage());
        }
    }

    public static ObjectNode getCardCollection(String requestId) {
        try {
            List<ActionCard> cards = List.copyOf(CardRepository.findAll().values());
            return JsonTemplates.actionCardsResponse(requestId, cards);
        } catch (SQLException e) {
            return JsonTemplates.error("LIST_ACTION_CARDS", requestId, "DB_ERROR", "Không tải được danh sách thẻ: " + e.getMessage());
        }
    }
}