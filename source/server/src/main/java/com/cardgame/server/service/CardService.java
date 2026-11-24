package com.cardgame.server.service;


import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import com.cardgame.server.repository.CardRepository;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import com.cardgame.server.model.Card;
import com.cardgame.server.network.ClientHandler;
import com.cardgame.server.util.Json;
/** Card catalogue serialization, DB loading and collection endpoint. */
public final class CardService {
    private CardService() {
    }
    public static final Map<String, Card> CARDS = new LinkedHashMap<>();
    public static final String[][] CARD_CODES = {
        {
            "A01", "A02", "A03", "A04"
        }
        ,
        {
            "S01", "S02", "S03", "S04"
        }
        ,
        {
            "L01", "L02", "L03", "L04"
        }
    };
    static {
        add("A01", "ATTACK", "Đóng băng", "Đối thủ mất lượt kế tiếp; bạn được tiếp tục.", 5);
        add("A02", "ATTACK", "Khóa bài", "Khóa một lá úp trong lượt kế tiếp của đối thủ.", 3);
        add("A03", "ATTACK", "Lời nguyền", "Nếu đối thủ lật sai ở lượt bị nguyền, họ mất thêm một lượt sau đó.", 5);
        add("A04", "ATTACK", "Ảo ảnh", "Đổi vị trí ngẫu nhiên của hai lá chưa ghép.", 3);
        add("S01", "SUPPORT", "Con mắt tinh anh", "Xem riêng một lá úp trong 2 giây.", 3);
        add("S02", "SUPPORT", "Khiên bảo vệ", "Chặn một thẻ Attack nhắm vào bạn.", 4);
        add("S03", "SUPPORT", "Tăng tốc", "Sau khi lật sai, được thêm một lần thử.", 4);
        add("S04", "SUPPORT", "Ngôi sao may mắn", "Có 75% cơ hội tự ghép một cặp.", 5);
        add("L01", "LUCK", "Rương kho báu", "Thắng: nhận thêm 10 xu.", 2);
        add("L02", "LUCK", "Xúc xắc may mắn", "Thắng: 50% cơ hội nhận thêm 20 xu.", 1);
        add("L03", "LUCK", "Móng ngựa vàng", "Thắng: nhân đôi thưởng thắng cơ bản.", 2);
        add("L04", "LUCK", "Vương miện vàng", "Thua: 50% cơ hội tránh bị trừ 10 xu.", 1);
    }
    private static void add(String code, String category, String name, String description, int price) {
        CARDS.put(code, new Card(code, category, name, description, price));
    }
    public static void loadFromDatabase() throws SQLException {
        Map<String, Card> loaded = CardRepository.findAll();
        java.util.Set<String> expectedCodes = new java.util.HashSet<>();
        for (String[] group : CARD_CODES) java.util.Collections.addAll(expectedCodes, group);
        if (!loaded.keySet().equals(expectedCodes)) {
            throw new SQLException("Danh mục thẻ trong Database không khớp 12 mã thẻ yêu cầu.");
        }
        if (loaded.size() == expectedCodes.size()) {
            CARDS.clear();
            CARDS.putAll(loaded);
            Card luckyStar = CARDS.get("S04");
            if (luckyStar != null) {
                CARDS.put("S04", new Card(luckyStar.code, luckyStar.category, luckyStar.name,
                    "Có 75% cơ hội tự ghép một cặp.", luckyStar.price));
            }
        }
        System.out.println("[DB] Loaded " + CARDS.size() + " game cards");
    }
    public static ObjectNode toJson(Card card) {
        ObjectNode result = Json.MAPPER.createObjectNode();
        result.put("code", card.code);
        result.put("category", card.category);
        result.put("name", card.name);
        result.put("description", card.description);
        result.put("price", card.price);
        return result;
    }
    public static ArrayNode catalogue() {
        ArrayNode result = Json.MAPPER.createArrayNode();
        for (Card card : CARDS.values()) result.add(toJson(card));
        return result;
    }
    public static void collection(ClientHandler client) {
        ObjectNode response = Json.object("CARD_COLLECTION");
        response.set("cards", catalogue());
        client.send(response);
    }
}
