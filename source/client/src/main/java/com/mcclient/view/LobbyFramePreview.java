
package com.mcclient.view;

import java.time.LocalDateTime;
import java.util.List;

import javax.swing.SwingUtilities;

import com.mcclient.model.RoomInfo;
import com.mcclient.model.RoomPlayer;

public class LobbyFramePreview {

    private static RoomPlayer player(long id, String username) {
        RoomPlayer p = new RoomPlayer();
        p.setUserId(id);
        p.setUsername(username);
        return p;
    }

    private static RoomInfo createRoom(
            String id,
            long hostId,
            String hostName,
            String status,
            List<RoomPlayer> players) {

        RoomInfo room = new RoomInfo(
                id,
                hostId,
                status,
                LocalDateTime.now(),
                players.size()
        );

        room.setHostName(hostName);
        room.setMaxPlayers(2);
        room.setPlayers(players);

        return room;
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            long myId = 1;

            // Tạo dữ liệu giả
            RoomInfo room1 = createRoom(
                    "R001",
                    1,
                    "alice",
                    "WAITING",
                    List.of(player(1, "alice"))
            );

            RoomInfo room2 = createRoom(
                    "R002",
                    3,
                    "charlie",
                    "FULL",
                    List.of(
                            player(3, "charlie"),
                            player(4, "david")
                    )
            );

            LobbyFrame frame = new LobbyFrame("alice");

            // ===== KIỂM TRA 1: GIAO DIỆN SẢNH CHỜ =====
            frame.showRooms(List.of(room1, room2));

            // ===== KIỂM TRA 2: GIAO DIỆN PHÒNG CHỜ =====
            // Bỏ comment dòng dưới nếu muốn kiểm tra màn hình phòng.
            frame.showRoom(room1, myId);

            frame.setVisible(true);
        });
    }
}