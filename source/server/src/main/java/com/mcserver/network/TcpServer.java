package com.mcserver.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mcserver.service.RoomService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TcpServer {
    private final String host;
    private final int port;
    private final MessageDispatcher dispatcher;
    private final SessionRegistry sessionRegistry;
    private final RoomService roomService;

    private volatile boolean running;
    private ServerSocket serverSocket;
    private ExecutorService clientExecutor;
    private Thread acceptThread;
    

    public synchronized void start() {
        if (running) {
            return;
        }

        try {
            ServerSocket socket = new ServerSocket();
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(host, port));
            serverSocket = socket;
            clientExecutor = Executors.newVirtualThreadPerTaskExecutor();
            running = true;
            acceptThread = Thread.ofPlatform()
                    .name("tcp-server-accept")
                    .start(this::acceptConnections);
            System.out.printf("TCP server listening on {%s}:{%s}\n",host,socket.getLocalPort());
        } catch (IOException e) {
            throw new IllegalStateException("Could not start TCP server on " + host + ":" + port, e);
        }
    }

    private void acceptConnections() {
        while (running) {
            try {
                Socket client = serverSocket.accept();
                clientExecutor.submit(() -> handleClient(client));
            } catch (SocketException e) {
                if (running) {
                    System.err.println("TCP server socket failed. " + e);
                }
                return;
            } catch (IOException e) {
                if (running) {
                    System.err.println("Could not accept TCP client. "+ e);
                }
            }
        }
    }

    private void handleClient(Socket client) {
        ClientSession session = null;
        try (client;
                BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
                BufferedWriter output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8))) {
            session = new ClientSession(output);
            sessionRegistry.add(session);
            String line;
            while ((line = input.readLine()) != null) {
                session.send(dispatcher.dispatch(line, session));
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("Client I/O error: " + e.getMessage());
                e.printStackTrace();
            }
        } finally {
            if (session != null) {
                sessionRegistry.remove(session);
                Long uid = session.getUserId();
                if (uid != null) {
                    roomService.handleDisconnect(uid); // Đóng cửa sổ = tự rời phòng
                }
            }
        }
    }

    public synchronized void stop() {
        running = false;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.out.println("Could not close TCP server socket: "+ e);
            }
        }
        if (clientExecutor != null) {
            clientExecutor.shutdownNow();
        }
        if (acceptThread != null) {
            acceptThread.interrupt();
        }
    }
}