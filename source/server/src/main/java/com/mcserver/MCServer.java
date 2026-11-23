package com.mcserver;

import java.util.List;

import com.mcserver.network.MessageDispatcher;
import com.mcserver.network.RoomMessageHandler;
import com.mcserver.network.SessionRegistry;
import com.mcserver.network.TcpServer;
import com.mcserver.network.TypeHandler;
import com.mcserver.service.RoomService;

public class MCServer {
    public static void main(String[] args) {
        SessionRegistry sessionRegistry = new SessionRegistry();
        RoomService roomService = new RoomService();

        List<TypeHandler> handlers = List.of(
                new RoomMessageHandler(roomService, sessionRegistry)
        );

        MessageDispatcher dispatcher = new MessageDispatcher(handlers);
        TcpServer server = new TcpServer("0.0.0.0", 5000, dispatcher, sessionRegistry, roomService);

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        server.start();
    }
}