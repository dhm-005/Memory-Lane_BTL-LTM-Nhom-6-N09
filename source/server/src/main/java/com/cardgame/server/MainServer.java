package com.cardgame.server;


import tools.jackson.databind.node.ObjectNode;
import com.cardgame.server.config.DatabaseConfig;
import com.cardgame.server.service.CardService;
import com.cardgame.server.repository.UserRepository;
import com.cardgame.server.util.Json;
import java.sql.Connection;
/** Application entry point and small compatibility facade for shared server utilities. */
public class MainServer {
    public static int port = 5000;
    public static final int BOARD_SIZE = 36;
    public static final int PAIR_COUNT = 18;
    public static void main(String[] args) throws Exception {
        DatabaseConfig.load();
        port = DatabaseConfig.getServerPort();
        try (Connection ignored = DatabaseConfig.getConnection()) {
            System.out.println("[DB] Connected to card_memory_game");
        }
        CardService.loadFromDatabase();
        new com.cardgame.server.network.TcpServer(port).start();
    }



    public static ObjectNode error(String message) {
        ObjectNode o=Json.object("ERROR");
        o.put("message",message);
        return o;
    }
    public static int balance(int userId) {
        return UserRepository.findCoins(userId);
    }
    public static int adjustCoins(int userId, int delta) {
        int before = UserRepository.findCoins(userId);
        int after = UserRepository.adjustCoins(userId, delta);
        return after - before;
    }
    public static void adjustPoints(int userId, int delta) {
        UserRepository.adjustRankingPoints(userId, delta);
    }
}
