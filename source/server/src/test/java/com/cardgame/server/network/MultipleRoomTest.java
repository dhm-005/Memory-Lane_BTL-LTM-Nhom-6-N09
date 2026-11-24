package com.cardgame.server.network;

import com.cardgame.server.util.Json;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class MultipleRoomTest {
    private static final int TEST_PORT = 15502;

    @BeforeAll
    static void startServer() throws Exception {
        TestClientSocket.startTestServer(TEST_PORT);
    }

    @Test
    void testCreateAndJoinMultipleRoomsSimultaneously() throws Exception {
        int roomCount = 5;
        long runId = System.currentTimeMillis() % 100000;
        List<TestClientSocket> hosts = new ArrayList<>();
        List<TestClientSocket> guests = new ArrayList<>();
        Set<String> roomIds = new HashSet<>();

        try {
            // 1. Tạo đồng thời 5 phòng chờ
            for (int i = 0; i < roomCount; i++) {
                TestClientSocket host = new TestClientSocket("127.0.0.1", TEST_PORT, "host_" + runId + "_" + i);
                host.registerAndLogin("123456");
                host.send(Json.object("CREATE_ROOM"));

                ObjectNode joined = host.waitForType("ROOM_JOINED", 3000);
                String roomId = Json.str(joined, "roomId");
                assertFalse(roomId.isBlank(), "Mã phòng không được rỗng");
                assertTrue(roomIds.add(roomId), "Mã phòng bị trùng lặp: " + roomId);
                hosts.add(host);
            }

            // 2. Kiểm tra LIST_ROOMS chứa đủ cả 5 phòng vừa tạo
            TestClientSocket observer = new TestClientSocket("127.0.0.1", TEST_PORT, "obs_" + runId);
            observer.registerAndLogin("123456");
            observer.send(Json.object("LIST_ROOMS"));
            ObjectNode roomsList = observer.waitForType("ROOMS", 3000);

            Set<String> listedIds = new HashSet<>();
            for (JsonNode r : roomsList.path("rooms")) {
                listedIds.add(r.path("roomId").asText());
            }
            assertTrue(listedIds.containsAll(roomIds), "Danh sách phòng phải chứa đủ toàn bộ các phòng đang chờ");
            observer.close();

            // 3. 5 khách vào đúng 5 phòng tương ứng
            List<String> orderedIds = new ArrayList<>(roomIds);
            for (int i = 0; i < roomCount; i++) {
                TestClientSocket guest = new TestClientSocket("127.0.0.1", TEST_PORT, "guest_" + runId + "_" + i);
                guest.registerAndLogin("123456");
                ObjectNode joinReq = Json.object("JOIN_ROOM");
                joinReq.put("roomId", orderedIds.get(i));
                guest.send(joinReq);

                ObjectNode joined = guest.waitForType("ROOM_JOINED", 3000);
                assertEquals(orderedIds.get(i), Json.str(joined, "roomId"));
                guests.add(guest);
            }
        } finally {
            hosts.forEach(TestClientSocket::close);
            guests.forEach(TestClientSocket::close);
        }
    }
}