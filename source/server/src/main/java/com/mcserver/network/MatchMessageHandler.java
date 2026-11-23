package com.mcserver.network;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mcserver.service.GameMatchService;
import com.mcserver.util.JsonTemplates;
import com.mcserver.util.JsonUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MatchMessageHandler implements TypeHandler {
    private final GameMatchService gameService = null;

    @Override
    public Set<String> supportedTypes() {
        return Set.of("HEALTH", "LIST_MATCHES", "CREATE_MATCH", "FINISH_MATCH");
    }

    @Override
    public ObjectNode handle(String type, String requestId, JsonNode data, ClientSession session) {
        try {
            return switch (type) {
                case "HEALTH" -> JsonTemplates.ok(type, requestId, Map.of(
                        "status", "UP",
                        "matchCount", gameService.countMatches()));
                case "LIST_MATCHES" -> JsonTemplates.ok(type, requestId, gameService.listMatches());
                case "CREATE_MATCH" -> {
                    String joinCode = JsonUtils.getString(data, "joinCode");
                    if (joinCode.isBlank()) {
                        throw new IllegalArgumentException("Field 'joinCode' must be a non-empty string");
                    }
                    yield JsonTemplates.ok(type, requestId, gameService.createMatch(joinCode));
                }
                default -> { // FINISH_MATCH
                    long matchId = JsonUtils.getLong(data, "matchId", -1L);
                    if (matchId <= 0) {
                        throw new IllegalArgumentException("Field 'matchId' must be a valid positive integer");
                    }
                    yield JsonTemplates.ok(type, requestId, gameService.finishMatchSummary(matchId));
                }
            };
        } catch (IllegalArgumentException e) {
            return JsonTemplates.error("ERROR", requestId, "INVALID_REQUEST", e.getMessage());
        }
    }
}