package com.mcserver.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.mcserver.config.DBConfig;
import com.mcserver.model.ActionCard;

public final class ActionCardRepository {
    private ActionCardRepository() {}

    public static Map<Long, ActionCard> findAll() throws SQLException {
        Map<Long, ActionCard> cards = new LinkedHashMap<>();
        String sql = "SELECT id, code, category, name, description, price FROM action_cards "
                   + "ORDER BY CASE category WHEN 'ATTACK' THEN 1 WHEN 'SUPPORT' THEN 2 ELSE 3 END, id";
        try (Connection conn = DBConfig.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ActionCard card = new ActionCard(
                        rs.getLong("id"),
                        rs.getString("code"),
                        rs.getString("category"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("price")
                );
                cards.put(card.getId(), card);
            }
        }
        return cards;
    }
}