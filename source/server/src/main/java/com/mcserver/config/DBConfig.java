
package com.mcserver.config;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConfig {

   public static Connection connect() throws SQLException {
        return DriverManager.getConnection(
                Config.get("db.url"),
                Config.get("db.username"),
                Config.get("db.password")
        );
    }
}