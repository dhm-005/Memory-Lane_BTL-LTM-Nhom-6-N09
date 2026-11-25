package com.cardgame.client.view;


import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import com.cardgame.client.util.Json;
import com.cardgame.client.model.RoomInfo;
import com.cardgame.client.view.components.CardTile;
import com.cardgame.client.view.dialogs.*;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import static com.cardgame.client.view.MainFrame.*;
import static com.cardgame.client.view.UiTheme.*;
final class ServerMessageHandler {
    private ServerMessageHandler() {
    }
    private static String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
    static void handle(MainFrame owner, ObjectNode m) {
        String type = Json.str(m, "type");
        switch (type) {
            case "SESSION_REPLACED" -> owner.handleSessionReplaced(Json.str(m, "message"));
            case "LOGIN_FAILED" -> {
                // Lỗi xác thực có loại phản hồi riêng để luôn hiện rõ trên form đăng nhập.
                if (owner.loginButton != null) owner.loginButton.setEnabled(true);
                if (!"login".equals(owner.currentPage)) owner.showPage("login");
                LoginPanel.showError(Json.str(m, "message"));
            }
            case "ERROR" -> {
                if (owner.soloAwaitingServer) {
                    owner.soloAwaitingServer = false;
                    owner.setupSoloLevel();
                }
                if (owner.loginButton != null) owner.loginButton.setEnabled(true);
                if (owner.registerButton != null) owner.registerButton.setEnabled(true);
                // Lỗi đăng nhập/đăng ký hiện ngay trên form (màu đỏ); các màn khác vẫn dùng hộp thoại
                if ("login".equals(owner.currentPage)) LoginPanel.showError(Json.str(m, "message"));
                else if ("register".equals(owner.currentPage)) RegisterPanel.showError(Json.str(m, "message"));
                else owner.popup(Json.str(m, "message"));
            }
            case "PASSWORD_CHANGED" -> owner.popup("Đổi mật khẩu thành công.");
            case "SOLO_BEST" -> {
                int bestLevel=Json.integer(m,"level",0);
                long bestTime=m.has("timeMs")&&!m.get("timeMs").isNull()?m.get("timeMs").asLong():-1;
                if (owner.soloRecordLabel != null) owner.soloRecordLabel.setText(bestLevel <= 0 ? "Thành tích tốt nhất: chưa có" : (bestTime >= 0 ? "Thành tích tốt nhất: Level " + bestLevel + " · " + owner.formatSoloTime(bestTime) : "Thành tích tốt nhất: đã hoàn thành đến Level " + bestLevel));
            }
            case "SOLO_LEVEL_OK" -> owner.acceptSoloLevel(Json.integer(m,"level",0));
            case "SOLO_REWARD_OK" -> {
                owner.session.coins = Json.integer(m,"coins",owner.session.coins);
                if (owner.coinLabel != null) owner.coinLabel.setText("Xu: " + owner.session.coins);
                owner.soloStatusLabel.setText(owner.soloStatusLabel.getText() + "  (+20 xu đã cộng)");
            }
            case "LOGIN_OK" -> {
                if (owner.loginButton != null) owner.loginButton.setEnabled(true);
                if (owner.registerButton != null) owner.registerButton.setEnabled(true);
                owner.session.loggedIn = true;
                owner.session.username = Json.str(m,"username");
                owner.session.coins = Json.integer(m,"coins",50);
                owner.session.rankingPoints = Json.integer(m,"points",0);
                if (owner.coinLabel != null) owner.coinLabel.setText("Xu: " + owner.session.coins);
                LoginPanel.clearFields();
                RegisterPanel.clearFields();
                owner.showPage("lobby");
                owner.send("LIST_ROOMS");
            }
            case "ROOMS" -> {
                owner.roomModel.clear();
                owner.availableRooms.clear();
                ArrayNode a = m.get("rooms") instanceof ArrayNode array ? array : null;
                if(a!=null) for(JsonNode el:a){
                    ObjectNode x=(ObjectNode) el;
                    RoomInfo room=new RoomInfo(Json.str(x,"roomId"),Json.str(x,"host"),Json.integer(x,"players",1));
                    owner.availableRooms.add(room);
                    owner.roomModel.addElement(room.displayText());
                }
            }
            case "LOBBY_UPDATE" -> {
                if(owner.session.loggedIn && "lobby".equals(owner.currentPage)) owner.send("LIST_ROOMS");
            }
            case "ROOM_JOINED" -> {
                owner.session.roomId=Json.str(m,"roomId");
                owner.roomLabel.setText("Mã phòng: "+owner.session.roomId);
                owner.readyButton.setEnabled(true);
                owner.readyButton.setText("SẴN SÀNG");
                owner.waitingPlayersLabel.setText("Đang chờ người chơi tham gia...");
                owner.showPage("waiting");
            }
            case "ROOM_STATE" -> {
                String p1 = Json.str(m, "player1");
                String p2 = Json.str(m, "player2");

                if (p2.isBlank()) {
                    RpsDialog.closeActive();
                    OrderChoiceDialog.closeActive();
                    CardPurchaseDialog.closeActive();
                }

                boolean r1 = m.has("ready1") && m.get("ready1").asBoolean();
                boolean r2 = m.has("ready2") && m.get("ready2").asBoolean();

                owner.waitingPlayersLabel.setHorizontalAlignment(SwingConstants.CENTER);

                String p1Html = escapeHtml(p1);
                String p2Html = escapeHtml(p2);
                owner.waitingPlayersLabel.setText(
                    "<html><div style='text-align:center;'>"
                    + "<span style='font-family:Segoe UI;'>"
                    + (p1Html.isBlank() ? "Đang chờ" : p1Html)
                    + "</span>"
                    + (r1 ? " <span style='font-family:Segoe UI Symbol;'>✔</span>" : "")
                    + " &nbsp;&nbsp; | &nbsp;&nbsp; "
                    + "<span style='font-family:Segoe UI;'>"
                    + (p2Html.isBlank() ? "Đang chờ đối thủ" : p2Html)
                    + "</span>"
                    + (r2 ? " <span style='font-family:Segoe UI Symbol;'>✔</span>" : "")
                    + "</div></html>"
                );

                owner.waitingPlayersLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

                boolean mine = owner.session.username.equals(p1)
                        ? r1
                        : owner.session.username.equals(p2) && r2;

                owner.readyButton.setText(mine ? "BỎ SẴN SÀNG" : "SẴN SÀNG");

                boolean both = r1 && r2;
                owner.readyButton.setEnabled(!both);

                if (both) {
                    owner.readyButton.setText("CẢ HAI ĐÃ SẴN SÀNG");
                }
            }
            case "ROOM_CANCELLED", "ROOM_LEFT" -> {
                RpsDialog.closeActive();
                OrderChoiceDialog.closeActive();
                CardPurchaseDialog.closeActive();
                CardUseDialog.closeActive();
                owner.session.coins=Json.integer(m,"coins",owner.session.coins);
                if(owner.coinLabel!=null)owner.coinLabel.setText("Xu: "+owner.session.coins);
                String reason=Json.str(m,"reason");
                if(!reason.isBlank())owner.popup(reason);
                owner.session.roomId="";
                owner.showPage("lobby");
                owner.send("LIST_ROOMS");
            }
            case "RPS_START" -> owner.showRpsChoiceDialog(false);
            case "RPS_WAIT" -> owner.gameStatus.setText("Đang chờ đối thủ chọn oẳn tù tì...");
            case "RPS_TIE" -> owner.showRpsChoiceDialog(true);
            case "RPS_RESULT" -> {
                if(m.has("youWin")&&m.get("youWin").asBoolean()) owner.showOrderChoiceDialog();
                else owner.gameStatus.setText("Đối thủ thắng oẳn tù tì và đang chọn thứ tự đánh trước/sau.");
            }
            case "ORDER_SELECTED" -> {
                /* Thứ tự đã chọn; không giữ dòng đánh trước/đánh sau trong màn chơi. */
            }
            case "CARD_OFFERS" -> {
                if (m.get("cards") instanceof ArrayNode cards) owner.showCardPurchaseDialog(cards);
            }
            case "BUY_CONFIRMED", "COINS_UPDATED" -> {
                owner.session.coins=Json.integer(m,"coins",owner.session.coins);
                if(owner.coinLabel!=null)owner.coinLabel.setText("Xu: "+owner.session.coins);
            }
            case "GAME_START" -> {
                Arrays.fill(owner.revealed,false);
                Arrays.fill(owner.matched,false);
                Arrays.fill(owner.locked,false);
                Arrays.fill(owner.privatePeek,false);
                owner.ownedCardCodes.clear();
                owner.availableCardCodes.clear();
                owner.usedCardCodes.clear();
                owner.cardPromptPhase="";
                owner.selectingCardTarget=false;
                owner.pendingCardUseCode="";
                for(int i=0;i<PVP_BOARD_SIZE;i++){
                    CardTile tile=(CardTile)owner.cardButtons[i];
                    tile.setSymbolIndex(-1);
                    tile.setBackground(LAVENDER);
                    tile.setEnabled(true);
                    tile.repaint();
                }
                String p1=Json.str(m,"player1"),p2=Json.str(m,"player2");
                String me=owner.session.username.equals(p1)?p1:p2;
                String opponent=owner.session.username.equals(p1)?p2:p1;
                if(owner.playerNamesLabel!=null)owner.playerNamesLabel.setText("Bạn: "+me+"     |     Đối thủ: "+opponent);
                owner.myTurn=owner.session.username.equals(Json.str(m,"currentPlayer"));
                owner.scoreLabel.setText("0   —   0");
                owner.setTurnIndicator(owner.myTurn);
                owner.showPage("game");
                owner.stopPvpTurnClock();
                owner.matchLogArea.setText("");
                owner.appendLog("Trận đấu bắt đầu. Bàn chơi có 18 cặp.");
                owner.refreshBoardEnabled();
            }
            case "PRIVATE_CARDS" -> {
                if (m.get("cards") instanceof ArrayNode cards) owner.refreshPrivateCards(cards);
            }
            case "CARD_FLIP" -> {
                int i=Json.integer(m,"index",-1),v=Json.integer(m,"value",-1);
                if(i>=0&&i<PVP_BOARD_SIZE){
                    owner.revealed[i]=true;
                    owner.privatePeek[i]=false;
                    ((CardTile)owner.cardButtons[i]).setSymbolIndex(v);
                    owner.cardButtons[i].setBackground(YELLOW);
                    owner.cardButtons[i].setEnabled(false);
                    owner.cardButtons[i].repaint();
                }
                String by=Json.str(m,"by");
                if(!by.isBlank()) {
                    if(by.equals("Ngôi sao may mắn")) owner.appendLog(by+" đã lật một lá bài.", new Color(200, 45, 55));
                    else owner.appendLog(by+" đã lật một lá bài.");
                }
                if(owner.session.username.equals(by)&&owner.cardPromptPhase.equals("TURN")){
                    owner.availableCardCodes.clear();
                    owner.cardPromptPhase="";
                    owner.renderPrivateCards();
                }
                int count=0;
                for(int j=0;j<PVP_BOARD_SIZE;j++)if(owner.revealed[j]&&!owner.privatePeek[j])count++;
                if(count>=2)for(int j=0;j<PVP_BOARD_SIZE;j++)owner.cardButtons[j].setEnabled(false);
            }
            case "ROUND_RESULT" -> {
                int s1=Json.integer(m,"score1",0),s2=Json.integer(m,"score2",0);
                owner.scoreLabel.setText(owner.scoreForMe(s1,s2,m));
                boolean match=m.has("matched")&&m.get("matched").asBoolean();
                int a=Json.integer(m,"indexA",-1),b=Json.integer(m,"indexB",-1);
                if(match){
                    if(a>=0&&a<PVP_BOARD_SIZE){
                        owner.matched[a]=true;
                        owner.revealed[a]=false;
                        owner.cardButtons[a].setBackground(MINT);
                    }
                    if(b>=0&&b<PVP_BOARD_SIZE){
                        owner.matched[b]=true;
                        owner.revealed[b]=false;
                        owner.cardButtons[b].setBackground(MINT);
                    }
                    owner.appendLog(Json.str(m,"player")+" đã ghép đúng một cặp!");
                    owner.refreshBoardEnabled();
                } else owner.appendLog(Json.str(m,"player")+" lật sai. Hai lá sẽ úp lại sau 1,5 giây.");
                owner.myTurn=owner.session.username.equals(Json.str(m,"currentPlayer"));
                owner.setTurnIndicator(owner.myTurn);
            }
            case "BOARD_RESET" -> {
                for(int i=0;i<PVP_BOARD_SIZE;i++)if(!owner.matched[i]){
                    owner.revealed[i]=false;
                    owner.privatePeek[i]=false;
                    ((CardTile)owner.cardButtons[i]).setSymbolIndex(-1);
                    owner.cardButtons[i].setBackground(owner.locked[i]?PINK:LAVENDER);
                }
                owner.refreshBoardEnabled();
            }
            case "TURN_CHANGED" -> {
                owner.startPvpTurnClock(Json.integer(m,"turnSeconds",60));
                owner.myTurn=owner.session.username.equals(Json.str(m,"currentPlayer"));
                boolean keepStartOptions=owner.myTurn&&owner.cardPromptPhase.equals("TURN")&&!owner.availableCardCodes.isEmpty();
                if(!keepStartOptions){
                    owner.availableCardCodes.clear();
                    owner.cardPromptPhase="";
                }
                owner.selectingCardTarget=false;
                owner.pendingCardUseCode="";
                owner.renderPrivateCards();
                owner.scoreLabel.setText(owner.scoreForMe(Json.integer(m,"score1",0),Json.integer(m,"score2",0),m));
                Arrays.fill(owner.locked,false);
                int li=Json.integer(m,"lockedIndex",-1);
                if(li>=0&&li<PVP_BOARD_SIZE)owner.locked[li]=true;
                for(int i=0;i<PVP_BOARD_SIZE;i++)if(!owner.matched[i]&&!owner.revealed[i])owner.cardButtons[i].setBackground(owner.locked[i]?PINK:LAVENDER);
                owner.setTurnIndicator(owner.myTurn);
                owner.refreshBoardEnabled();
            }
            case "CARD_PROMPT" -> {
                owner.availableCardCodes.clear();
                ArrayNode eligible = m.get("eligible") instanceof ArrayNode array ? array : null;
                if(eligible!=null)for(JsonNode e:eligible)owner.availableCardCodes.add(e.asText());
                owner.cardPromptPhase=Json.str(m,"phase");
                owner.myTurn=true;
                owner.renderPrivateCards();
                owner.refreshBoardEnabled();
                owner.setTurnIndicator(true);
            }
            case "CARD_USED" -> {
                String player=Json.str(m,"player"),code=Json.str(m,"code");
                owner.appendLog(player+" đã sử dụng thẻ "+Json.str(m,"name")+".", new Color(200, 45, 55));
                if(player.equals(owner.session.username)){
                    owner.usedCardCodes.add(code);
                    owner.availableCardCodes.remove(code);
                    owner.renderPrivateCards();
                }
            }
            case "CARD_EFFECT" -> owner.appendLog(Json.str(m,"message"), new Color(200, 45, 55));
            case "SHIELD_PROMPT" -> owner.send("SHIELD_RESPONSE", "use", CardUseDialog.confirmShield(owner.frame, Json.str(m, "code")));
            case "SHIELD_BLOCKED" -> owner.appendLog(Json.str(m,"player")+" đã dùng Khiên bảo vệ và chặn đòn tấn công.", new Color(200, 45, 55));
            case "PRIVATE_REVEAL" -> {
                int i=Json.integer(m,"index",-1),v=Json.integer(m,"value",-1);
                if(i>=0&&i<PVP_BOARD_SIZE){
                    owner.revealed[i]=true;
                    owner.privatePeek[i]=true;
                    ((CardTile)owner.cardButtons[i]).setSymbolIndex(v);
                    owner.cardButtons[i].setBackground(new Color(220,245,232));
                    javax.swing.Timer hideTimer=new javax.swing.Timer(2000,e->{
                        owner.revealed[i]=false;owner.privatePeek[i]=false;((CardTile)owner.cardButtons[i]).setSymbolIndex(-1);owner.cardButtons[i].setBackground(owner.locked[i]?PINK:LAVENDER);owner.refreshBoardEnabled();
                    }
                    );
                    hideTimer.setRepeats(false);
                    hideTimer.start();
                }
            }
            case "CARD_FAILED" -> owner.appendLog("Ngôi sao may mắn không thành công.", new Color(200, 45, 55));
            case "BONUS_ATTEMPT" -> owner.appendLog("Tăng tốc: người chơi được thêm một lần thử.", new Color(200, 45, 55));
            case "LUCKY_PAIR" -> {
                int a=Json.integer(m,"indexA",-1),b=Json.integer(m,"indexB",-1);
                for(int i:new int[]{
                    a,b
                }
                )if(i>=0&&i<PVP_BOARD_SIZE){
                    owner.matched[i]=true;
                    owner.revealed[i]=false;
                    owner.cardButtons[i].setBackground(MINT);
                    owner.cardButtons[i].repaint();
                }
                owner.appendLog(Json.str(m,"player")+" đã được Ngôi sao may mắn ghép một cặp!", new Color(200, 45, 55));
                owner.refreshBoardEnabled();
            }
            case "BOARD_SHUFFLED" -> owner.appendLog("Ảo ảnh đã đổi vị trí hai lá bài.", new Color(200, 45, 55));
            case "TURN_SKIPPED" -> owner.appendLog(Json.str(m,"player")+" bị mất lượt.", new Color(200, 45, 55));
            case "TURN_TIMEOUT" -> owner.appendLog(Json.str(m,"player")+" hết 60 giây, lượt được tự động chuyển.");
            case "GAME_OVER" -> {
                owner.stopPvpTurnClock();
                int s1=Json.integer(m,"score1",0),s2=Json.integer(m,"score2",0);
                String winner=Json.str(m,"winner");
                int myCoin=owner.session.username.equals(Json.str(m,"player1"))?Json.integer(m,"coin1",0):Json.integer(m,"coin2",0);
                int myRank=owner.session.username.equals(Json.str(m,"player1"))?Json.integer(m,"rank1",0):Json.integer(m,"rank2",0);
                int purchaseCoin=owner.session.username.equals(Json.str(m,"player1"))?Json.integer(m,"purchaseCoin1",0):Json.integer(m,"purchaseCoin2",0);
                int finishCoin=owner.session.username.equals(Json.str(m,"player1"))?Json.integer(m,"finishCoin1",myCoin):Json.integer(m,"finishCoin2",myCoin);
                int exactCoins=owner.session.username.equals(Json.str(m,"player1"))?Json.integer(m,"coins1",-1):Json.integer(m,"coins2",-1);
                owner.session.coins=exactCoins>=0?exactCoins:Math.max(0,owner.session.coins+myCoin);
                owner.session.rankingPoints+=myRank;
                if(owner.coinLabel!=null)owner.coinLabel.setText("Xu: "+owner.session.coins);
                String result=winner.equals("DRAW")?"Hai người hòa nhau!":winner.equals(owner.session.username)?"Chúc mừng! Bạn đã chiến thắng!":"Bạn đã thua. Chơi lại nhé!";
                int myS=owner.session.username.equals(Json.str(m,"player1"))?s1:s2, oppS=owner.session.username.equals(Json.str(m,"player1"))?s2:s1;
                MatchResultDialog.show(owner.frame, result, myS, oppS, purchaseCoin, finishCoin, myRank);
                owner.session.roomId="";
                owner.showPage("lobby");
                owner.send("LIST_ROOMS");
            }
            case "MATCH_CANCELLED" -> {
                CardUseDialog.closeActive();
                owner.stopPvpTurnClock();
                int myCoin=owner.session.username.equals(Json.str(m,"player1"))?Json.integer(m,"coin1",0):Json.integer(m,"coin2",0);
                int exactCoins=owner.session.username.equals(Json.str(m,"player1"))?Json.integer(m,"coins1",-1):Json.integer(m,"coins2",-1);
                owner.session.coins=exactCoins>=0?exactCoins:Math.max(0,owner.session.coins+myCoin);
                if(owner.coinLabel!=null)owner.coinLabel.setText("Xu: "+owner.session.coins);
                boolean isPlayer1 = owner.session.username.equals(Json.str(m, "player1"));
                boolean isQuitter = owner.session.username.equals(Json.str(m, "quitter"));

                int purchaseCoin = isQuitter
                        ? (isPlayer1
                            ? Json.integer(m, "purchaseCoin1", 0)
                            : Json.integer(m, "purchaseCoin2", 0))
                        : 0;

                // Trận bị hủy nên không có xu hoàn thành
                int finishCoin = isQuitter ? -10 : 0;


                MatchCancelledDialog.show(owner.frame, Json.str(m,"reason"), purchaseCoin, finishCoin);
                owner.session.roomId="";
                owner.showPage("lobby");
                owner.send("LIST_ROOMS");
            }
            case "HISTORY" -> {
                if (m.get("items") instanceof ArrayNode items) {
                    owner.updateDataPage("history", items, new String[]{
                        "opponent","score","result","coinChange","ended"
                    });
                }
            }
            case "LEADERBOARD" -> {
                if (m.get("items") instanceof ArrayNode items) {
                    owner.updateDataPage("leaderboard", items, new String[]{
                        "username","points","wins","winRate"
                    });
                }
            }
            case "SOLO_LEADERBOARD" -> {
                if (m.get("items") instanceof ArrayNode items) {
                    owner.updateDataPage("soloLeaderboard", items, new String[]{
                        "username","bestLevel","achievement","bestTime"
                    });
                }
            }
            case "CARD_COLLECTION" -> {
                if (m.get("cards") instanceof ArrayNode cards) owner.updateCollection(cards);
            }
            case "LOGOUT_OK" -> {
            }
        }
    }
}
