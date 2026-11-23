package com.mcserver.network;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mcserver.util.JsonTemplates;
import com.mcserver.util.JsonUtils;

public class MessageDispatcher {
    private final Map<String, TypeHandler> handlers = new HashMap<>();

    public MessageDispatcher(List<TypeHandler> typeHandlers) {
        for (TypeHandler h : typeHandlers) {
            for (String t : h.supportedTypes()) {
                handlers.put(t, h);
            }
        }
    }

    public ObjectNode dispatch(String line, ClientSession session) {
        JsonNode root;
        try {
            root = JsonUtils.parse(line);
        } catch (JsonProcessingException e) {
            return JsonTemplates.error("ERROR", null, "INVALID_JSON", "Mỗi dòng phải là một JSON hợp lệ");
        }
        if (root == null || !root.isObject() || !root.hasNonNull("type")) {
            return JsonTemplates.error("ERROR", null, "INVALID_REQUEST", "Thông điệp phải là JSON object có trường 'type'");
        }

        String type = root.get("type").asText();
        String requestId = root.hasNonNull("requestId") ? root.get("requestId").asText() : null;
        TypeHandler handler = handlers.get(type);
        if (handler == null) {
            return JsonTemplates.error("ERROR", requestId, "UNKNOWN_TYPE", "Unknown type: " + type);
        }
        try {
            return handler.handle(type, requestId, root.path("data"), session);
        } catch (IllegalArgumentException e) {
            return JsonTemplates.error("ERROR", requestId, "INVALID_REQUEST", e.getMessage());
        } catch (RuntimeException e) {
            return JsonTemplates.error("ERROR", requestId, "INTERNAL", "Lỗi xử lý phía server");
        }
    }
}