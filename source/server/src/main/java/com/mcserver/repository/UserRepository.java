package com.mcserver.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.mcserver.config.DBConfig;
import com.mcserver.model.User;

public final class UserRepository {

    private UserRepository() {
    }

    public static void create(String username, String passwordHash) throws SQLException {
        String sql = "INSERT INTO users(username, display_name, password_hash, role, coins, total_score) VALUES(?, ?, ?, 'PLAYER', 50, 0)";
        try (Connection connection = DBConfig.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, username);
            statement.setString(3, passwordHash);
            statement.executeUpdate();
        }
    }

    public static User findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, display_name, password_hash, role, coins, total_score, created_at, last_login_at "
                   + "FROM users WHERE username = ?";
        try (Connection connection = DBConfig.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapToUser(result) : null;
            }
        }
    }

    public static User findById(long userId) throws SQLException {
        String sql = "SELECT id, username, display_name, password_hash, role, coins, total_score, created_at, last_login_at "
                   + "FROM users WHERE id = ?";
        try (Connection connection = DBConfig.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapToUser(result) : null;
            }
        }
    }

    public static String findPasswordHash(long userId) throws SQLException {
        String sql = "SELECT password_hash FROM users WHERE id = ?";
        try (Connection connection = DBConfig.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getString("password_hash") : null;
            }
        }
    }

    public static void updateLastLogin(long userId) throws SQLException {
        String sql = "UPDATE users SET last_login_at = NOW() WHERE id = ?";
        try (Connection connection = DBConfig.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.executeUpdate();
        }
    }

    public static void updatePassword(long userId, String passwordHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection connection = DBConfig.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, passwordHash);
            statement.setLong(2, userId);
            statement.executeUpdate();
        }
    }

    public static void addCoins(long userId, int amount) throws SQLException {
        String sql = "UPDATE users SET coins = coins + ? WHERE id = ?";
        try (Connection connection = DBConfig.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, amount);
            statement.setLong(2, userId);
            statement.executeUpdate();
        }
    }

    public static boolean deductCoins(long userId, int amount) throws SQLException {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }
        String sql = "UPDATE users SET coins = coins - ? WHERE id = ? AND coins >= ?";
        try (Connection connection = DBConfig.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, amount);
            statement.setLong(2, userId);
            statement.setInt(3, amount);
            return statement.executeUpdate() > 0;
        }
    }

    public static int getCoins(long userId) throws SQLException {
        String sql = "SELECT coins FROM users WHERE id = ?";
        try (Connection conn = DBConfig.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("coins") : 0;
            }
        }
    }

    private static User mapToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setDisplayName(rs.getString("display_name"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(User.Role.valueOf(rs.getString("role")));
        u.setCoins(rs.getInt("coins"));
        u.setTotalScore(rs.getInt("total_score"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            u.setCreatedAt(created.toLocalDateTime());
        }
        Timestamp lastLogin = rs.getTimestamp("last_login_at");
        if (lastLogin != null) {
            u.setLastLoginAt(lastLogin.toLocalDateTime());
        }
        return u;
    }
}