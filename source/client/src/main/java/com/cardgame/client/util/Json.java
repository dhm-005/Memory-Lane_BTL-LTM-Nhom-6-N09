package com.cardgame.client.util;


import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import java.io.*;
public final class Json {
    public static final JsonMapper MAPPER = JsonMapper.builder().build();
    private Json() {
    }
    public static ObjectNode object(String type) {
        ObjectNode o = Json.MAPPER.createObjectNode();
        o.put("type", type);
        return o;
    }
    public static void send(BufferedWriter out, ObjectNode message) throws IOException {
        out.write(MAPPER.writeValueAsString(message));
        out.write("\n");
        out.flush();
    }
    public static ObjectNode parse(String line) {
        return (ObjectNode) MAPPER.readTree(line);
    }
    public static String str(ObjectNode o, String key) {
        return o.has(key) && !o.get(key).isNull() ? o.get(key).asText() : "";
    }
    public static int integer(ObjectNode o, String key, int fallback) {
        try {
            if (!o.has(key) || o.get(key) == null || o.get(key).isNull()) return fallback;
            return o.get(key).asInt();
        } catch (Exception e) {
            return fallback;
        }
    }
}
