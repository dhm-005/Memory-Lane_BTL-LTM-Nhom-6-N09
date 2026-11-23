package com.mcserver.network;

import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface TypeHandler {
    Set<String> supportedTypes();
    ObjectNode handle(String type, String requestId, JsonNode data, ClientSession session);
}