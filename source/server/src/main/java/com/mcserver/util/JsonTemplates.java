package com.mcserver.util;

import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mcserver.model.ActionCard;
import com.mcserver.model.GameMatch;
import com.mcserver.model.GameType;
import com.mcserver.model.MatchMove;
import com.mcserver.model.MatchPlayer;
import com.mcserver.model.User;

import com.mcserver.model.ramOnly.Room;
import com.mcserver.model.ramOnly.BoardCard;
import com.mcserver.model.ramOnly.GameState;

public final class JsonTemplates {

    private JsonTemplates() {}

    // ==================== 1. KHUNG GÓI TIN CHUẨN ====================

    public static ObjectNode ok(String type, String requestId, Object data) {
        ObjectNode root = JsonUtils.createObject();
        root.put("type", type);
        if (requestId != null) root.put("requestId", requestId);
        root.put("success", true);
        if (data != null) root.set("data", JsonUtils.toNode(data));
        return root;
    }

    public static ObjectNode error(String type, String requestId, String code, String message) {
        ObjectNode root = JsonUtils.createObject();
        root.put("type", type);
        if (requestId != null) root.put("requestId", requestId);
        root.put("success", false);

        ObjectNode err = JsonUtils.createObject();
        err.put("code", code);
        err.put("message", message);
        root.set("data", err);
        return root;
    }

    public static ObjectNode push(String type, Object data) {
        ObjectNode root = JsonUtils.createObject();
        root.put("type", type);
        if (data != null) root.set("data", JsonUtils.toNode(data));
        return root;
    }

    // ==================== 2. TEMPLATE THEO TỪNG NGHIỆP VỤ / MODEL ====================

    // Auth & User (passwordHash tự động ẩn nhờ @JsonIgnore trong User)
    public static ObjectNode authSuccess(String type, String requestId, User user) {
        return ok(type, requestId, user);
    }

    // Phòng chờ (Lobby & Room)
    public static ObjectNode roomResponse(String type, String requestId, Room room) {
        return ok(type, requestId, room);
    }

    public static ObjectNode roomListResponse(String requestId, List<Room> rooms) {
        return ok("LIST_ROOMS", requestId, rooms);
    }

    public static ObjectNode roomsChangedPush(List<Room> rooms) {
        return push("ROOMS_CHANGED", rooms);
    }

    // Danh mục chế độ chơi & Thẻ chức năng
    public static ObjectNode gameTypesResponse(String requestId, List<GameType> gameTypes) {
        return ok("LIST_GAME_TYPES", requestId, gameTypes);
    }

    public static ObjectNode actionCardsResponse(String requestId, List<ActionCard> actionCards) {
        return ok("LIST_ACTION_CARDS", requestId, actionCards);
    }

    // Trong trận đấu (GameMatch, GameState, BoardCard, MatchMove)
    public static ObjectNode matchStartedPush(GameMatch match, GameState initialState) {
        ObjectNode payload = JsonUtils.createObject();
        payload.set("match", JsonUtils.toNode(match));
        payload.set("state", JsonUtils.toNode(initialState));
        return push("MATCH_STARTED", payload);
    }

    public static ObjectNode boardStatePush(GameState state) {
        return push("BOARD_UPDATED", state);
    }

    public static ObjectNode cardFlippedPush(BoardCard card, long playerId) {
        ObjectNode payload = JsonUtils.createObject();
        payload.set("card", JsonUtils.toNode(card));
        payload.put("playerId", playerId);
        return push("CARD_FLIPPED", payload);
    }

    public static ObjectNode moveResultPush(MatchMove move, GameState updatedState) {
        ObjectNode payload = JsonUtils.createObject();
        payload.set("move", JsonUtils.toNode(move));
        payload.set("state", JsonUtils.toNode(updatedState));
        return push("MOVE_RESULT", payload);
    }

    // Kết thúc trận & Bảng xếp hạng (MatchPlayer)
    public static ObjectNode matchEndedPush(GameMatch match, List<MatchPlayer> results) {
        ObjectNode payload = JsonUtils.createObject();
        payload.set("match", JsonUtils.toNode(match));
        payload.set("results", JsonUtils.toNode(results));
        return push("MATCH_ENDED", payload);
    }
    
    // Lịch sử đấu & Bảng xếp hạng
    public static ObjectNode matchHistoryResponse(String requestId, List<?> history) {
        return ok("MATCH_HISTORY", requestId, history);
    }

    public static ObjectNode leaderboardResponse(String requestId, List<?> leaderboard) {
        return ok("LEADERBOARD", requestId, leaderboard);
    }
}