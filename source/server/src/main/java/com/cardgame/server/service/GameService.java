package com.cardgame.server.service;


import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import com.cardgame.server.network.ClientHandler;
import com.cardgame.server.model.Room;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
public final class GameService {
    public static final ScheduledExecutorService TIMER = Executors.newScheduledThreadPool(4);
    private GameService() {
    }
    private static Room room(ClientHandler c){
        return c.room;
    }
    public static void rpsChoice(ClientHandler c,String v){
        if(room(c)!=null)room(c).rpsChoice(c,v);
    }
    public static void chooseOrder(ClientHandler c,String v){
        if(room(c)!=null)room(c).chooseOrder(c,v);
    }
    public static void updateBuySelection(ClientHandler c,ObjectNode m){
        if(room(c)!=null&&m!=null&&m.get("codes") instanceof ArrayNode codes){
            room(c).updateBuySelection(c,codes);
        }
    }
    public static void confirmBuy(ClientHandler c){
        if(room(c)!=null)room(c).confirmBuy(c);
    }
    public static void flip(ClientHandler c,int i){
        if(room(c)!=null)room(c).flip(c,i);
    }
    public static void useCard(ClientHandler c,String code,int i){
        if(room(c)!=null)room(c).useCard(c,code,i);
    }
    public static void skipCard(ClientHandler c){
        if(room(c)!=null)room(c).skipCard(c);
    }
    public static void shieldResponse(ClientHandler c,boolean use){
        if(room(c)!=null)room(c).shieldResponse(c,use);
    }
    public static void leaveMatch(ClientHandler c){
        if(room(c)!=null)room(c).leaveByChoice(c);
    }
}
