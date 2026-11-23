package com.mcserver.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.mcserver.config.DBConfig;
import com.mcserver.model.GameMatch;
import com.mcserver.model.GameType;
import com.mcserver.model.User;

public final class SoloRecordRepository {
    private SoloRecordRepository() {}

    /**
     * Lấy ván Solo đạt thành tích cao nhất của 1 user (trả về nguyên object GameMatch)
     */
    public static GameMatch findBestMatch(long userId) throws SQLException {
        String sql = """
                SELECT gm.id, gm.join_code, gm.started_at, gm.ended_at,
                       gt.id AS gt_id, gt.name AS gt_name, gt.time_limit,
                       gt.board_size_m, gt.board_size_n, gt.game_mode, gt.level,
                       u.id AS u_id, u.username, u.display_name
                FROM game_matches gm
                JOIN game_types gt ON gt.id = gm.game_type_id
                JOIN users u ON u.id = gm.winner_id
                WHERE gm.winner_id = ?
                  AND gt.game_mode = 'SOLO_MATCH'
                  AND gm.ended_at IS NOT NULL
                ORDER BY gt.level DESC,
                         TIMESTAMPDIFF(MICROSECOND, gm.started_at, gm.ended_at) ASC
                LIMIT 1
                """;

        try (Connection conn = DBConfig.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapToGameMatch(rs);
                }
            }
        }
        return null;
    }

    /**
     * Bảng xếp hạng Top 50 chế độ Solo (trả về List<GameMatch> nguyên bản)
     */
    public static List<GameMatch> findLeaderboard() throws SQLException {
        String sql = """
                SELECT gm.id, gm.join_code, gm.started_at, gm.ended_at,
                       gt.id AS gt_id, gt.name AS gt_name, gt.time_limit,
                       gt.board_size_m, gt.board_size_n, gt.game_mode, gt.level,
                       u.id AS u_id, u.username, u.display_name
                FROM game_matches gm
                JOIN game_types gt ON gt.id = gm.game_type_id
                JOIN users u ON u.id = gm.winner_id
                WHERE gt.game_mode = 'SOLO_MATCH'
                  AND gm.ended_at IS NOT NULL
                ORDER BY gt.level DESC,
                         TIMESTAMPDIFF(MICROSECOND, gm.started_at, gm.ended_at) ASC,
                         gm.ended_at ASC
                LIMIT 50
                """;

        List<GameMatch> list = new ArrayList<>();
        try (Connection conn = DBConfig.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapToGameMatch(rs));
            }
        }
        return list;
    }

    /**
     * Lưu kết quả 1 ván Solo
     */
    public static void save(long userId, int level, LocalDateTime startedAt, LocalDateTime endedAt, int score, int coinsEarned) throws SQLException {
        String findTypeSql = "SELECT id FROM game_types WHERE game_mode = 'SOLO_MATCH' AND level = ? LIMIT 1";
        String insertMatchSql = "INSERT INTO game_matches (join_code, game_type_id, started_at, ended_at, winner_id) VALUES (?, ?, ?, ?, ?)";
        String insertPlayerSql = "INSERT INTO match_players (match_id, player_id, score, coins_earned) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConfig.connect()) {
            conn.setAutoCommit(false);
            try {
                long gameTypeId;
                try (PreparedStatement ps = conn.prepareStatement(findTypeSql)) {
                    ps.setInt(1, level);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("SOLO_MATCH level not found: " + level);
                        gameTypeId = rs.getLong("id");
                    }
                }

                long matchId;
                try (PreparedStatement ps = conn.prepareStatement(insertMatchSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, "SOLO-" + userId);
                    ps.setLong(2, gameTypeId);
                    ps.setTimestamp(3, Timestamp.valueOf(startedAt));
                    if (endedAt != null) {
                        ps.setTimestamp(4, Timestamp.valueOf(endedAt));
                        ps.setLong(5, userId);
                    } else {
                        ps.setNull(4, Types.TIMESTAMP);
                        ps.setNull(5, Types.BIGINT);
                    }
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        matchId = keys.getLong(1);
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(insertPlayerSql)) {
                    ps.setLong(1, matchId);
                    ps.setLong(2, userId);
                    ps.setInt(3, score);
                    ps.setInt(4, coinsEarned);
                    ps.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private static GameMatch mapToGameMatch(ResultSet rs) throws SQLException {
        GameType gameType = new GameType();
        gameType.setId(rs.getLong("gt_id"));
        gameType.setName(rs.getString("gt_name"));
        gameType.setTimeLimit(rs.getInt("time_limit"));
        gameType.setBoardSizeM(rs.getInt("board_size_m"));
        gameType.setBoardSizeN(rs.getInt("board_size_n"));
        gameType.setGameMode(GameType.GameMode.valueOf(rs.getString("game_mode")));
        gameType.setLevel(rs.getInt("level"));

        User winner = new User();
        winner.setId(rs.getLong("u_id"));
        winner.setUsername(rs.getString("username"));
        winner.setDisplayName(rs.getString("display_name"));

        GameMatch match = new GameMatch();
        match.setId(rs.getLong("id"));
        match.setJoinCode(rs.getString("join_code"));
        match.setGameType(gameType);
        match.setStartedAt(rs.getTimestamp("started_at").toLocalDateTime());
        Timestamp ended = rs.getTimestamp("ended_at");
        if (ended != null) {
            match.setEndedAt(ended.toLocalDateTime());
        }
        match.setWinner(winner);
        return match;
    }
}