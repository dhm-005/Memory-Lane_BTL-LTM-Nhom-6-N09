package com.cardgame.server.service;


import tools.jackson.databind.node.ObjectNode;
import com.cardgame.server.network.ClientHandler;
import com.cardgame.server.repository.SoloRecordRepository;
import com.cardgame.server.repository.UserRepository;
import com.cardgame.server.util.Json;
import static com.cardgame.server.MainServer.error;

/** Handles Solo progression, server-side timing, personal records and rewards. */
public final class SoloService {
    private static final long MIN_MILLIS_PER_PAIR = 1000L;
    private static final long LEVEL_TRANSITION_MILLIS = 1000L;

    private SoloService() {}

    public static void start(ClientHandler client) {
        if (client.userId <= 0) {
            client.send(error("Vui lòng đăng nhập trước khi chơi Solo."));
            return;
        }
        long now = System.nanoTime();
        client.soloProgressLevel = 0;
        client.soloRunStartedAtNanos = now;
        client.soloLevelStartedAtNanos = now;
    }

    public static void soloBest(ClientHandler client) {
        if (client.userId <= 0) { client.send(error("Vui lòng đăng nhập trước.")); return; }
        try {
            ObjectNode response = Json.object("SOLO_BEST");
            ObjectNode best = SoloRecordRepository.findBest(client.userId);
            response.setAll(best);
            client.send(response);
        } catch (Exception e) {
            client.send(error("Không tải được thành tích chơi đơn: " + e.getMessage()));
        }
    }

    public static void soloLevelComplete(ClientHandler client, int level, int ignoredClientTimeMs) {
        if (client.userId <= 0 || client.soloRunStartedAtNanos == 0) {
            client.send(error("Bạn chưa bắt đầu lượt chơi Solo."));
            return;
        }
        if (level != client.soloProgressLevel + 1 || level < 1 || level > 5) {
            client.send(error("Cấp độ không hợp lệ hoặc không theo thứ tự."));
            return;
        }

        long now = System.nanoTime();
        long elapsedLevelMs = (now - client.soloLevelStartedAtNanos) / 1_000_000L;
        int[] pairs = {0, 2, 4, 8, 12, 18};
        long minimumMs = pairs[level] * MIN_MILLIS_PER_PAIR;
        if (elapsedLevelMs < minimumMs) {
            client.send(error("Level hoàn thành quá nhanh, hệ thống chưa ghi nhận."));
            return;
        }

        long totalElapsedMs = (now - client.soloRunStartedAtNanos) / 1_000_000L;
        int serverTimeMs = (int) Math.min(Integer.MAX_VALUE, totalElapsedMs);
        saveSoloRecord(client, level, serverTimeMs);

        int currentCoins = UserRepository.findCoins(client.userId);
        if (level == 4 || level == 5) {
            try {
                UserRepository.adjustCoins(client.userId, 20);
                currentCoins = UserRepository.findCoins(client.userId);
                ObjectNode reward = Json.object("SOLO_REWARD_OK");
                reward.put("coins", currentCoins);
                reward.put("amount", 20);
                reward.put("level", level);
                client.send(reward);
            } catch (Exception e) {
                client.send(error("Không cập nhật được xu chơi đơn: " + e.getMessage()));
                return;
            }
        }
        client.soloProgressLevel = level;
        ObjectNode accepted = Json.object("SOLO_LEVEL_OK");
        accepted.put("level", level);
        accepted.put("timeMs", serverTimeMs);
        accepted.put("coins", currentCoins);
        client.send(accepted);
        if (level < 5) {
            client.soloLevelStartedAtNanos = now + LEVEL_TRANSITION_MILLIS * 1_000_000L;
        } else {
            client.soloRunStartedAtNanos = 0;
            client.soloLevelStartedAtNanos = 0;
        }
    }

    public static void saveSoloRecord(ClientHandler client, int level, int timeMs) {
        if (client.userId <= 0) return;
        try {
            SoloRecordRepository.save(client.userId, Math.max(0, Math.min(5, level)), timeMs);
        } catch (Exception e) {
            System.err.println("[DB] Solo record failed: " + e.getMessage());
        }
    }

    public static void soloLeaderboard(ClientHandler client) {
        if (client.userId <= 0) { client.send(error("Vui lòng đăng nhập trước.")); return; }
        try {
            ObjectNode response = Json.object("SOLO_LEADERBOARD");
            response.set("items", SoloRecordRepository.findLeaderboard());
            client.send(response);
        } catch (Exception e) {
            client.send(error("Không tải được BXH Solo: " + e.getMessage()));
        }
    }
}
