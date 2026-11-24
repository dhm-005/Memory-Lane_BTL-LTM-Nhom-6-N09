package com.cardgame.server.network;


import tools.jackson.databind.node.ObjectNode;
import com.cardgame.server.MainServer;
import com.cardgame.server.service.*;
import com.cardgame.server.util.Json;
public final class MessageDispatcher {
    private MessageDispatcher() {
    }
    public static void dispatch(ClientHandler client, ObjectNode message) throws Exception {
        switch (Json.str(message,"type")) {
            case "REGISTER" -> AuthService.register(client,Json.str(message,"username"),Json.str(message,"password"));
            case "LOGIN" -> AuthService.login(client,Json.str(message,"username"),Json.str(message,"password"));
            case "CHANGE_PASSWORD" -> AuthService.changePassword(client,Json.str(message,"oldPassword"),Json.str(message,"newPassword"));
            case "LIST_ROOMS" -> RoomService.listRooms(client);
            case "CREATE_ROOM" -> RoomService.createRoom(client);
            case "JOIN_ROOM" -> RoomService.joinRoom(client,Json.str(message,"roomId"));
            case "READY" -> RoomService.setReady(client, true);
            case "UNREADY" -> RoomService.setReady(client, false);
            case "LEAVE_ROOM" -> RoomService.leave(client);
            case "RPS_CHOICE" -> GameService.rpsChoice(client,Json.str(message,"choice"));
            case "CHOOSE_ORDER" -> GameService.chooseOrder(client,Json.str(message,"order"));
            case "BUY_SELECTION" -> GameService.updateBuySelection(client,message);
            case "BUY_CONFIRM" -> GameService.confirmBuy(client);
            case "FLIP" -> GameService.flip(client,Json.integer(message,"index",-1));
            case "USE_CARD" -> GameService.useCard(client,Json.str(message,"code"),Json.integer(message,"index",-1));
            case "SKIP_CARD" -> GameService.skipCard(client);
            case "SHIELD_RESPONSE" -> GameService.shieldResponse(client,message.has("use")&&message.get("use").asBoolean());
            case "LEAVE_MATCH" -> GameService.leaveMatch(client);
            case "SOLO_START" -> SoloService.start(client);
            case "SOLO_BEST" -> SoloService.soloBest(client);
            case "SOLO_LEVEL_COMPLETE" -> SoloService.soloLevelComplete(client,Json.integer(message,"level",0),Json.integer(message,"timeMs",-1));
            case "HISTORY" -> HistoryService.history(client);
            case "LEADERBOARD" -> HistoryService.leaderboard(client);
            case "SOLO_LEADERBOARD" -> SoloService.soloLeaderboard(client);
            case "CARD_COLLECTION" -> CardService.collection(client);
            case "LOGOUT" -> AuthService.logout(client);
            default -> client.send(MainServer.error("Không hỗ trợ yêu cầu này."));
        }
    }
}
