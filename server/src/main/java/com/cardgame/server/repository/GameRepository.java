package com.cardgame.server.repository;


import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import com.cardgame.server.config.DatabaseConfig;
import com.cardgame.server.util.Json;
import java.sql.*;
import java.time.LocalDateTime;
public final class GameRepository {
    private GameRepository() {
    }
    public static ArrayNode findHistory(int userId) throws SQLException {
        ArrayNode items = Json.MAPPER.createArrayNode();
        String sql = "SELECT g.room_id,u1.username,u2.username,g.player1_score,g.player2_score,g.status," +
        "g.player1_coin_change,g.player2_coin_change,g.ended_at,g.player1_id " +
        "FROM game_results g JOIN users u1 ON u1.id=g.player1_id " +
        "JOIN users u2 ON u2.id=g.player2_id WHERE g.player1_id=? OR g.player2_id=? " +
        "ORDER BY g.ended_at DESC LIMIT 50";
        try (Connection connection = DatabaseConfig.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, userId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    String player1 = result.getString(2);
                    int player1Id = result.getInt(10);
                    int score1 = result.getInt(4);
                    int score2 = result.getInt(5);
                    String status = result.getString(6);
                    boolean isPlayer1 = userId == player1Id;
                    ObjectNode item = Json.MAPPER.createObjectNode();
                    item.put("room", result.getString(1));
                    item.put("opponent", isPlayer1 ? result.getString(3) : player1);
                    item.put("score", isPlayer1 ? score1 + " - " + score2 : score2 + " - " + score1);
                    String outcome = status.equals("CANCELLED") ? "Đã hủy" :
                    score1 == score2 ? "Hòa" :
                    (isPlayer1 ? score1 > score2 : score2 > score1) ? "Thắng" : "Thua";
                    item.put("result", outcome);
                    item.put("coinChange", isPlayer1 ? result.getInt(7) : result.getInt(8));
                    item.put("ended", result.getString(9));
                    items.add(item);
                }
            }
        }
        return items;
    }
    public static ArrayNode findLeaderboard() throws SQLException {
        ArrayNode items = Json.MAPPER.createArrayNode();
        String sql = "SELECT u.username,u.ranking_points," +
        "COUNT(CASE WHEN g.status='COMPLETED' AND g.winner_id=u.id THEN 1 END) wins," +
        "COUNT(CASE WHEN g.status='COMPLETED' THEN 1 END) games " +
        "FROM users u LEFT JOIN game_results g ON (u.id=g.player1_id OR u.id=g.player2_id) " +
        "GROUP BY u.id,u.username,u.ranking_points " +
        "HAVING COUNT(CASE WHEN g.status='COMPLETED' THEN 1 END)>0 " +
        "ORDER BY u.ranking_points DESC,(wins*1.0/NULLIF(games,0)) DESC,wins DESC,u.username ASC LIMIT 50";
        try (Connection connection = DatabaseConfig.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                ObjectNode item = Json.MAPPER.createObjectNode();
                item.put("username", result.getString(1));
                item.put("points", result.getInt(2));
                item.put("wins", result.getInt(3));
                int games = result.getInt(4);
                item.put("winRate", games == 0 ? 0 : result.getInt(3) * 100.0 / games);
                items.add(item);
            }
        }
        return items;
    }
    public record Settlement(int[] coinChanges, int[] netCoinChanges, int[] balances) {}

    /** Updates both players and inserts the match result in one database transaction. */
    public static Settlement settle(com.cardgame.server.model.GameResult game) throws SQLException {
        String balanceSql = "SELECT coins FROM users WHERE id=? FOR UPDATE";
        String updateSql = "UPDATE users SET coins=?, ranking_points=GREATEST(0, ranking_points + ?) WHERE id=?";
        String insertSql = "INSERT INTO game_results(room_id,player1_id,player2_id,player1_score,player2_score,winner_id,status,cancel_reason,player1_coin_change,player2_coin_change,player1_rank_change,player2_rank_change,started_at,ended_at) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection connection = DatabaseConfig.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int[] ids = {game.player1Id(), game.player2Id()};
                int[] requestedCoins = {game.player1CoinDelta(), game.player2CoinDelta()};
                int[] rankChanges = {game.player1RankChange(), game.player2RankChange()};
                int[] actualCoins = new int[2];
                int[] netCoins = new int[2];
                int[] balances = new int[2];

                // Pre-lock both player rows in ascending order to prevent deadlocks
                int minId = Math.min(ids[0], ids[1]);
                int maxId = Math.max(ids[0], ids[1]);
                for (int lockId : new int[]{minId, maxId}) {
                    try (PreparedStatement lock = connection.prepareStatement(balanceSql)) {
                        lock.setInt(1, lockId);
                        try (ResultSet result = lock.executeQuery()) {
                            if (!result.next()) throw new SQLException("Không tìm thấy tài khoản người chơi " + lockId);
                        }
                    }
                }

                for (int i = 0; i < 2; i++) {
                    int before;
                    try (PreparedStatement read = connection.prepareStatement("SELECT coins FROM users WHERE id=?")) {
                        read.setInt(1, ids[i]);
                        try (ResultSet result = read.executeQuery()) {
                            if (!result.next()) throw new SQLException("Không tìm thấy tài khoản người chơi " + ids[i]);
                            before = result.getInt(1);
                        }
                    }
                    balances[i] = Math.max(0, before + requestedCoins[i]);
                    actualCoins[i] = balances[i] - before;
                    int previousNet = i == 0 ? game.player1CoinChangeBeforeSettlement() : game.player2CoinChangeBeforeSettlement();
                    netCoins[i] = previousNet + actualCoins[i];
                    try (PreparedStatement update = connection.prepareStatement(updateSql)) {
                        update.setInt(1, balances[i]);
                        update.setInt(2, rankChanges[i]);
                        update.setInt(3, ids[i]);
                        update.executeUpdate();
                    }
                }

                try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
                    insert.setString(1, game.roomId());
                    insert.setInt(2, game.player1Id());
                    insert.setInt(3, game.player2Id());
                    insert.setInt(4, game.player1Score());
                    insert.setInt(5, game.player2Score());
                    if ("COMPLETED".equals(game.status()) && game.player1Score() != game.player2Score()) {
                        insert.setInt(6, game.player1Score() > game.player2Score() ? game.player1Id() : game.player2Id());
                    } else {
                        insert.setNull(6, Types.INTEGER);
                    }
                    insert.setString(7, game.status());
                    insert.setString(8, game.cancelReason());
                    insert.setInt(9, netCoins[0]);
                    insert.setInt(10, netCoins[1]);
                    insert.setInt(11, game.player1RankChange());
                    insert.setInt(12, game.player2RankChange());
                    LocalDateTime started = game.startedAt() == null ? LocalDateTime.now() : game.startedAt();
                    insert.setTimestamp(13, Timestamp.valueOf(started));
                    insert.setTimestamp(14, Timestamp.valueOf(LocalDateTime.now()));
                    insert.executeUpdate();
                }
                connection.commit();
                return new Settlement(actualCoins, netCoins, balances);
            } catch (Exception e) {
                try { connection.rollback(); } catch (SQLException rollbackError) { e.addSuppressed(rollbackError); }
                if (e instanceof SQLException sqlException) throw sqlException;
                throw new SQLException("Không thể kết toán trận đấu", e);
            } finally {
                try { connection.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
    }
}
