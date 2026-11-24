package com.cardgame.server.model;


import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import com.cardgame.server.network.ClientHandler;
import com.cardgame.server.util.Json;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.security.SecureRandom;
import static com.cardgame.server.MainServer.BOARD_SIZE;
import static com.cardgame.server.MainServer.PAIR_COUNT;
import static com.cardgame.server.MainServer.adjustCoins;
import static com.cardgame.server.MainServer.balance;
import static com.cardgame.server.MainServer.error;
import com.cardgame.server.service.RoomRegistry;
import com.cardgame.server.service.RoomService;
import com.cardgame.server.service.GameService;
import com.cardgame.server.repository.GameRepository;
import com.cardgame.server.repository.UserRepository;
import com.cardgame.server.model.GameResult;
import static com.cardgame.server.service.CardService.CARDS;
import static com.cardgame.server.service.CardService.CARD_CODES;
import static com.cardgame.server.service.CardService.toJson;
public final class Room {
    public final Match state = new Match(BOARD_SIZE);
    public final String id;
    public final List<ClientHandler> players=new ArrayList<>(2);
    public final boolean[] ready={
        false,false
    };
    public final String[] rps={
        null,null
    };
    public final List<String>[] offers=new List[]{
        new ArrayList<>(),new ArrayList<>()
    };
    public final List<String>[] selected=new List[]{
        new ArrayList<>(),new ArrayList<>()
    };
    public final List<String>[] owned=new List[]{
        new ArrayList<>(),new ArrayList<>()
    };
    public final Set<String>[] used=new Set[]{
        new HashSet<>(),new HashSet<>()
    };
    public final int[] coinsChange={
        0,0
    };
    public final int[] rankChange={
        0,0
    };
    public int pendingRpsWinner=-1;
    public boolean started=false,finished=false,cancelled=false,buyConfirmed0=false,buyConfirmed1=false;
    public boolean joinReserved=false;
    public LocalDateTime startedAt;
    private ScheduledFuture<?> purchaseTimeout;
    private ScheduledFuture<?> turnTimeout;
    private long purchaseGeneration;
    private long turnGeneration;
    private long turnDeadlineNanos;
    private boolean purchasesRefunded;
    private boolean rpsPhaseStarted;
    private final int[] playerUserIds = {-1, -1};
    private final String[] playerUsernames = {"", ""};
    private static final int TURN_SECONDS = 60;
    private static final SecureRandom RANDOM = new SecureRandom();
    public Room(String id){
        this.id=id;
    }
    public int index(ClientHandler h){
        return players.indexOf(h);
    }
    public void broadcast(ObjectNode o){
        for(ClientHandler h:new ArrayList<>(players))h.send(o);
    }
    public void sendTo(int i,ObjectNode o){
        if(i>=0&&i<players.size())players.get(i).send(o);
    }
    public void broadcastLobbyState(){
        ObjectNode o=Json.object("ROOM_STATE");
        o.put("roomId",id);
        for(int i=0;i<2;i++){
            o.put("player"+(i+1),i<players.size()?players.get(i).username:"");
            o.put("ready"+(i+1),ready[i]);
        }
        broadcast(o);
    }
    public synchronized void setReady(ClientHandler h,boolean value){
        int i=index(h);
        if(i<0||started||cancelled||rpsPhaseStarted)return;
        ready[i]=value;
        broadcastLobbyState();
        if(players.size()==2&&ready[0]&&ready[1]){
            if(UserRepository.findCoins(players.get(0).userId)<10 || UserRepository.findCoins(players.get(1).userId)<10){
                ready[0]=ready[1]=false;
                broadcastLobbyState();
                broadcast(error("Mỗi người chơi cần có ít nhất 10 xu để bắt đầu trận 1v1."));
                return;
            }
            rpsPhaseStarted=true;
            ObjectNode o=Json.object("RPS_START");
            broadcast(o);
        }
    }
    public synchronized void cancelWaiting(String reason){
        if(cancelled||started&&state.hasFlipped)return;
        cancelPurchaseTimeout();
        refundPurchases();
        cancelled=true;
        for(ClientHandler h:new ArrayList<>(players)){
            h.room=null;
            ObjectNode o=Json.object("ROOM_CANCELLED");
            o.put("reason",reason);
            o.put("coins",balance(h.userId));
            h.send(o);
        }
        players.clear();
        RoomRegistry.all().remove(id);
        RoomService.broadcastLobby();
    }
    public synchronized void leaveWaiting(ClientHandler leaver){
        if(started){
            leaveByChoice(leaver);
            return;
        }
        int leavingIndex=index(leaver);
        if(leavingIndex<0)return;
        cancelPurchaseTimeout();
        refundPurchases();
        players.remove(leaver);
        leaver.room=null;
        ObjectNode left=Json.object("ROOM_LEFT");
        left.put("reason","Bạn đã rời phòng chờ.");
        left.put("coins",balance(leaver.userId));
        leaver.send(left);
        if(players.isEmpty()){
            RoomRegistry.all().remove(id);
            cancelled=true;
        } else {
            // Keep the room alive for the remaining player; reset all pre-game choices.
            ready[0]=ready[1]=false;
            rpsPhaseStarted=false;
            rps[0]=rps[1]=null;
            pendingRpsWinner=-1;
            selected[0].clear();
            selected[1].clear();
            owned[0].clear();
            owned[1].clear();
            coinsChange[0]=coinsChange[1]=0;
            rankChange[0]=rankChange[1]=0;
            buyConfirmed0=buyConfirmed1=false;
            offers[0].clear();
            offers[1].clear();
            broadcastLobbyState();
        }
        RoomService.broadcastLobby();
    }
    public synchronized void playerDisconnected(ClientHandler h){
        if(finished||cancelled)return;
        if(started){
            cancelMatch(h,"Mất kết nối trong trận.");
        } else {
            leaveWaiting(h);
        }
    }
    public synchronized void leaveByChoice(ClientHandler h){
        if(finished||cancelled)return;
        if(started){
            cancelMatch(h,"Người chơi đã thoát trận.");
        } else{
            refundPurchases();
            cancelWaiting("Người chơi đã rời phòng trước khi trận bắt đầu.");
        }
    }
    private void cancelPurchaseTimeout(){
        purchaseGeneration++;
        if(purchaseTimeout!=null){
            purchaseTimeout.cancel(false);
            purchaseTimeout=null;
        }
    }
    public void refundPurchases(){
        if(state.hasFlipped||purchasesRefunded)return;
        purchasesRefunded=true;
        for(int i=0;i<Math.min(2,players.size());i++){
            if(isConfirmed(i)&&!owned[i].isEmpty()){
                int refund=owned[i].stream().mapToInt(c->CARDS.get(c).price).sum();
                if(refund>0){
                    adjustCoins(players.get(i).userId,refund);
                    ObjectNode update=Json.object("COINS_UPDATED");
                    update.put("coins",balance(players.get(i).userId));
                    players.get(i).send(update);
                }
            }
        }
    }
    public synchronized void rpsChoice(ClientHandler h,String choice){
        int i=index(h);
        if(i<0||!rpsPhaseStarted||!ready[0]||!ready[1]||started||offers[0].size()>0||pendingRpsWinner>=0)return;
        if(!Set.of("ROCK","PAPER","SCISSORS").contains(choice)){
            h.send(error("Lựa chọn không hợp lệ."));
            return;
        }
        if(rps[i]!=null){
            h.send(error("Bạn đã chọn oẳn tù tì rồi."));
            return;
        }
        rps[i]=choice;
        if(rps[0]==null||rps[1]==null){
            ObjectNode o=Json.object("RPS_WAIT");
            sendTo(i,o);
            return;
        }
        if(rps[0].equals(rps[1])){
            rps[0]=rps[1]=null;
            broadcast(Json.object("RPS_TIE"));
            return;
        }
        pendingRpsWinner=state.beats(rps[0],rps[1])?0:1;
        ObjectNode o=Json.object("RPS_RESULT");
        o.put("winner",players.get(pendingRpsWinner).username);
        o.put("youWin",false);
        sendTo(1-pendingRpsWinner,o);
        o.put("youWin",true);
        sendTo(pendingRpsWinner,o);
    }

    public synchronized void chooseOrder(ClientHandler h,String order){
        if(index(h)!=pendingRpsWinner||pendingRpsWinner<0||started)return;
        if(!order.equals("FIRST")&&!order.equals("SECOND")){
            h.send(error("Lựa chọn không hợp lệ."));
            return;
        }
        state.turn=order.equals("FIRST")?pendingRpsWinner:1-pendingRpsWinner;
        pendingRpsWinner=-1;
        ObjectNode picked=Json.object("ORDER_SELECTED");
        picked.put("firstPlayer",players.get(state.turn).username);
        picked.put("secondPlayer",players.get(1-state.turn).username);
        broadcast(picked);
        dealOffers();
    }
    public synchronized void dealOffers(){
        if(started||cancelled||players.size()!=2)return;
        cancelPurchaseTimeout();
        purchasesRefunded=false;
        buyConfirmed0=buyConfirmed1=false;
        for(int i=0;i<2;i++){
            offers[i].clear();
            selected[i].clear();
            owned[i].clear();
            used[i].clear();
            coinsChange[i]=0;
            rankChange[i]=0;
            for(String[] group:CARD_CODES)offers[i].add(group[RANDOM.nextInt(group.length)]);
        }
        long generation=++purchaseGeneration;
        for(int i=0;i<2;i++){
            ObjectNode o=Json.object("CARD_OFFERS");
            ArrayNode a=Json.MAPPER.createArrayNode();
            for(String code:offers[i])a.add(toJson(CARDS.get(code)));
            o.set("cards",a);
            sendTo(i,o);
        }
        purchaseTimeout=GameService.TIMER.schedule(()->{
            synchronized(this){
                if(generation==purchaseGeneration&&!started&&!cancelled&&players.size()==2&&offers[0].size()>0&&offers[1].size()>0){
                    try {
                        autoConfirm(0);
                        autoConfirm(1);
                        completePurchases();
                    } catch (RuntimeException e) {
                        System.err.println("[TIMER] Purchase timeout failed for room " + id + ": " + e.getMessage());
                        cancelWaiting("Không thể hoàn tất bước mua thẻ. Vui lòng tạo phòng mới.");
                    }
                }
            }
        },32,TimeUnit.SECONDS);
    }
    public synchronized void updateBuySelection(ClientHandler h,ArrayNode codes){
        int i=index(h);
        if(i<0||started||offers[i].isEmpty()||isConfirmed(i))return;
        List<String> list=new ArrayList<>();
        if(codes!=null)for(JsonNode e:codes){
            String code=e.asText();
            if(offers[i].contains(code)&&!list.contains(code)&&list.size()<2)list.add(code);
        }
        selected[i]=list;
    }
    public boolean isConfirmed(int i){
        return i==0?buyConfirmed0:buyConfirmed1;
    }
    public synchronized void confirmBuy(ClientHandler h){
        int i=index(h);
        if(i<0||started)return;
        if(isConfirmed(i))return;
        if(!chargeSelection(i)){
            h.send(error("Bạn không đủ xu để mua các thẻ đã chọn."));
            selected[i].clear();
            return;
        }
        setConfirmed(i);
        ObjectNode o=Json.object("BUY_CONFIRMED");
        o.put("coins",balance(players.get(i).userId));
        o.set("owned",strings(owned[i]));
        sendTo(i,o);
        if(buyConfirmed0&&buyConfirmed1)completePurchases();
    }
    public boolean chargeSelection(int i){
        int total=selected[i].stream().mapToInt(c->CARDS.get(c).price).sum();
        if(balance(players.get(i).userId)<total+10)return false;
        if(total>0)adjustCoins(players.get(i).userId,-total);
        coinsChange[i]-=total;
        owned[i].clear();
        owned[i].addAll(selected[i]);
        return true;
    }
    public void setConfirmed(int i){
        if(i==0)buyConfirmed0=true;
        else buyConfirmed1=true;
    }
    public void autoConfirm(int i){
        if(isConfirmed(i))return;
        if(!chargeSelection(i))selected[i].clear();
        setConfirmed(i);
        ObjectNode o=Json.object("BUY_CONFIRMED");
        o.put("coins",balance(players.get(i).userId));
        o.set("owned",strings(owned[i]));
        sendTo(i,o);
    }
    public synchronized void completePurchases(){
        if(started||cancelled||players.size()!=2)return;
        cancelPurchaseTimeout();
        started=true;
        for(int i=0;i<2;i++){
            playerUserIds[i]=players.get(i).userId;
            playerUsernames[i]=players.get(i).username;
        }
        state.turnHasFlipped=false;
        state.startTurnCardWindow=false;
        state.turnCardUsed=false;
        startedAt=LocalDateTime.now();
        List<Integer> symbols=new ArrayList<>();
        for(int i=0;i<32;i++)symbols.add(i);
        Collections.shuffle(symbols,RANDOM);
        List<Integer> deck=new ArrayList<>();
        for(int i=0;i<PAIR_COUNT;i++){
            int symbol=symbols.get(i);
            deck.add(symbol);
            deck.add(symbol);
        }
        Collections.shuffle(deck,RANDOM);
        for(int i=0;i<BOARD_SIZE;i++)state.cards[i]=deck.get(i);
        ObjectNode o=Json.object("GAME_START");
        o.put("player1",players.get(0).username);
        o.put("player2",players.get(1).username);
        o.put("currentPlayer",players.get(state.turn).username);
        o.put("boardSize",BOARD_SIZE);
        o.put("firstPlayer",players.get(state.turn).username);
        o.put("secondPlayer",players.get(1-state.turn).username);
        o.put("coins1",balance(players.get(0).userId));
        o.put("coins2",balance(players.get(1).userId));
        broadcast(o);
        for(int i=0;i<2;i++){
            ObjectNode p=Json.object("PRIVATE_CARDS");
            ArrayNode a=Json.MAPPER.createArrayNode();
            for(String c:owned[i])a.add(toJson(CARDS.get(c)));
            p.set("cards",a);
            sendTo(i,p);
        }
        startTurn();
    }
    ArrayNode strings(List<String> list){
        ArrayNode a=Json.MAPPER.createArrayNode();
        for(String s:list)a.add(s);
        return a;
    }
    public synchronized void flip(ClientHandler h,int index){
        int i=index(h);
        if(i<0||!started||finished||cancelled||i!=state.turn||state.promptPending||state.waitingShield){
            h.send(error("Chưa đến lượt hoặc hiện không thể lật bài."));
            return;
        }
        if(state.opened.size()>=2||index<0||index>=BOARD_SIZE||state.matched[index]||state.opened.contains(index)||index==state.lockedIndex){
            h.send(error("Lá bài không hợp lệ hoặc đang bị khóa."));
            return;
        }
        state.hasFlipped=true;
        state.turnHasFlipped=true;
        state.startTurnCardWindow=false;
        state.opened.add(index);
        ObjectNode reveal=Json.object("CARD_FLIP");
        reveal.put("index",index);
        reveal.put("value",state.cards[index]);
        reveal.put("by",players.get(i).username);
        broadcast(reveal);
        if(state.opened.size()==2){
            int a=state.opened.get(0),b=state.opened.get(1);
            boolean match=state.cards[a]==state.cards[b];
            if(match){
                state.matched[a]=state.matched[b]=true;
                state.scores[i]++;
                state.matchedPairs++;
                if(state.bonusAttempt)state.bonusAttempt=false;
                if(i==0)state.curse0=false;
                else state.curse1=false;
            }
            ObjectNode result=Json.object("ROUND_RESULT");
            result.put("matched",match);
            result.put("score1",state.scores[0]);
            result.put("score2",state.scores[1]);
            result.put("indexA",a);
            result.put("indexB",b);
            result.put("player",players.get(i).username);
            result.put("currentPlayer",players.get(state.turn).username);
            result.put("player1",players.get(0).username);
            result.put("player2",players.get(1).username);
            broadcast(result);
            if(state.matchedPairs==PAIR_COUNT){
                finish();
                return;
            }
            if(match){
                state.opened.clear();
                startTurn();
            } else{
                GameService.TIMER.schedule(()->{
                    synchronized(Room.this){
                        if(finished||cancelled||players.size()!=2||state.opened.size()!=2)return;state.opened.clear();ObjectNode reset=Json.object("BOARD_RESET");reset.put("currentPlayer",players.get(state.turn).username);broadcast(reset);endTurnAfterMiss();
                    }
                }
                ,1500,TimeUnit.MILLISECONDS);
            }
        }
    }
    public void startTurn(){
        if(finished||cancelled)return;
        if(state.lockedIndex>=0&&state.lockedIndex<BOARD_SIZE&&state.matched[state.lockedIndex]){
            state.lockedIndex=-1;
            state.lockedPlayer=-1;
        }
        state.promptPending=false;
        state.startTurnCardWindow=!state.turnHasFlipped&&!state.turnCardUsed;
        if(state.startTurnCardWindow){
            List<String> available=new ArrayList<>();
            for(String c:owned[state.turn])if(!used[state.turn].contains(c)&&Set.of("A04","S01","S04").contains(c))available.add(c);
            if(!available.isEmpty())sendTo(state.turn,cardPrompt("TURN",available));
            else state.startTurnCardWindow=false;
        }
        scheduleTurnTimeout();
        notifyTurn();
    }
    public void notifyTurn(){
        ObjectNode o=Json.object("TURN_CHANGED");
        o.put("currentPlayer",players.get(state.turn).username);
        o.put("score1",state.scores[0]);
        o.put("score2",state.scores[1]);
        o.put("lockedIndex",state.lockedIndex);
        long remainingNanos = turnDeadlineNanos - System.nanoTime();
        int remainingSeconds = turnDeadlineNanos <= 0 ? TURN_SECONDS
                : (int)Math.max(0, (remainingNanos + 999_999_999L) / 1_000_000_000L);
        o.put("turnSeconds",remainingSeconds);
        o.put("player1",players.get(0).username);
        o.put("player2",players.get(1).username);
        broadcast(o);
    }
    public ObjectNode cardPrompt(String phase,List<String> codes){
        ObjectNode o=Json.object("CARD_PROMPT");
        o.put("phase",phase);
        o.set("eligible",strings(codes));
        return o;
    }
    public void prompt(int player,String type,List<String> codes){
        state.promptPending=true;
        state.promptType=type;
        sendTo(player,cardPrompt(type,codes));
    }
    public synchronized void skipCard(ClientHandler h){
        int i=index(h);
        if(i!=state.turn||!state.promptPending)return;
        boolean end=state.promptType.equals("END_TURN");
        state.promptPending=false;
        if(end){
            applyCurseSkip(i);
            passTurn();
        } else notifyTurn();
    }
    public synchronized void useCard(ClientHandler h,String code,int targetIndex){
        int i=index(h);
        boolean endPrompt=state.promptPending&&state.promptType.equals("END_TURN");
        boolean startPrompt=state.startTurnCardWindow&&!state.turnHasFlipped&&!state.turnCardUsed;
        if(i!=state.turn||(!endPrompt&&!startPrompt)||!owned[i].contains(code)||used[i].contains(code)
        ||(startPrompt&&!Set.of("A04","S01","S04").contains(code))
        ||(endPrompt&&!Set.of("A01","A02","A03","A04","S03").contains(code))){
            h.send(error("Không thể sử dụng thẻ này."));
            return;
        }
        Card card=CARDS.get(code);
        if(card==null||card.category.equals("LUCK")){
            h.send(error("Thẻ không hợp lệ."));
            return;
        }
        if(code.equals("A02")&&(state.matchedPairs>=PAIR_COUNT-1||targetIndex<0||targetIndex>=BOARD_SIZE||state.matched[targetIndex]||state.opened.contains(targetIndex)||targetIndex==state.lockedIndex)){
            h.send(error(state.matchedPairs>=PAIR_COUNT-1
                ? "Không thể khóa bài khi chỉ còn một cặp cuối cùng."
                : "Hãy chọn một lá úp chưa ghép để khóa."));
            return;
        }
        if(code.equals("S01")&&(targetIndex<0||targetIndex>=BOARD_SIZE||state.matched[targetIndex]||state.opened.contains(targetIndex)||targetIndex==state.lockedIndex)){
            h.send(error("Hãy chọn một lá úp để xem."));
            return;
        }
        String usedPhase=endPrompt?"END_TURN":"TURN";
        state.promptPending=false;
        state.startTurnCardWindow=false;
        if(usedPhase.equals("END_TURN")&&!code.equals("S03"))applyCurseSkip(i);
        used[i].add(code);
        state.turnCardUsed=true;
        ObjectNode notice=Json.object("CARD_USED");
        notice.put("player",players.get(i).username);
        notice.put("code",code);
        notice.put("name",card.name);
        broadcast(notice);
        switch(code){
            case "A01","A02","A03" -> attack(i,code,targetIndex);
            case "A04" -> {
                shuffleHidden();
                if(usedPhase.equals("END_TURN"))passTurn();
                else notifyTurn();
            }
            case "S01" -> {
                ObjectNode p=Json.object("PRIVATE_REVEAL");
                p.put("index",targetIndex);
                p.put("value",state.cards[targetIndex]);
                sendTo(i,p);
                notifyTurn();
            }
            case "S03" -> {
                state.bonusAttempt=true;
                broadcast(Json.object("BONUS_ATTEMPT"));
                notifyTurn();
            }
            case "S04" -> {
                if(RANDOM.nextInt(100)<75){
                    int[] pair=state.randomPair(BOARD_SIZE);
                    if(pair!=null){
                        for(int x:pair){
                            state.matched[x]=true;
                            ObjectNode f=Json.object("CARD_FLIP");
                            f.put("index",x);
                            f.put("value",state.cards[x]);
                            f.put("by","Ngôi sao may mắn");
                            broadcast(f);
                        }
                        state.scores[i]++;
                        state.matchedPairs++;
                        if(i==0)state.curse0=false;
                        else state.curse1=false;
                        ObjectNode s=Json.object("LUCKY_PAIR");
                        s.put("player",players.get(i).username);
                        s.put("indexA",pair[0]);
                        s.put("indexB",pair[1]);
                        broadcast(s);
                        if(state.matchedPairs==PAIR_COUNT){
                            finish();
                            return;
                        }
                    } else broadcast(Json.object("CARD_FAILED"));
                } else broadcast(Json.object("CARD_FAILED"));
                notifyTurn();
            }
            default -> notifyTurn();
        }
    }
    public void attack(int attacker,String code,int targetIndex){
        int target=1-attacker;
        if(owned[target].contains("S02")&&!used[target].contains("S02")){
            state.waitingShield=true;
            state.pendingAttack=code;
            state.pendingAttacker=attacker;
            state.pendingAttackIndex=targetIndex;
            ObjectNode ask=Json.object("SHIELD_PROMPT");
            ask.put("attacker",players.get(attacker).username);
            ask.put("code",code);
            sendTo(target,ask);
        } else applyAttack(attacker,code,targetIndex);
    }
    public synchronized void shieldResponse(ClientHandler h,boolean use){
        int i=index(h);
        if(!state.waitingShield||i<0||i==state.pendingAttacker)return;
        String code=state.pendingAttack;
        int attacker=state.pendingAttacker;
        int targetIndex=state.pendingAttackIndex;
        state.waitingShield=false;
        state.pendingAttack="";
        state.pendingAttacker=-1;
        state.pendingAttackIndex=-1;
        if(use&&owned[i].contains("S02")&&!used[i].contains("S02")){
            used[i].add("S02");
            ObjectNode usedNotice=Json.object("CARD_USED");
            usedNotice.put("player",players.get(i).username);
            usedNotice.put("code","S02");
            usedNotice.put("name",CARDS.get("S02").name);
            broadcast(usedNotice);
            ObjectNode o=Json.object("SHIELD_BLOCKED");
            o.put("player",players.get(i).username);
            broadcast(o);
            passTurn();
        } else applyAttack(attacker,code,targetIndex);
    }
    public void applyAttack(int attacker,String code,int targetIndex){
        int target=1-attacker;
        ObjectNode o=Json.object("CARD_EFFECT");
        o.put("code",code);
        o.put("target",players.get(target).username);
        switch(code){
            case "A01" -> {
                if(state.lockedPlayer==attacker){
                    state.lockedIndex=-1;
                    state.lockedPlayer=-1;
                }
                state.turn=attacker;
                state.turnCardUsed=false;
                state.turnHasFlipped=false;
                state.startTurnCardWindow=false;
                o.put("message",players.get(target).username+" bị đóng băng và mất lượt.");
            }
            case "A02" -> {
                state.lockedIndex=targetIndex;
                state.lockedPlayer=target;
                o.put("index",targetIndex);
                o.put("message",players.get(target).username+" bị khóa một lá bài trong lượt kế tiếp.");
            }
            case "A03" -> {
                if(target==0)state.curse0=true;
                else state.curse1=true;
                o.put("message",players.get(target).username+" bị nguyền trong lượt kế tiếp.");
            }
        }
        broadcast(o);
        if(code.equals("A01")){
            startTurn();
        } else if(code.equals("A02")||code.equals("A03")){
            passTurn();
        }
    }
    public void shuffleHidden(){
        List<Integer> hidden=new ArrayList<>();
        for(int i=0;i<BOARD_SIZE;i++)if(!state.matched[i]&&!state.opened.contains(i))hidden.add(i);
        if(hidden.size()<2)return;
        Collections.shuffle(hidden,RANDOM);
        int a=hidden.get(0),b=hidden.get(1),t=state.cards[a];
        state.cards[a]=state.cards[b];
        state.cards[b]=t;
        broadcast(Json.object("BOARD_SHUFFLED"));
    }

    public void endTurnAfterMiss(){
        int current=state.turn;
        if(state.bonusAttempt){
            state.bonusAttempt=false;
            applyCurseSkip(current);
            passTurn();
            return;
        }
        List<String> eligible=new ArrayList<>();
        if(!state.turnCardUsed)for(String c:owned[current])if(!used[current].contains(c)&&Set.of("A01","A02","A03","A04","S03").contains(c))eligible.add(c);
        if(!eligible.isEmpty())prompt(current,"END_TURN",eligible);
        else{
            applyCurseSkip(current);
            passTurn();
        }
    }
    public void applyCurseSkip(int player){
        if(player==0&&state.curse0){
            state.curse0=false;
            state.skipNext0=true;
        } else if(player==1&&state.curse1){
            state.curse1=false;
            state.skipNext1=true;
        }
    }
    public void passTurn(){
        int old=state.turn;
        if(state.bonusAttempt){
            state.bonusAttempt=false;
        }
        if(state.lockedPlayer==old){
            state.lockedIndex=-1;
            state.lockedPlayer=-1;
        }
        state.turn=1-state.turn;
        state.turnHasFlipped=false;
        state.startTurnCardWindow=false;
        boolean skip=state.turn==0?state.skipNext0:state.skipNext1;
        if(skip){
            if(state.turn==0)state.skipNext0=false;
            else state.skipNext1=false;
            state.turn=old;
            ObjectNode o=Json.object("TURN_SKIPPED");
            o.put("player",players.get(1-old).username);
            broadcast(o);
        }
        state.turnCardUsed=false;
        startTurn();
    }

    public synchronized void finish(){
        if(finished||players.size()!=2)return;
        cancelTurnTimeout();
        int winner=state.scores[0]==state.scores[1]?-1:(state.scores[0]>state.scores[1]?0:1);
        int[] requestedCoins={0,0};
        int[] points={0,0};
        if(winner<0){
            requestedCoins[0]=requestedCoins[1]=10;
        }else{
            int loser=1-winner;
            requestedCoins[winner]=15;
            requestedCoins[loser]=-10;
            String luck=owned[winner].stream().filter(c->c.startsWith("L")).findFirst().orElse("");
            if(luck.equals("L01"))requestedCoins[winner]+=10;
            else if(luck.equals("L02")&&RANDOM.nextBoolean())requestedCoins[winner]+=20;
            else if(luck.equals("L03"))requestedCoins[winner]+=15;
            String loserLuck=owned[loser].stream().filter(c->c.startsWith("L")).findFirst().orElse("");
            if(loserLuck.equals("L04")&&RANDOM.nextBoolean())requestedCoins[loser]=0;
            points[winner]=5;
        }
        int[] purchaseChanges={coinsChange[0],coinsChange[1]};
        GameRepository.Settlement settlement;
        try{
            settlement=GameRepository.settle(new GameResult(id,playerUserIds[0],playerUserIds[1],
                state.scores[0],state.scores[1],"COMPLETED",null,
                requestedCoins[0],requestedCoins[1],coinsChange[0],coinsChange[1],
                points[0],points[1],startedAt));
        }catch(Exception e){
            finished=true;
            refundFailedSettlementPurchases();
            notifySettlementFailure(e);
            cleanupFinishedRoom();
            return;
        }
        finished=true;
        int[] applied=settlement.coinChanges();
        int[] netChanges=settlement.netCoinChanges();
        int[] balances=settlement.balances();
        for(int i=0;i<2;i++){
            coinsChange[i]=netChanges[i];
            rankChange[i]=points[i];
        }
        ObjectNode o=Json.object("GAME_OVER");
        o.put("score1",state.scores[0]);
        o.put("score2",state.scores[1]);
        o.put("winner",winner<0?"DRAW":players.get(winner).username);
        o.put("player1",players.get(0).username);
        o.put("player2",players.get(1).username);
        o.put("coin1",applied[0]);
        o.put("coin2",applied[1]);
        o.put("purchaseCoin1",purchaseChanges[0]);
        o.put("purchaseCoin2",purchaseChanges[1]);
        o.put("finishCoin1",applied[0]);
        o.put("finishCoin2",applied[1]);
        o.put("netCoin1",netChanges[0]);
        o.put("netCoin2",netChanges[1]);
        o.put("rank1",points[0]);
        o.put("rank2",points[1]);
        o.put("coins1",balances[0]);
        o.put("coins2",balances[1]);
        broadcast(o);
        cleanupFinishedRoom();
    }

    public synchronized void cancelMatch(ClientHandler quitter,String reason){
        if(finished||cancelled||players.size()!=2)return;
        cancelTurnTimeout();
        int q=index(quitter);
        if(q<0)q=0;
        int[] purchaseChanges={coinsChange[0],coinsChange[1]};
        int[] requested={0,0};
        requested[q]=-10;
        requested[1-q]=-purchaseChanges[1-q];
        GameRepository.Settlement settlement;
        try{
            settlement=GameRepository.settle(new GameResult(id,playerUserIds[0],playerUserIds[1],
                state.scores[0],state.scores[1],"CANCELLED",reason,
                requested[0],requested[1],coinsChange[0],coinsChange[1],0,0,startedAt));
        }catch(Exception e){
            cancelled=true;
            refundFailedSettlementPurchases();
            notifySettlementFailure(e);
            cleanupFinishedRoom();
            return;
        }
        cancelled=true;
        int[] applied=settlement.coinChanges();
        int[] netChanges=settlement.netCoinChanges();
        for(int i=0;i<2;i++)coinsChange[i]=netChanges[i];
        ObjectNode o=Json.object("MATCH_CANCELLED");
        o.put("reason",reason);
        o.put("quitter",players.get(q).username);
        o.put("player1",players.get(0).username);
        o.put("player2",players.get(1).username);
        o.put("coin1",applied[0]);
        o.put("coin2",applied[1]);
        // Xu mua thẻ: người thoát vẫn bị trừ, người ở lại hiển thị 0
        o.put("purchaseCoin1", q == 0 ? purchaseChanges[0] : 0);
        o.put("purchaseCoin2", q == 1 ? purchaseChanges[1] : 0);

        // Trận bị hủy nên không cộng xu hoàn thành
        o.put("finishCoin1", 0);
        o.put("finishCoin2", 0);
        o.put("coins1",settlement.balances()[0]);
        o.put("coins2",settlement.balances()[1]);
        broadcast(o);
        cleanupFinishedRoom();
    }

    private void refundFailedSettlementPurchases(){
        for(int i=0;i<2;i++){
            int refund=-Math.min(0,coinsChange[i]);
            if(refund>0&&playerUserIds[i]>0){
                try {
                    UserRepository.adjustCoins(playerUserIds[i],refund);
                    coinsChange[i]=0;
                } catch (RuntimeException refundError) {
                    System.err.println("[DB] Purchase refund failed for user " + playerUserIds[i] + ": " + refundError.getMessage());
                }
            }
        }
    }

    private void cancelTurnTimeout(){
        turnGeneration++;
        if(turnTimeout!=null){
            turnTimeout.cancel(false);
            turnTimeout=null;
        }
        turnDeadlineNanos=0;
    }

    private void scheduleTurnTimeout(){
        cancelTurnTimeout();
        long generation=++turnGeneration;
        turnDeadlineNanos=System.nanoTime()+TimeUnit.SECONDS.toNanos(TURN_SECONDS);
        turnTimeout=GameService.TIMER.schedule(()->{
            synchronized(Room.this){
                if(generation!=turnGeneration||!started||finished||cancelled||players.size()!=2)return;
                turnTimeout=null;
                turnDeadlineNanos=0;
                try {
                    if(state.waitingShield){
                        int target=1-state.pendingAttacker;
                        shieldResponse(players.get(target),false);
                        return;
                    }
                    int timedOutPlayer=state.turn;
                    state.promptPending=false;
                    state.promptType="";
                    state.startTurnCardWindow=false;
                    state.turnCardUsed=true;
                    if(!state.opened.isEmpty()){
                        state.opened.clear();
                        ObjectNode reset=Json.object("BOARD_RESET");
                        reset.put("currentPlayer",players.get(timedOutPlayer).username);
                        broadcast(reset);
                    }
                    applyCurseSkip(timedOutPlayer);
                    ObjectNode timeout=Json.object("TURN_TIMEOUT");
                    timeout.put("player",players.get(timedOutPlayer).username);
                    broadcast(timeout);
                    passTurn();
                } catch (RuntimeException e) {
                    System.err.println("[TIMER] Turn timeout failed for room " + id + ": " + e.getMessage());
                    if(started&&!finished&&!cancelled&&players.size()==2) scheduleTurnTimeout();
                }
            }
        },TURN_SECONDS,TimeUnit.SECONDS);
    }

    private void notifySettlementFailure(Exception e){
        System.err.println("[DB] Match settlement failed for room "+id+": "+e.getMessage());
        ObjectNode failed=Json.object("MATCH_CANCELLED");
        failed.put("reason","Không thể lưu kết quả trận đấu do lỗi cơ sở dữ liệu. Trận đã được đóng; vui lòng kiểm tra số dư.");
        if(players.size()==2){
            failed.put("player1",players.get(0).username);
            failed.put("player2",players.get(1).username);
            try { failed.put("coins1",UserRepository.findCoins(playerUserIds[0])); }
            catch(RuntimeException ignored) { failed.put("coins1",-1); }
            try { failed.put("coins2",UserRepository.findCoins(playerUserIds[1])); }
            catch(RuntimeException ignored) { failed.put("coins2",-1); }
        }
        failed.put("coin1",0);
        failed.put("coin2",0);
        failed.put("purchaseCoin1",0);
        failed.put("purchaseCoin2",0);
        failed.put("finishCoin1",0);
        failed.put("finishCoin2",0);
        broadcast(failed);
    }

    private void cleanupFinishedRoom(){
        cancelPurchaseTimeout();
        cancelTurnTimeout();
        for(ClientHandler h:new ArrayList<>(players))h.room=null;
        players.clear();
        RoomRegistry.all().remove(id,this);
        RoomService.broadcastLobby();
    }
}
