package com.mcserver.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class JsonUtils {
    public static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private JsonUtils() {}

    public static ObjectNode createObject() {
        return MAPPER.createObjectNode();
    }

    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize object to JSON", e);
        }
    }

    public static JsonNode toNode(Object obj) {
        return MAPPER.valueToTree(obj);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON format for " + clazz.getSimpleName(), e);
        }
    }

    public static JsonNode parse(String line) throws JsonProcessingException {
        return MAPPER.readTree(line);
    }

    public static String getString(JsonNode node, String key) {
        return node != null && node.hasNonNull(key) ? node.get(key).asText() : "";
    }

    public static int getInt(JsonNode node, String key, int fallback) {
        return node != null && node.hasNonNull(key) && node.get(key).isNumber()
                ? node.get(key).asInt()
                : fallback;
    }

    public static long getLong(JsonNode node, String key, long fallback) {
        return node != null && node.hasNonNull(key) && node.get(key).isNumber()
                ? node.get(key).asLong()
                : fallback;
    }

    public static boolean getBoolean(JsonNode node, String key, boolean fallback) {
        return node != null && node.hasNonNull(key) && node.get(key).isBoolean()
                ? node.get(key).asBoolean()
                : fallback;
    }
}