package com.mcserver.network;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class MessageDispatcher {
    private final ObjectMapper mapper;
    private final TcpRequestHandler legacyHandler;
    private final Map<String, TypeHandler> handlers = new HashMap<>();

    public MessageDispatcher(ObjectMapper mapper, TcpRequestHandler legacyHandler, List<TypeHandler> typeHandlers) {
        this.mapper = mapper;
        this.legacyHandler = legacyHandler;
        for (TypeHandler h : typeHandlers) {
            for (String t : h.supportedTypes()) {
                handlers.put(t, h);
            }
        }
    }

    /** Trả về object để ghi ra socket. */
    public Object dispatch(String line, ClientSession session) {
        JsonNode root;
        try {
            root = mapper.readTree(line);
        } catch (JsonProcessingException e) {
            return Message.error("ERROR", null, "INVALID_JSON", "Mỗi dòng phải là một JSON hợp lệ");
        }
        if (root == null || !root.isObject()) {
            return Message.error("ERROR", null, "INVALID_REQUEST", "Thông điệp phải là JSON object");
        }
        if (!root.has("type")) {
            return legacyHandler.handle(line); // giao thức cũ {"action": ...} (health, matches.*)
        }

        String type = root.path("type").asText();
        String requestId = root.hasNonNull("requestId") ? root.get("requestId").asText() : null;
        TypeHandler handler = handlers.get(type);
        if (handler == null) {
            return Message.error("ERROR", requestId, "UNKNOWN_TYPE", "Unknown type: " + type);
        }
        try {
            return handler.handle(type, requestId, root.path("data"), session);
        } catch (IllegalArgumentException e) {
            return Message.error("ERROR", requestId, "INVALID_REQUEST", e.getMessage());
        } catch (RuntimeException e) {
            return Message.error("ERROR", requestId, "INTERNAL", "Lỗi xử lý phía server");
        }
    }
}