package com.cardgame.server.repository;

import com.cardgame.server.config.DatabaseConfig;
import com.cardgame.server.model.Card;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;
public final class CardRepository {
    private CardRepository() {
    }
    public static Map<String, Card> findAll() throws SQLException {
        Map<String, Card> cards = new LinkedHashMap<>();
        String sql = "SELECT card_code,category,card_name,description,price FROM cards " +
        "ORDER BY CASE category WHEN 'ATTACK' THEN 1 WHEN 'SUPPORT' THEN 2 ELSE 3 END,card_code";
        try (Connection connection = DatabaseConfig.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                Card card = new Card(result.getString(1), result.getString(2), result.getString(3),
                result.getString(4), result.getInt(5));
                cards.put(card.code, card);
            }
        }
        return cards;
    }
}
