package com.mcserver.network;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcserver.service.game.GameMatchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TcpRequestHandler {
    private final ObjectMapper objectMapper;
    private final GameMatchService gameMatchService;

    public TcpResponse handle(String line) {
        final JsonNode request;
        try {
            request = objectMapper.readTree(line);
        } catch (JsonProcessingException e) {
            return TcpResponse.failure("INVALID_JSON", "Request must be one valid JSON object per line");
        }

        if (request == null || !request.isObject()) {
            return TcpResponse.failure("INVALID_REQUEST", "Request must be a JSON object");
        }

        try {
            String action = requiredText(request, "action");
            return switch (action) {
                case "health" -> TcpResponse.success(Map.of(
                        "status", "UP",
                        "matchCount", gameMatchService.countMatches()));
                case "matches.list" -> TcpResponse.success(gameMatchService.listMatches());
                case "matches.create" -> TcpResponse.success(
                        gameMatchService.createMatch(requiredLong(request, "joinCode")));
                case "matches.finish" -> TcpResponse.success(
                        gameMatchService.finishMatchSummary(requiredLong(request, "matchId")));
                default -> TcpResponse.failure("UNKNOWN_ACTION", "Unknown action: " + action);
            };
        } catch (IllegalArgumentException e) {
            return TcpResponse.failure("INVALID_REQUEST", e.getMessage());
        } catch (DataAccessException e) {
            log.error("Database operation failed while processing TCP request", e);
            return TcpResponse.failure("DATABASE_ERROR", "Database operation failed");
        }
    }

    private String requiredText(JsonNode request, String field) {
        JsonNode value = request.get(field);
        if (value == null || !value.isTextual() || value.asText().isBlank()) {
            throw new IllegalArgumentException("Field '" + field + "' must be a non-empty string");
        }
        return value.asText();
    }

    private long requiredLong(JsonNode request, String field) {
        JsonNode value = request.get(field);
        if (value == null || !value.isIntegralNumber() || !value.canConvertToLong()) {
            throw new IllegalArgumentException("Field '" + field + "' must be an integer");
        }
        return value.longValue();
    }
}
