package com.mcclient.util;

import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.Map;

public final class JsonTemplates {
    private JsonTemplates() {}

    public static Map<String, Object> empty() {
        return Map.of();
    }

    public static Map<String, Object> login(String username, String password) {
        return Map.of("username", username, "password", password);
    }

    public static Map<String, Object> register(String username, String password) {
        return Map.of("username", username, "password", password);
    }

    public static Map<String, Object> changePassword(String oldPassword, String newPassword) {
        return Map.of("oldPassword", oldPassword, "newPassword", newPassword);
    }

    public static Map<String, Object> joinRoom(String roomId) {
        return Map.of("roomId", roomId);
    }

    public static Map<String, Object> setReady(boolean ready) {
        return Map.of("ready", ready);
    }

    public static Map<String, Object> flip(int index) {
        return Map.of("index", index);
    }

    public static Map<String, Object> useCard(String code, int index) {
        return Map.of("code", code, "index", index);
    }

    public static Map<String, Object> shieldResponse(boolean use) {
        return Map.of("use", use);
    }

    public static Map<String, Object> buySelection(ArrayNode codes) {
        return Map.of("codes", codes);
    }

    public static Map<String, Object> chooseOrder(String order) {
        return Map.of("order", order);
    }

    public static Map<String, Object> rpsChoice(String choice) {
        return Map.of("choice", choice);
    }

    public static Map<String, Object> soloLevelComplete(int level, int timeMs) {
        return Map.of("level", level, "timeMs", timeMs);
    }
}