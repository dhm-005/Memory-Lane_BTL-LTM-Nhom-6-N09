package com.mcserver.service.room;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.mcserver.dto.response.RoomDTO;
import com.mcserver.dto.response.RoomUpdateEvent;

class RoomServiceTest {
    private final List<RoomUpdateEvent> events = Collections.synchronizedList(new ArrayList<>());
    private final RoomService service = new RoomService(events::add);

    private static String codeOf(Runnable r) {
        try {
            r.run();
            return "OK";
        } catch (RoomException e) {
            return e.getCode();
        }
    }

    private RoomDTO room(String id) {
        return service.listRooms().rooms().stream().filter(r -> r.id().equals(id)).findFirst().orElseThrow();
    }

    @Test
    void thirdPlayerIsRejected() {
        String id = service.createRoom(1, "a").roomId();
        service.joinRoom(id, 2, "b");
        assertEquals("FULL", room(id).status());
        assertEquals("ROOM_FULL", codeOf(() -> service.joinRoom(id, 3, "c")));
    }

    @Test
    void cannotBeInTwoRooms() {
        String id = service.createRoom(1, "a").roomId();
        assertEquals("ALREADY_IN_ROOM", codeOf(() -> service.createRoom(1, "a")));
        assertEquals("ALREADY_IN_ROOM", codeOf(() -> service.joinRoom(id, 1, "a")));
    }

    @Test
    void unknownRoomIsRejected() {
        assertEquals("ROOM_NOT_FOUND", codeOf(() -> service.joinRoom("R999", 1, "a")));
    }

    @Test
    void hostLeavingTransfersHost() {
        String id = service.createRoom(1, "a").roomId();
        service.joinRoom(id, 2, "b");
        RoomUpdateEvent e = service.leaveRoom(1);
        assertEquals("HOST_CHANGED", e.event());
        assertEquals(2, room(id).hostId());
        assertEquals("WAITING", room(id).status());
    }

    @Test
    void lastPlayerLeavingClosesRoom() {
        service.createRoom(1, "a");
        RoomUpdateEvent e = service.leaveRoom(1);
        assertEquals("ROOM_CLOSED", e.event());
        assertTrue(service.listRooms().rooms().isEmpty());
        assertEquals("NOT_IN_ROOM", codeOf(() -> service.leaveRoom(1)));
    }

    @Test
    void cannotJoinPlayingRoom() {
        String id = service.createRoom(1, "a").roomId();
        service.joinRoom(id, 2, "b");
        service.markPlaying(id);
        assertEquals("ROOM_IN_PROGRESS", codeOf(() -> service.joinRoom(id, 3, "c")));
    }

    @Test
    void versionsStrictlyIncrease() {
        String id = service.createRoom(1, "a").roomId();
        service.joinRoom(id, 2, "b");
        service.leaveRoom(2);
        long prev = 0;
        for (RoomUpdateEvent e : events) {
            assertTrue(e.version() > prev);
            prev = e.version();
        }
    }

    @Test
    void concurrentJoinsNeverExceedTwoPlayers() throws Exception {
        String roomId = service.createRoom(1000, "host").roomId();
        int n = 200;
        ExecutorService pool = Executors.newFixedThreadPool(32);
        CountDownLatch go = new CountDownLatch(1);
        AtomicInteger ok = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            long uid = 2000 + i;
            futures.add(pool.submit(() -> {
                go.await();
                try {
                    service.joinRoom(roomId, uid, "u" + uid);
                    ok.incrementAndGet();
                } catch (RoomException ignored) {
                    // ROOM_FULL
                }
                return null;
            }));
        }
        go.countDown();
        for (Future<?> f : futures) {
            f.get();
        }
        pool.shutdown();
        assertEquals(1, ok.get());
        assertEquals(2, room(roomId).playerCount());
    }
}