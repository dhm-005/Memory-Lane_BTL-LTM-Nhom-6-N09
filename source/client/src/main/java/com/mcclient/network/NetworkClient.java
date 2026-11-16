package com.mcclient.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.mcclient.util.Json;

/** Một kết nối TCP dùng chung cho cả nhóm. Luồng đọc riêng; phản hồi ghép theo requestId; sự kiện đẩy gọi PushListener. */
public class NetworkClient {

    public interface PushListener {
        void onPush(String type, JsonNode data);
    }

    private static final NetworkClient INSTANCE = new NetworkClient();

    public static NetworkClient getInstance() {
        return INSTANCE;
    }

    private final Map<String, CompletableFuture<JsonNode>> pending = new ConcurrentHashMap<>();
    private final List<PushListener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicLong seq = new AtomicLong();
    private final Object sendLock = new Object();
    private volatile Socket socket;
    private volatile BufferedWriter out;

    public synchronized void connect(String host, int port) throws IOException {
        if (socket != null && !socket.isClosed()) {
            return;
        }
        socket = new Socket(host, port);
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        Thread.ofPlatform().name("network-reader").daemon(true).start(() -> readLoop(in));
    }

    public void addPushListener(PushListener l) {
        listeners.add(l);
    }

    /** Hoàn tất với "data" khi success=true; ServerException(code, message) khi success=false. */
    public CompletableFuture<JsonNode> request(String type, Map<String, Object> data) {
        CompletableFuture<JsonNode> f = new CompletableFuture<>();
        String id = "req-" + seq.incrementAndGet();
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("type", type);
        msg.put("requestId", id);
        msg.put("data", data == null ? Map.of() : data);
        pending.put(id, f);
        synchronized (sendLock) {
            try {
                if (out == null) {
                    throw new IOException("Chưa kết nối server");
                }
                out.write(Json.MAPPER.writeValueAsString(msg));
                out.newLine();
                out.flush();
            } catch (IOException e) {
                pending.remove(id);
                f.completeExceptionally(e);
            }
        }
        return f;
    }

    private void readLoop(BufferedReader in) {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                try {
                    dispatch(Json.MAPPER.readTree(line));
                } catch (JsonProcessingException e) {
                    System.err.println("Bad JSON from server: " + line);
                }
            }
        } catch (IOException ignored) {
            // kết nối đóng
        } finally {
            onDisconnected();
        }
    }

    private void dispatch(JsonNode node) {
        if (node.hasNonNull("requestId")) { // phản hồi cho một request
            CompletableFuture<JsonNode> f = pending.remove(node.get("requestId").asText());
            if (f == null) {
                return;
            }
            JsonNode data = node.path("data");
            if (node.path("success").asBoolean(false)) {
                f.complete(data);
            } else {
                f.completeExceptionally(new ServerException(
                        data.path("code").asText("ERROR"), data.path("message").asText("Lỗi không xác định")));
            }
        } else { // sự kiện server chủ động đẩy
            fire(node.path("type").asText(), node.path("data"));
        }
    }

    private void fire(String type, JsonNode data) {
        for (PushListener l : listeners) {
            try {
                l.onPush(type, data);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    private void onDisconnected() {
        synchronized (sendLock) {
            out = null;
        }
        pending.values().forEach(f -> f.completeExceptionally(new IOException("Mất kết nối server")));
        pending.clear();
        fire("DISCONNECTED", Json.MAPPER.nullNode());
    }

    public void close() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
            // bỏ qua
        }
    }
}