package com.cardgame.server.service;


import tools.jackson.databind.node.ObjectNode;
import com.cardgame.server.network.ClientHandler;
import com.cardgame.server.repository.GameRepository;
import com.cardgame.server.util.Json;
import static com.cardgame.server.MainServer.error;
public final class HistoryService {
    private HistoryService() {
    }
    public static void history(ClientHandler client) {
        if (client.userId <= 0) { client.send(error("Vui lòng đăng nhập trước.")); return; }
        try {
            ObjectNode response = Json.object("HISTORY");
            response.set("items", GameRepository.findHistory(client.userId));
            client.send(response);
        } catch (Exception e) {
            client.send(error("Không tải được lịch sử: " + e.getMessage()));
        }
    }
    public static void leaderboard(ClientHandler client) {
        if (client.userId <= 0) { client.send(error("Vui lòng đăng nhập trước.")); return; }
        try {
            ObjectNode response = Json.object("LEADERBOARD");
            response.set("items", GameRepository.findLeaderboard());
            client.send(response);
        } catch (Exception e) {
            client.send(error("Không tải được bảng xếp hạng: " + e.getMessage()));
        }
    }

}
