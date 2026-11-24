package com.cardgame.server.network;

import com.cardgame.server.config.DatabaseConfig;
import com.cardgame.server.service.CardService;
import com.cardgame.server.util.Json;
import tools.jackson.databind.node.ObjectNode;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

final class TestClientSocket implements Closeable {
    final String username;
    private final Socket socket;
    private final BufferedWriter writer;
    private final BlockingQueue<ObjectNode> inbox = new LinkedBlockingQueue<>();

    TestClientSocket(String host, int port, String username) throws IOException {
        this.username = username;
        this.socket = new Socket(host, port);
        this.socket.setSoTimeout(5000);
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

        Thread readerThread = new Thread(() -> {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    inbox.offer(Json.parse(line));
                }
            } catch (Exception ignored) {
            }
        }, "test-client-" + username);
        readerThread.setDaemon(true);
        readerThread.start();
    }
    
    static synchronized void startTestServer(int port) throws Exception {
        DatabaseConfig.load();
        CardService.loadFromDatabase();

        Thread serverThread = new Thread(() -> {
            try {
                new TcpServer(port).start();
            } catch (Exception ignored) {
            }
        }, "test-tcp-server-" + port);
        serverThread.setDaemon(true);
        serverThread.start();
        Thread.sleep(300);
    }

    synchronized void send(ObjectNode payload) throws IOException {
        Json.send(writer, payload);
    }

    void registerAndLogin(String password) throws Exception {
        ObjectNode reg = Json.object("REGISTER");
        reg.put("username", username);
        reg.put("password", password);
        send(reg);
        // Chờ phản hồi đăng ký (có thể LOGIN_OK luôn hoặc ERROR nếu user đã tồn tại từ lần test trước)
        ObjectNode regRes = waitForAny(Set.of("LOGIN_OK", "ERROR"), 3000);
        if ("ERROR".equals(Json.str(regRes, "type"))) {
            ObjectNode login = Json.object("LOGIN");
            login.put("username", username);
            login.put("password", password);
            send(login);
            ObjectNode loginRes = waitForAny(Set.of("LOGIN_OK", "ERROR"), 3000);
            if (!"LOGIN_OK".equals(Json.str(loginRes, "type"))) {
                throw new IllegalStateException("Login failed for " + username + ": " + loginRes);
            }
        }
    }

    ObjectNode waitForType(String expectedType, long timeoutMs) throws InterruptedException {
        return waitForAny(Set.of(expectedType), timeoutMs);
    }

    ObjectNode waitForAny(Set<String> expectedTypes, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            long remain = Math.max(1, deadline - System.currentTimeMillis());
            ObjectNode msg = inbox.poll(remain, TimeUnit.MILLISECONDS);
            if (msg == null) break;
            if (expectedTypes.contains(Json.str(msg, "type"))) {
                return msg;
            }
        }
        throw new AssertionError("Timeout waiting for " + expectedTypes + " on client " + username);
    }

    List<ObjectNode> drainMessages(long quietWaitMs) throws InterruptedException {
        List<ObjectNode> list = new ArrayList<>();
        ObjectNode msg;
        while ((msg = inbox.poll(quietWaitMs, TimeUnit.MILLISECONDS)) != null) {
            list.add(msg);
        }
        return list;
    }

    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}