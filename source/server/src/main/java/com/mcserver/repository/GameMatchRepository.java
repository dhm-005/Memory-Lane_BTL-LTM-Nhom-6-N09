
package com.mcserver.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mcserver.config.DBConfig;

public final class GameMatchRepository {

    private GameMatchRepository() {}

    /**
     * Lấy lịch sử 50 trận gần nhất của người chơi
     */
    public static List<Map<String, Object>> findHistory(Long userId)
            throws SQLException {

        List<Map<String, Object>> items = new ArrayList<>();

        String sql = """
                SELECT gm.id AS match_id,
                       gm.join_code,
                       gm.started_at,
                       gm.ended_at,
                       gm.winner_id,
                       mp1.score AS my_score,
                       mp1.coins_earned,
                       mp2.score AS opponent_score,
                       u2.username AS opponent_username
                FROM game_matches gm
                JOIN match_players mp1
                    ON mp1.match_id = gm.id
                    AND mp1.player_id = ?
                LEFT JOIN match_players mp2
                    ON mp2.match_id = gm.id
                    AND mp2.player_id <> ?
                LEFT JOIN users u2
                    ON u2.id = mp2.player_id
                WHERE gm.ended_at IS NOT NULL
                ORDER BY gm.ended_at DESC
                LIMIT 50
                """;

        try (Connection conn = DBConfig.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setLong(2, userId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    long winnerId = rs.getLong("winner_id");
                    boolean hasWinner = !rs.wasNull();

                    int myScore = rs.getInt("my_score");
                    int opponentScore = rs.getInt("opponent_score");

                    String outcome = !hasWinner
                            ? "Hòa"
                            : (winnerId == userId ? "Thắng" : "Thua");

                    Map<String, Object> item = new LinkedHashMap<>();

                    item.put("matchId", rs.getLong("match_id"));
                    item.put("room", rs.getString("join_code"));
                    item.put("opponent",
                            rs.getString("opponent_username"));
                    item.put("score",
                            myScore + " - " + opponentScore);
                    item.put("result", outcome);
                    item.put("coinChange",
                            rs.getInt("coins_earned"));
                    item.put("ended", rs.getTimestamp("ended_at"));

                    items.add(item);
                }
            }
        }

        return items;
    }

    /**
     * Lấy bảng xếp hạng Top 50 người chơi
     */
    public static List<Map<String, Object>> findLeaderboard()
            throws SQLException {

        List<Map<String, Object>> leaderboard = new ArrayList<>();

        String sql = """
                SELECT u.id,
                       u.username,
                       u.display_name,
                       u.total_score,
                       COUNT(gm.id) AS total_games,
                       COUNT(CASE
                           WHEN gm.winner_id = u.id THEN 1
                       END) AS wins
                FROM users u
                LEFT JOIN match_players mp
                    ON mp.player_id = u.id
                LEFT JOIN game_matches gm
                    ON gm.id = mp.match_id
                    AND gm.ended_at IS NOT NULL
                WHERE u.role = 'PLAYER'
                GROUP BY u.id, u.username,
                         u.display_name, u.total_score
                ORDER BY u.total_score DESC, wins DESC
                LIMIT 50
                """;

        try (Connection conn = DBConfig.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                int totalGames = rs.getInt("total_games");
                int wins = rs.getInt("wins");

                double winRate = totalGames == 0
                        ? 0.0
                        : Math.round(
                            wins * 1000.0 / totalGames
                        ) / 10.0;

                Map<String, Object> item = new LinkedHashMap<>();

                item.put("username", rs.getString("username"));
                item.put("displayName", rs.getString("display_name"));
                item.put("points", rs.getInt("total_score"));
                item.put("wins", wins);
                item.put("totalGames", totalGames);
                item.put("winRate", winRate);

                leaderboard.add(item);
            }
        }

        return leaderboard;
    }
}
