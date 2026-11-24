package com.cardgame.server.service;


import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import com.cardgame.server.model.Room;
import com.cardgame.server.network.ClientHandler;
import com.cardgame.server.util.Json;
import java.util.concurrent.ThreadLocalRandom;
import static com.cardgame.server.MainServer.error;
import com.cardgame.server.network.OnlineUsers;
import com.cardgame.server.repository.UserRepository;
public final class RoomService {
    private RoomService() {
    }
    private static ObjectNode roomsMessage(){
        ObjectNode o=Json.object("ROOMS");
        ArrayNode a=Json.MAPPER.createArrayNode();
        for(Room r:RoomRegistry.all().values())synchronized(r){
            if(!r.started&&!r.cancelled&&!r.joinReserved&&r.players.size()==1){
                ObjectNode x=Json.MAPPER.createObjectNode();
                x.put("roomId",r.id);
                x.put("host",r.players.get(0).username);
                x.put("players",1);
                a.add(x);
            }
        }
        o.set("rooms",a);
        return o;
    }
    public static void listRooms(ClientHandler client){
        client.send(roomsMessage());
    }
    public static void createRoom(ClientHandler client){
        if(client.userId<=0){
            client.send(error("Hãy đăng nhập trước."));
            return;
        }
        if(UserRepository.findCoins(client.userId)<10){
            client.send(error("Bạn cần có ít nhất 10 xu để tạo phòng 1v1."));
            return;
        }
        if(client.room!=null)client.room.leaveWaiting(client);
        Room created=null;
        int firstId=ThreadLocalRandom.current().nextInt(1000,10000);
        for(int attempt=0;attempt<9000;attempt++){
            int numericId=1000+((firstId-1000+attempt)%9000);
            String id=String.format("%04d",numericId);
            Room candidate=new Room(id);
            if(RoomRegistry.all().putIfAbsent(id,candidate)==null){
                created=candidate;
                break;
            }
        }
        if(created==null){
            client.send(error("Không thể tạo phòng lúc này. Vui lòng thử lại."));
            return;
        }
        synchronized(created){
            created.players.add(client);
            client.room=created;
        }
        sendRoomJoined(client,true);
        broadcastLobby();
    }
    public static void broadcastLobby(){
        for(ClientHandler h:OnlineUsers.all().values())h.send(Json.object("LOBBY_UPDATE"));
    }
    public static void sendRoomJoined(ClientHandler client,boolean host){
        ObjectNode o=Json.object("ROOM_JOINED");
        o.put("roomId",client.room.id);
        o.put("host",host);
        client.send(o);
    }
    public static void joinRoom(ClientHandler client,String id){
        if(client.userId<=0){
            client.send(error("Hãy đăng nhập trước."));
            return;
        }
        if(UserRepository.findCoins(client.userId)<10){
            client.send(error("Bạn cần có ít nhất 10 xu để tham gia phòng 1v1."));
            return;
        }
        Room r=RoomRegistry.all().get(id);
        if(r==null){
            client.send(error("Không tìm thấy phòng."));
            return;
        }
        if(client.room==r){
            client.send(error("Bạn đang ở trong phòng này rồi."));
            return;
        }
        synchronized(r){
            if(RoomRegistry.all().get(id)!=r || r.started || r.cancelled || r.joinReserved || r.players.size()!=1){
                client.send(error("Phòng đã đủ người hoặc không còn hoạt động."));
                return;
            }
            r.joinReserved=true;
        }
        Room oldRoom=client.room;
        try {
            if(oldRoom!=null)oldRoom.leaveWaiting(client);
        } catch (RuntimeException e) {
            synchronized(r){ r.joinReserved=false; }
            client.send(error("Không thể rời phòng hiện tại. Vui lòng thử lại."));
            return;
        }
        synchronized(r){
            if(RoomRegistry.all().get(id)!=r || r.started || r.cancelled || r.players.size()!=1){
                r.joinReserved=false;
                client.send(error("Phòng đã thay đổi trạng thái. Vui lòng thử lại."));
                return;
            }
            r.players.add(client);
            r.joinReserved=false;
            client.room=r;
            sendRoomJoined(client,false);
            r.broadcastLobbyState();
            broadcastLobby();
        }
    }
    public static void setReady(ClientHandler client, boolean ready) {
        if (client.room != null) client.room.setReady(client, ready);
    }

    public static void leave(ClientHandler client) {
        if (client.room != null) client.room.leaveWaiting(client);
    }

}
