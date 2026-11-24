package com.cardgame.server.network;


import tools.jackson.databind.node.ObjectNode;
import com.cardgame.server.MainServer;
import com.cardgame.server.network.OnlineUsers;
import com.cardgame.server.model.Room;
import com.cardgame.server.util.Json;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
public final class ClientHandler extends Thread {
    public final Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private final Runnable onClose;
    public volatile int userId = -1;
    public volatile String username = "";
    public volatile Room room;
    public int soloProgressLevel;
    public long soloRunStartedAtNanos;
    public long soloLevelStartedAtNanos;
    public ClientHandler(Socket socket) {
        this(socket, () -> {});
    }
    public ClientHandler(Socket socket, Runnable onClose) {
        this.socket = socket;
        this.onClose = onClose;
        setName("client-" + socket.getPort());
    }
    public synchronized void send(ObjectNode message) {
        try {
            if (writer != null) Json.send(writer, message);
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
    @Override
    public void run() {
        try {
            socket.setKeepAlive(true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = readBoundedLine(reader, 65536)) != null) {
                try {
                    MessageDispatcher.dispatch(this, Json.parse(line));
                } catch (Exception e) {
                    send(MainServer.error("Yêu cầu không hợp lệ: " + e.getMessage()));
                }
            }
        } catch (IOException ignored) {
            // The client closed the connection.
        } finally {
            try {
                disconnect();
            } finally {
                onClose.run();
            }
        }
    }
    private String readBoundedLine(BufferedReader input, int maxLength) throws IOException {
        StringBuilder line = new StringBuilder();
        int value;
        while ((value = input.read()) != -1) {
            if (value == '\n') return line.toString();
            if (value != '\r') {
                if (line.length() >= maxLength) throw new IOException("Dữ liệu gửi lên vượt quá giới hạn.");
                line.append((char) value);
            }
        }
        return line.isEmpty() ? null : line.toString();
    }
    public void disconnect() {
        if (userId > 0) OnlineUsers.all().remove(userId, this);
        Room currentRoom = room;
        if (currentRoom != null) currentRoom.playerDisconnected(this);
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
