package com.cardgame.server.repository;


import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import com.cardgame.server.config.DatabaseConfig;
import com.cardgame.server.util.Json;
import java.sql.*;
public final class SoloRecordRepository {
    private SoloRecordRepository() {
    }
    public static ObjectNode findBest(int userId) throws SQLException {
        ObjectNode data = Json.MAPPER.createObjectNode();
        try (Connection connection = DatabaseConfig.getConnection();
        PreparedStatement statement = connection.prepareStatement(
        "SELECT best_level,best_time_ms FROM solo_records WHERE user_id=?")) {
            statement.setInt(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    data.put("level", result.getInt(1));
                    long time = result.getLong(2);
                    data.put("timeMs", result.wasNull() ? -1 : time);
                } else {
                    data.put("level", 0);
                    data.put("timeMs", -1);
                }
            }
        }
        return data;
    }
    public static void save(int userId, int level, int timeMs) throws SQLException {
        String sql = "INSERT INTO solo_records(user_id,best_level,best_time_ms,completed_runs) VALUES(?,?,?,?) " +
        "ON DUPLICATE KEY UPDATE best_time_ms=CASE " +
        "WHEN VALUES(best_level)>best_level THEN VALUES(best_time_ms) " +
        "WHEN VALUES(best_level)=best_level THEN CASE WHEN best_time_ms IS NULL THEN VALUES(best_time_ms) " +
        "WHEN VALUES(best_time_ms) IS NULL THEN best_time_ms ELSE LEAST(best_time_ms,VALUES(best_time_ms)) END " +
        "ELSE best_time_ms END, best_level=GREATEST(best_level,VALUES(best_level)), " +
        "completed_runs=completed_runs+VALUES(completed_runs)";
        try (Connection connection = DatabaseConfig.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, level);
            if (timeMs >= 0) statement.setLong(3, timeMs);
            else statement.setNull(3, Types.BIGINT);
            statement.setInt(4, level == 5 ? 1 : 0);
            statement.executeUpdate();
        }
    }
    public static ArrayNode findLeaderboard() throws SQLException {
        ArrayNode items = Json.MAPPER.createArrayNode();
        String sql = "SELECT u.username,s.best_level,s.best_time_ms FROM solo_records s " +
        "JOIN users u ON u.id=s.user_id " +
        "ORDER BY s.best_level DESC, COALESCE(s.best_time_ms,9223372036854775807) ASC, s.updated_at ASC LIMIT 50";
        try (Connection connection = DatabaseConfig.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                ObjectNode item = Json.MAPPER.createObjectNode();
                item.put("username", result.getString(1));
                int level = result.getInt(2);
                item.put("bestLevel", "Level " + level + " / 5");
                item.put("achievement", level >= 5 ? "Hoàn thành 5 Level" : "Đạt Level " + level);
                long time = result.getLong(3);
                item.put("bestTime", result.wasNull() ? "—" : formatTime(time));
                items.add(item);
            }
        }
        return items;
    }
    private static String formatTime(long millis) {
        long seconds = millis / 1000;
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }
}
