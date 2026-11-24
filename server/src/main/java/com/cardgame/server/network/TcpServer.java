package com.cardgame.server.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public final class TcpServer {
    private static final int MAX_CLIENTS = 200;
    private final int port;
    private final Semaphore connections = new Semaphore(MAX_CLIENTS);

    public TcpServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("CARD MEMORY GAME server listening on " + port);
            while (true) {
                Socket socket = server.accept();
                if (!connections.tryAcquire()) {
                    socket.close();
                    continue;
                }
                try {
                    new ClientHandler(socket, connections::release).start();
                } catch (RuntimeException e) {
                    connections.release();
                    socket.close();
                    System.err.println("[TCP] Cannot start client handler: " + e.getMessage());
                }
            }
        }
    }
}
