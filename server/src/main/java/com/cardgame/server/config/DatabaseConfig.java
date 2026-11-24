package com.cardgame.server.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
/** Loads local database settings and provides JDBC connections for the server. */
public final class DatabaseConfig {
    private static String url;
    private static String username;
    private static String password;
    private static int serverPort = 5000;
    private DatabaseConfig() {
    }
    public static void load() throws Exception {
        Properties properties = new Properties();
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) properties.load(input);
        }
        url = System.getenv().getOrDefault("CARDGAME_DB_URL", properties.getProperty("db.url",
        "jdbc:mysql://localhost:3306/card_memory_game?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true"));
        username = System.getenv().getOrDefault("CARDGAME_DB_USER", properties.getProperty("db.username", "root"));
        password = System.getenv().getOrDefault("CARDGAME_DB_PASSWORD", properties.getProperty("db.password", ""));
        serverPort = Integer.parseInt(properties.getProperty("server.port", "5000"));
        Class.forName("com.mysql.cj.jdbc.Driver");
    }
    public static Connection getConnection() throws SQLException {
        if (url == null) throw new SQLException("DatabaseConfig chưa được khởi tạo.");
        return DriverManager.getConnection(url, username, password);
    }
    public static int getServerPort() {
        return serverPort;
    }
}
