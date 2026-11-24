package com.cardgame.server.repository;

import com.cardgame.server.config.DatabaseConfig;
import com.cardgame.server.model.User;
import java.sql.*;
public final class UserRepository {
    private UserRepository() {
    }
    public static void create(String username, String passwordHash) throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection();
        PreparedStatement statement = connection.prepareStatement(
        "INSERT INTO users(username,password_hash,coins,ranking_points) VALUES(?,?,50,0)")) {
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.executeUpdate();
        }
    }
    public static User findByUsername(String username) throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection();
        PreparedStatement statement = connection.prepareStatement(
        "SELECT id,username,password_hash,coins,ranking_points,created_at FROM users WHERE username=?")) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return null;
                Timestamp createdAt = result.getTimestamp("created_at");
                return new User(
                    result.getInt("id"),
                    result.getString("username"),
                    result.getString("password_hash"),
                    result.getInt("coins"),
                    result.getInt("ranking_points"),
                    createdAt == null ? null : createdAt.toLocalDateTime()
                );
            }
        }
    }
    public static String findPasswordHash(int userId) throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection();
        PreparedStatement statement = connection.prepareStatement(
        "SELECT password_hash FROM users WHERE id=?")) {
            statement.setInt(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getString(1) : null;
            }
        }
    }
    public static void updatePassword(int userId, String passwordHash) throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection();
        PreparedStatement statement = connection.prepareStatement(
        "UPDATE users SET password_hash=? WHERE id=?")) {
            statement.setString(1, passwordHash);
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
    }
    public static int findCoins(int userId) {
        try (Connection connection = DatabaseConfig.getConnection();
        PreparedStatement statement = connection.prepareStatement(
        "SELECT coins FROM users WHERE id=?")) {
            statement.setInt(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot read user balance", e);
        }
    }
    /** Applies a coin delta while guaranteeing that the stored balance stays non-negative. */
    public static int adjustCoins(int userId, int delta) {
        try (Connection connection = DatabaseConfig.getConnection();
        PreparedStatement statement = connection.prepareStatement(
        "UPDATE users SET coins=GREATEST(0, coins + ?) WHERE id=?")) {
            statement.setInt(1, delta);
            statement.setInt(2, userId);
            statement.executeUpdate();
            return findCoins(userId);
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot update user balance", e);
        }
    }
    public static void adjustRankingPoints(int userId, int delta) {
        try (Connection connection = DatabaseConfig.getConnection();
        PreparedStatement statement = connection.prepareStatement(
        "UPDATE users SET ranking_points=GREATEST(0, ranking_points + ?) WHERE id=?")) {
            statement.setInt(1, delta);
            statement.setInt(2, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot update ranking points", e);
        }
    }
}
