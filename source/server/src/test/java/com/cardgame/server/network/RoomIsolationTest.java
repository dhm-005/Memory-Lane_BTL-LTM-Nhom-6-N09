package com.cardgame.server.network;

import com.cardgame.server.util.Json;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RoomIsolationTest {
    private static final int TEST_PORT = 15503;

    @BeforeAll
    static void startServer() throws Exception {
        TestClientSocket.startTestServer(TEST_PORT);
    }

    @Test
    void testEventsInRoomADoNotLeakToRoomB() throws Exception {
        long runId = System.currentTimeMillis() % 100000;
        try (
            TestClientSocket pA1 = new TestClientSocket("127.0.0.1", TEST_PORT, "isoA1_" + runId);
            TestClientSocket pA2 = new TestClientSocket("127.0.0.1", TEST_PORT, "isoA2_" + runId);
            TestClientSocket pB1 = new TestClientSocket("127.0.0.1", TEST_PORT, "isoB1_" + runId);
            TestClientSocket pB2 = new TestClientSocket("127.0.0.1", TEST_PORT, "isoB2_" + runId)
        ) {
            pA1.registerAndLogin("123456");
            pA2.registerAndLogin("123456");
            pB1.registerAndLogin("123456");
            pB2.registerAndLogin("123456");

            // Tạo phòng A và ghép đủ 2 người
            pA1.send(Json.object("CREATE_ROOM"));
            String roomA = Json.str(pA1.waitForType("ROOM_JOINED", 3000), "roomId");
            ObjectNode joinA = Json.object("JOIN_ROOM");
            joinA.put("roomId", roomA);
            pA2.send(joinA);
            pA2.waitForType("ROOM_JOINED", 3000);

            // Tạo phòng B và ghép đủ 2 người
            pB1.send(Json.object("CREATE_ROOM"));
            String roomB = Json.str(pB1.waitForType("ROOM_JOINED", 3000), "roomId");
            ObjectNode joinB = Json.object("JOIN_ROOM");
            joinB.put("roomId", roomB);
            pB2.send(joinB);
            pB2.waitForType("ROOM_JOINED", 3000);

            // Xả sạch các gói tin trạng thái phòng ban đầu của phòng B
            pB1.drainMessages(300);
            pB2.drainMessages(300);

            // Phòng A bấm READY cả 2 người -> Bắt đầu oẳn tù tì & chọn thẻ
            pA1.send(Json.object("READY"));
            pA2.send(Json.object("READY"));
            assertNotNull(pA1.waitForType("RPS_START", 3000));
            assertNotNull(pA2.waitForType("RPS_START", 3000));

            // Kiểm tra hộp thư của pB1 và pB2: không được nhận RPS_START hay ROOM_STATE của phòng A
            List<ObjectNode> leakedToB1 = pB1.drainMessages(400);
            List<ObjectNode> leakedToB2 = pB2.drainMessages(400);

            for (ObjectNode msg : leakedToB1) {
                String type = Json.str(msg, "type");
                assertNotEquals("RPS_START", type, "Sự kiện RPS của phòng A bị rò rỉ sang phòng B!");
                assertNotEquals("GAME_START", type, "Sự kiện GAME_START của phòng A bị rò rỉ sang phòng B!");
            }
            assertTrue(leakedToB2.isEmpty(), "Phòng B không được nhận bất kỳ gói tin trận đấu nào từ Phòng A");
        }
    }
}