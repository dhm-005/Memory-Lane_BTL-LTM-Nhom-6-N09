package com.mcclient.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Công cụ kiểm thử TCP độc lập cho Ngày 6 - Test 2 và Test 5.
 *
 * Chạy khi server đang hoạt động:
 *   mvn -q -DskipTests compile
 *   mvn org.codehaus.mojo:exec-maven-plugin:3.5.0:java \
 *       "-Dexec.mainClass=com.mcclient.tools.RoomEdgeCaseTcpTest"
 *
 * Dùng giao thức TCP JSON-line của Memory Lane, cổng mặc định 5000.
 * Mỗi peer có một socket và một luồng đọc riêng để xử lý response theo requestId.
 */
public final class RoomEdgeCaseTcpTest {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 5000;
    private static final long TIMEOUT_SECONDS = 8;

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final AtomicLong REQUEST_SEQ = new AtomicLong();

    private RoomEdgeCaseTcpTest() {}

    public static void main(String[] args) throws Exception {
        long seed = System.currentTimeMillis() * 10L;
        long hostId = seed + 1;
        long bobId = seed + 2;
        long charlieId = seed + 3;
        long test5Id = seed + 4;

        System.out.println("=== MEMORY LANE - ROOM EDGE CASE TCP TEST ===");
        System.out.println("Server: " + HOST + ":" + PORT);

        testConcurrentLastSlot(hostId, bobId, charlieId);
        testCreateRoomWhileAlreadyInRoom(test5Id);

        System.out.println();
        System.out.println("KẾT LUẬN: Cả Test 2 và Test 5 đều PASS.");
    }

    /**
     * Test 2: Alice tạo phòng 1/2. Bob và Charlie gửi JOIN_ROOM đồng thời.
     * Kỳ vọng chính xác một SUCCESS và một ROOM_FULL.
     */
    private static void testConcurrentLastSlot(long hostId, long bobId, long charlieId)
            throws Exception {
        System.out.println();
        System.out.println("[TEST 2] Hai client cùng tranh slot cuối");

        try (Peer host = new Peer(hostId, "edge_host");
             Peer bob = new Peer(bobId, "edge_bob");
             Peer charlie = new Peer(charlieId, "edge_charlie")) {

            host.register();
            bob.register();
            charlie.register();

            JsonNode created = host.request("CREATE_ROOM", Map.of());
            requireSuccess(created, "Host không tạo được phòng");
            String roomId = created.path("data").path("roomId").asText("");

            // Một số phiên bản handler có thể chỉ trả roomId trong event data.
            if (roomId.isBlank()) {
                roomId = created.path("data").path("rooms").path(0).path("id").asText("");
            }
            if (roomId.isBlank()) {
                throw new AssertionError(
                    "Không tìm thấy roomId trong response CREATE_ROOM. Response: " + created);
            }

            System.out.println("Phòng thử nghiệm: " + roomId);
            final String targetRoomId = roomId;
            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch start = new CountDownLatch(1);
            ExecutorService pool = Executors.newFixedThreadPool(2);

            try {
                CompletableFuture<JsonNode> bobJoin = CompletableFuture.supplyAsync(() -> {
                    ready.countDown();
                    await(start);
                    try {
                        return bob.request("JOIN_ROOM", Map.of("roomId", targetRoomId));
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
                }, pool);

                CompletableFuture<JsonNode> charlieJoin = CompletableFuture.supplyAsync(() -> {
                    ready.countDown();
                    await(start);
                    try {
                        return charlie.request("JOIN_ROOM", Map.of("roomId", targetRoomId));
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
                }, pool);

                if (!ready.await(5, TimeUnit.SECONDS)) {
                    throw new AssertionError("Hai luồng không sẵn sàng đúng hạn");
                }
                start.countDown();

                JsonNode bobResult = bobJoin.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                JsonNode charlieResult = charlieJoin.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

                boolean bobOk = isSuccess(bobResult);
                boolean charlieOk = isSuccess(charlieResult);
                String bobCode = errorCode(bobResult);
                String charlieCode = errorCode(charlieResult);

                System.out.println("Bob:     " + (bobOk ? "SUCCESS" : bobCode));
                System.out.println("Charlie: " + (charlieOk ? "SUCCESS" : charlieCode));

                boolean exactlyOneSuccess = bobOk ^ charlieOk;
                boolean loserIsRoomFull = bobOk
                        ? "ROOM_FULL".equals(charlieCode)
                        : charlieOk && "ROOM_FULL".equals(bobCode);

                if (!exactlyOneSuccess || !loserIsRoomFull) {
                    throw new AssertionError(
                        "Kỳ vọng một SUCCESS và một ROOM_FULL. Bob=" + bobResult
                        + "; Charlie=" + charlieResult);
                }

                System.out.println("PASS: Chỉ một client chiếm được slot cuối.");
            } finally {
                pool.shutdownNow();
            }

            // Dọn phòng thử nghiệm. Client thua không ở trong phòng nên không LEAVE.
            safeLeave(bob);
            safeLeave(charlie);
            safeLeave(host);
        }
    }

    /**
     * Test 5: cùng một user/socket tạo phòng lần đầu thành công,
     * tạo phòng lần hai phải bị ALREADY_IN_ROOM.
     */
    private static void testCreateRoomWhileAlreadyInRoom(long userId) throws Exception {
        System.out.println();
        System.out.println("[TEST 5] Tạo phòng khi user đang ở trong phòng");

        try (Peer user = new Peer(userId, "edge_test5")) {
            user.register();

            JsonNode first = user.request("CREATE_ROOM", Map.of());
            requireSuccess(first, "Lần tạo phòng đầu tiên phải thành công");

            JsonNode second = user.request("CREATE_ROOM", Map.of());
            String code = errorCode(second);

            System.out.println("CREATE_ROOM lần 1: " + (isSuccess(first) ? "SUCCESS" : errorCode(first)));
            System.out.println("CREATE_ROOM lần 2: " + code);

            if (isSuccess(second) || !"ALREADY_IN_ROOM".equals(code)) {
                throw new AssertionError(
                    "Kỳ vọng lần 2 bị từ chối ALREADY_IN_ROOM. Response: " + second);
            }

            System.out.println("PASS: Server chặn user tạo phòng thứ hai.");
            safeLeave(user);
        }
    }

    private static void safeLeave(Peer peer) {
        try {
            JsonNode result = peer.request("LEAVE_ROOM", Map.of());
            if (isSuccess(result)) {
                System.out.println("Dọn phòng: " + peer.username + " đã rời phòng.");
            }
        } catch (Exception ignored) {
            // User có thể chưa vào phòng hoặc server đã đóng socket.
        }
    }


    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Luồng kiểm thử bị interrupt", e);
        }
    }

    private static boolean isSuccess(JsonNode response) {
        return response.path("success").asBoolean(false);
    }

    private static String errorCode(JsonNode response) {
        return response.path("data").path("code").asText(
                response.path("type").asText("UNKNOWN_RESPONSE"));
    }

    private static void requireSuccess(JsonNode response, String message) {
        if (!isSuccess(response)) {
            throw new AssertionError(message + ". Response: " + response);
        }
    }

    private static final class Peer implements AutoCloseable {
        private final long userId;
        private final String username;
        private final Socket socket;
        private final BufferedWriter writer;
        private final Map<String, CompletableFuture<JsonNode>> pending =
                new ConcurrentHashMap<>();
        private final Object writeLock = new Object();

        private Peer(long userId, String username) throws IOException {
            this.userId = userId;
            this.username = username;
            this.socket = new Socket(HOST, PORT);
            this.socket.setKeepAlive(true);
            this.writer = new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream(), StandardCharsets.UTF_8));

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), StandardCharsets.UTF_8));

            Thread readerThread = new Thread(() -> readLoop(reader),
                    "edge-test-reader-" + username);
            readerThread.setDaemon(true);
            readerThread.start();
        }

        private void register() throws Exception {
            JsonNode response = request("LIST_ROOMS", Map.of());
            requireSuccess(response, username + " không đăng ký LIST_ROOMS được");
            System.out.println("Đã kết nối: " + username);
        }

        private JsonNode request(String type, Map<String, Object> extraData) throws Exception {
            String requestId = "edge-" + username + "-" + REQUEST_SEQ.incrementAndGet();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("userId", userId);
            data.put("username", username);
            data.putAll(extraData);

            Map<String, Object> message = new LinkedHashMap<>();
            message.put("type", type);
            message.put("requestId", requestId);
            message.put("data", data);

            CompletableFuture<JsonNode> future = new CompletableFuture<>();
            pending.put(requestId, future);

            try {
                String line = JSON.writeValueAsString(message);
                synchronized (writeLock) {
                    writer.write(line);
                    writer.newLine();
                    writer.flush();
                }
                return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } finally {
                pending.remove(requestId);
            }
        }

        private void readLoop(BufferedReader reader) {
            try (reader) {
                String line;
                while ((line = reader.readLine()) != null) {
                    JsonNode message = JSON.readTree(line);
                    JsonNode idNode = message.get("requestId");
                    if (idNode != null && !idNode.isNull()) {
                        CompletableFuture<JsonNode> future =
                                pending.get(idNode.asText());
                        if (future != null) {
                            future.complete(message);
                        }
                    }
                    // ROOM_UPDATE push không có requestId: bỏ qua trong test này.
                }
            } catch (Exception e) {
                pending.values().forEach(f -> f.completeExceptionally(e));
            } finally {
                pending.values().forEach(f -> f.completeExceptionally(
                        new IOException("Server đã đóng kết nối")));
            }
        }

        @Override
        public void close() {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
