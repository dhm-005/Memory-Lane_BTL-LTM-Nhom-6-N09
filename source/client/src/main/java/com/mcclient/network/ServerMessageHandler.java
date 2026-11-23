package com.mcclient.network;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mcclient.model.RoomInfo;
import com.mcclient.util.JsonTemplates;
import com.mcclient.util.JsonUtils;
import com.mcclient.view.MainFrame;
import com.mcclient.view.dialogs.CardUseDialog;
import com.mcclient.view.dialogs.MatchCancelledDialog;
import com.mcclient.view.dialogs.MatchResultDialog;

public final class ServerMessageHandler {
    private ServerMessageHandler() { }

    public static void handle(MainFrame owner, String type, JsonNode m) {
        switch (type) {
            case "DISCONNECTED" -> {
                if (owner.loginButton != null) owner.loginButton.setEnabled(true);
                if (owner.registerButton != null) owner.registerButton.setEnabled(true);
                owner.popup("Mất kết nối tới Server.");
            }
            case "ERROR" -> {
                if (owner.loginButton != null) owner.loginButton.setEnabled(true);
                if (owner.registerButton != null) owner.registerButton.setEnabled(true);
                owner.popup(JsonUtils.getString(m, "message"));
            }
            case "PASSWORD_CHANGED" -> owner.popup("Đổi mật khẩu thành công.");

            // ==================== SOLO ====================
            case "SOLO_BEST" -> {
                int bestLevel = JsonUtils.getInt(m, "level", 0);
                long bestTime = JsonUtils.getLong(m, "timeMs", -1L);
                if (owner.soloPanel != null) {
                    owner.soloPanel.updateBestRecord(bestLevel, bestTime);
                }
            }
            case "SOLO_REWARD_OK" -> {
                owner.coins = JsonUtils.getInt(m, "coins", owner.coins);
                if (owner.coinLabel != null) owner.coinLabel.setText("Xu: " + owner.coins);
                if (owner.soloPanel != null) owner.soloPanel.onRewardGranted();
            }

            // ==================== AUTH & LOBBY ====================
            case "LOGIN_OK", "LOGIN" -> {
                if (owner.loginButton != null) owner.loginButton.setEnabled(true);
                if (owner.registerButton != null) owner.registerButton.setEnabled(true);
                owner.loggedIn = true;
                owner.username = JsonUtils.getString(m, "username");
                owner.coins = JsonUtils.getInt(m, "coins", 50);
                owner.rankingPoints = JsonUtils.getInt(m, "points", 0);
                if (owner.coinLabel != null) owner.coinLabel.setText("Xu: " + owner.coins);
                owner.showPage("lobby");
                owner.send("LIST_ROOMS", JsonTemplates.empty());
            }
            case "ROOMS", "LIST_ROOMS" -> {
                owner.roomModel.clear();
                owner.availableRooms.clear();
                JsonNode a = m.path("rooms");
                if (a.isArray()) {
                    for (JsonNode x : a) {
                        RoomInfo room = new RoomInfo(
                                JsonUtils.getString(x, "roomId"),
                                JsonUtils.getString(x, "host"),
                                JsonUtils.getInt(x, "players", 1)
                        );
                        owner.availableRooms.add(room);
                        owner.roomModel.addElement(room.displayText());
                    }
                }
            }
            case "LOBBY_UPDATE" -> {
                if (owner.loggedIn) owner.send("LIST_ROOMS", JsonTemplates.empty());
            }
            case "ROOM_JOINED", "CREATE_ROOM", "JOIN_ROOM" -> {
                owner.roomId = JsonUtils.getString(m, "roomId");
                owner.roomLabel.setText("Mã phòng: " + owner.roomId);
                owner.readyButton.setEnabled(true);
                owner.readyButton.setText("SẴN SÀNG");
                owner.waitingPlayersLabel.setText("Đang chờ người chơi tham gia...");
                owner.showPage("waiting");
            }
            case "ROOM_STATE" -> {
                String p1 = JsonUtils.getString(m, "player1");
                String p2 = JsonUtils.getString(m, "player2");
                boolean r1 = JsonUtils.getBoolean(m, "ready1", false);
                boolean r2 = JsonUtils.getBoolean(m, "ready2", false);
                owner.waitingPlayersLabel.setText(
                        (p1.isBlank() ? "Đang chờ" : p1) + (r1 ? " ✓" : "")
                        + "     |     "
                        + (p2.isBlank() ? "Đang chờ đối thủ" : p2) + (r2 ? " ✓" : "")
                );
                boolean mine = owner.username.equals(p1) ? r1 : owner.username.equals(p2) && r2;
                owner.readyButton.setText(mine ? "BỎ SẴN SÀNG" : "SẴN SÀNG");
                boolean both = r1 && r2;
                owner.readyButton.setEnabled(!both);
                if (both) owner.readyButton.setText("CẢ HAI ĐÃ SẴN SÀNG");
            }
            case "ROOM_CANCELLED", "ROOM_LEFT" -> {
                owner.coins = JsonUtils.getInt(m, "coins", owner.coins);
                if (owner.coinLabel != null) owner.coinLabel.setText("Xu: " + owner.coins);
                String reason = JsonUtils.getString(m, "reason");
                if (!reason.isBlank()) owner.popup(reason);
                owner.roomId = "";
                owner.showPage("lobby");
                owner.send("LIST_ROOMS", JsonTemplates.empty());
            }

            // ==================== PRE-MATCH (RPS & CARD OFFER) ====================
            case "RPS_START" -> owner.showRpsChoiceDialog(false);
            case "RPS_WAIT" -> owner.pvpPanel.setStatusText("Đang chờ đối thủ chọn oẳn tù tì...");
            case "RPS_TIE" -> owner.showRpsChoiceDialog(true);
            case "RPS_RESULT" -> {
                if (JsonUtils.getBoolean(m, "youWin", false)) owner.showOrderChoiceDialog();
                else owner.pvpPanel.setStatusText("Đối thủ thắng oẳn tù tì và đang chọn thứ tự đánh trước/sau.");
            }
            case "ORDER_SELECTED" -> { }
            case "CARD_OFFERS" -> {
                if (m.path("cards").isArray()) {
                    owner.showCardPurchaseDialog((ArrayNode) m.path("cards"));
                }
            }
            case "BUY_CONFIRMED" -> {
                owner.coins = JsonUtils.getInt(m, "coins", owner.coins);
                if (owner.coinLabel != null) owner.coinLabel.setText("Xu: " + owner.coins);
            }

            // ==================== PVP GAMEPLAY ====================
            case "GAME_START" -> owner.pvpPanel.onGameStart(JsonUtils.getString(m, "currentPlayer"));
            case "PRIVATE_CARDS" -> {
                if (m.path("cards").isArray()) {
                    owner.pvpPanel.refreshPrivateCards((ArrayNode) m.path("cards"));
                }
            }
            case "CARD_FLIP" -> owner.pvpPanel.onCardFlip(
                    JsonUtils.getInt(m, "index", -1),
                    JsonUtils.getInt(m, "value", -1),
                    JsonUtils.getString(m, "by")
            );
            case "ROUND_RESULT" -> owner.pvpPanel.onRoundResult(m);
            case "BOARD_RESET" -> owner.pvpPanel.onBoardReset();
            case "TURN_CHANGED" -> owner.pvpPanel.onTurnChanged(m);
            case "CARD_PROMPT" -> {
                ArrayNode eligible = m.path("eligible").isArray() ? (ArrayNode) m.path("eligible") : null;
                owner.pvpPanel.onCardPrompt(eligible, JsonUtils.getString(m, "phase"));
            }
            case "CARD_USED" -> owner.pvpPanel.onCardUsed(
                    JsonUtils.getString(m, "player"),
                    JsonUtils.getString(m, "code"),
                    JsonUtils.getString(m, "name")
            );
            case "CARD_EFFECT" -> owner.pvpPanel.appendLog(JsonUtils.getString(m, "message"));
            case "SHIELD_PROMPT" -> {
                boolean use = CardUseDialog.confirmShield(owner.frame, JsonUtils.getString(m, "code"));
                owner.send("SHIELD_RESPONSE", JsonTemplates.shieldResponse(use));
            }
            case "SHIELD_BLOCKED" -> owner.pvpPanel.appendLog(
                    JsonUtils.getString(m, "player") + " đã dùng Khiên bảo vệ và chặn đòn tấn công."
            );
            case "PRIVATE_REVEAL" -> owner.pvpPanel.onPrivateReveal(
                    JsonUtils.getInt(m, "index", -1),
                    JsonUtils.getInt(m, "value", -1)
            );
            case "CARD_FAILED" -> owner.pvpPanel.appendLog("Ngôi sao may mắn không thành công.");
            case "BONUS_ATTEMPT" -> owner.pvpPanel.appendLog("Tăng tốc: người chơi được thêm một lần thử.");
            case "LUCKY_PAIR" -> owner.pvpPanel.onLuckyPair(
                    JsonUtils.getString(m, "player"),
                    JsonUtils.getInt(m, "indexA", -1),
                    JsonUtils.getInt(m, "indexB", -1)
            );
            case "BOARD_SHUFFLED" -> owner.pvpPanel.appendLog("Ảo ảnh đã đổi vị trí hai lá bài.");
            case "TURN_SKIPPED" -> owner.pvpPanel.appendLog(JsonUtils.getString(m, "player") + " bị mất lượt.");

            // ==================== END MATCH & DATA PAGES ====================
            case "GAME_OVER" -> {
                int s1 = JsonUtils.getInt(m, "score1", 0);
                int s2 = JsonUtils.getInt(m, "score2", 0);
                String winner = JsonUtils.getString(m, "winner");
                String p1 = JsonUtils.getString(m, "player1");
                boolean isP1 = owner.username.equals(p1);

                int myCoin = isP1 ? JsonUtils.getInt(m, "coin1", 0) : JsonUtils.getInt(m, "coin2", 0);
                int myRank = isP1 ? JsonUtils.getInt(m, "rank1", 0) : JsonUtils.getInt(m, "rank2", 0);
                int netCoin = isP1 ? JsonUtils.getInt(m, "netCoin1", myCoin) : JsonUtils.getInt(m, "netCoin2", myCoin);

                owner.coins = Math.max(0, owner.coins + myCoin);
                owner.rankingPoints += myRank;
                if (owner.coinLabel != null) owner.coinLabel.setText("Xu: " + owner.coins);

                String result = winner.equals("DRAW")
                        ? "Hai người hòa nhau!"
                        : winner.equals(owner.username) ? "Chúc mừng! Bạn đã chiến thắng!" : "Bạn đã thua. Chơi lại nhé!";
                int myS = isP1 ? s1 : s2;
                int oppS = isP1 ? s2 : s1;

                MatchResultDialog.show(owner.frame, result, myS, oppS, netCoin, myRank);
                owner.roomId = "";
                owner.showPage("lobby");
                owner.send("LIST_ROOMS", JsonTemplates.empty());
            }
            case "MATCH_CANCELLED" -> {
                String p1 = JsonUtils.getString(m, "player1");
                int myCoin = owner.username.equals(p1) ? JsonUtils.getInt(m, "coin1", 0) : JsonUtils.getInt(m, "coin2", 0);
                owner.coins = Math.max(0, owner.coins + myCoin);
                if (owner.coinLabel != null) owner.coinLabel.setText("Xu: " + owner.coins);

                MatchCancelledDialog.show(owner.frame, JsonUtils.getString(m, "reason"), myCoin);
                owner.roomId = "";
                owner.showPage("lobby");
                owner.send("LIST_ROOMS", JsonTemplates.empty());
            }
            case "HISTORY" -> {
                if (m.path("items").isArray()) {
                    owner.updateDataPage("history", (ArrayNode) m.path("items"),
                            new String[]{"opponent", "score", "result", "coinChange", "ended"});
                }
            }
            case "LEADERBOARD" -> {
                if (m.path("items").isArray()) {
                    owner.updateDataPage("leaderboard", (ArrayNode) m.path("items"),
                            new String[]{"username", "points", "wins", "winRate"});
                }
            }
            case "SOLO_LEADERBOARD" -> {
                if (m.path("items").isArray()) {
                    owner.updateDataPage("soloLeaderboard", (ArrayNode) m.path("items"),
                            new String[]{"username", "bestLevel", "achievement", "bestTime"});
                }
            }
            case "CARD_COLLECTION" -> {
                if (m.path("cards").isArray()) {
                    owner.updateCollection((ArrayNode) m.path("cards"));
                }
            }
            case "LOGOUT_OK" -> { }
        }
    }
}