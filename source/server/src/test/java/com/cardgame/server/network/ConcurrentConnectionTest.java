package com.cardgame.server.network;

import com.cardgame.server.util.Json;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class ConcurrentConnectionTest {
    private static final int TEST_PORT = 15501;

    @BeforeAll
    static void startServer() throws Exception {
        TestClientSocket.startTestServer(TEST_PORT);
    }

    @Test
    void testMultipleClientsConnectAndLoginConcurrently() throws Exception {
        int clientCount = 20;
        ExecutorService pool = Executors.newFixedThreadPool(clientCount);
        CountDownLatch readyGate = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();

        long runId = System.currentTimeMillis() % 100000;
        for (int i = 0; i < clientCount; i++) {
            final String user = "conc_" + runId + "_" + i;
            futures.add(pool.submit(() -> {
                readyGate.await();
                try (TestClientSocket client = new TestClientSocket("127.0.0.1", TEST_PORT, user)) {
                    client.registerAndLogin("123456");

                    // Kiểm tra gửi yêu cầu lấy danh sách phòng sau khi đăng nhập
                    client.send(Json.object("LIST_ROOMS"));
                    ObjectNode roomsMsg = client.waitForType("ROOMS", 3000);
                    assertEquals("ROOMS", Json.str(roomsMsg, "type"));

                    client.send(Json.object("LOGOUT"));
                    return true;
                }
            }));
        }

        // Thả tất cả 20 luồng kết nối cùng một thời điểm
        readyGate.countDown();

        for (Future<Boolean> f : futures) {
            assertTrue(f.get(10, TimeUnit.SECONDS), "Mọi client đồng thời phải kết nối và đăng nhập thành công");
        }
        pool.shutdownNow();
    }
}