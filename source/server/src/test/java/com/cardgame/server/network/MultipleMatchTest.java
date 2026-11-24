package com.cardgame.server.network;

import com.cardgame.server.util.Json;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class MultipleMatchTest {
    private static final int TEST_PORT = 15504;

    @BeforeAll
    static void startServer() throws Exception {
        TestClientSocket.startTestServer(TEST_PORT);
    }

    @Test
    void testMultiplePvPMatchesRunInParallel() throws Exception {
        int matchCount = 3; // Chạy đồng thời 3 trận đấu 1v1 (6 người chơi)
        ExecutorService pool = Executors.newFixedThreadPool(matchCount);
        List<Future<Boolean>> results = new ArrayList<>();
        long runId = System.currentTimeMillis() % 100000;

        for (int m = 0; m < matchCount; m++) {
            final int matchIndex = m;
            results.add(pool.submit(() -> runSingleMatch(runId, matchIndex)));
        }

        for (Future<Boolean> f : results) {
            assertTrue(f.get(15, TimeUnit.SECONDS), "Mỗi trận 1v1 song song phải vào bàn và lật bài thành công");
        }
        pool.shutdownNow();
    }

    private boolean runSingleMatch(long runId, int matchIndex) throws Exception {
        try (
            TestClientSocket p1 = new TestClientSocket("127.0.0.1", TEST_PORT, "m" + matchIndex + "_p1_" + runId);
            TestClientSocket p2 = new TestClientSocket("127.0.0.1", TEST_PORT, "m" + matchIndex + "_p2_" + runId)
        ) {
            p1.registerAndLogin("123456");
            p2.registerAndLogin("123456");

            // Tạo phòng & vào phòng
            p1.send(Json.object("CREATE_ROOM"));
            String roomId = Json.str(p1.waitForType("ROOM_JOINED", 3000), "roomId");

            ObjectNode join = Json.object("JOIN_ROOM");
            join.put("roomId", roomId);
            p2.send(join);
            p2.waitForType("ROOM_JOINED", 3000);

            // Cả hai sẵn sàng -> Vào oẳn tù tì
            p1.send(Json.object("READY"));
            p2.send(Json.object("READY"));
            p1.waitForType("RPS_START", 3000);
            p2.waitForType("RPS_START", 3000);

            // P1 chọn Búa (ROCK), P2 chọn Kéo (SCISSORS) -> P1 thắng
            ObjectNode rps1 = Json.object("RPS_CHOICE");
            rps1.put("choice", "ROCK");
            p1.send(rps1);

            ObjectNode rps2 = Json.object("RPS_CHOICE");
            rps2.put("choice", "SCISSORS");
            p2.send(rps2);

            ObjectNode rpsRes1 = p1.waitForType("RPS_RESULT", 3000);
            assertTrue(rpsRes1.path("youWin").asBoolean(false));

            // P1 chọn đánh trước (FIRST) -> Chuyển sang chọn thẻ
            ObjectNode order = Json.object("CHOOSE_ORDER");
            order.put("order", "FIRST");
            p1.send(order);

            p1.waitForType("CARD_OFFERS", 3000);
            p2.waitForType("CARD_OFFERS", 3000);

            // Xác nhận không mua thẻ (hoặc mua mặc định) để bắt đầu bàn chơi
            p1.send(Json.object("BUY_CONFIRM"));
            p2.send(Json.object("BUY_CONFIRM"));

            ObjectNode start1 = p1.waitForType("GAME_START", 3000);
            ObjectNode start2 = p2.waitForType("GAME_START", 3000);
            assertEquals(p1.username, Json.str(start1, "currentPlayer"));
            assertEquals(p1.username, Json.str(start2, "currentPlayer"));

            // P1 lật lá bài đầu tiên (index = 0) -> Cả 2 cùng nhận được CARD_FLIP
            ObjectNode flip = Json.object("FLIP");
            flip.put("index", 0);
            p1.send(flip);

            ObjectNode flip1 = p1.waitForType("CARD_FLIP", 3000);
            ObjectNode flip2 = p2.waitForType("CARD_FLIP", 3000);
            assertEquals(0, Json.integer(flip1, "index", -1));
            assertEquals(0, Json.integer(flip2, "index", -1));

            return true;
        }
    }
}