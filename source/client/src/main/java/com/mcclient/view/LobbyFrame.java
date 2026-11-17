package com.mcclient.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import com.mcclient.model.RoomInfo;
import com.mcclient.model.RoomPlayer;

/**
 * View thuần: không biết gì về socket. Controller gắn hành vi qua các setter
 * on*.
 */
public class LobbyFrame extends JFrame {
    private static final String CARD_LOBBY = "LOBBY";
    private static final String CARD_ROOM = "ROOM";

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    // lobby card
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[] { "Mã phòng", "Chủ phòng", "Người chơi", "Trạng thái" }, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JButton btnCreate = new JButton("Tạo phòng");
    private final JButton btnJoin = new JButton("Vào phòng");
    private final JButton btnRefresh = new JButton("Làm mới");
    private final JLabel lblStatus = new JLabel(" ");
    private List<RoomInfo> shownRooms = List.of();

    // room card
    private final JLabel lblRoomTitle = new JLabel("", SwingConstants.CENTER);
    private final JLabel lblSlot1 = new JLabel("", SwingConstants.CENTER);
    private final JLabel lblSlot2 = new JLabel("", SwingConstants.CENTER);
    private final JLabel lblRoomHint = new JLabel("", SwingConstants.CENTER);
    private final JButton btnLeave = new JButton("Rời phòng");

    private Runnable onCreate = () -> {
    };
    private Runnable onRefresh = () -> {
    };
    private Runnable onLeave = () -> {
    };
    private Consumer<String> onJoin = id -> {
    };

    public LobbyFrame(String username) {
        super("Memory Lane - Sảnh chờ");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(640, 440);
        setLocationRelativeTo(null);

        root.add(buildLobbyCard(username), CARD_LOBBY);
        root.add(buildRoomCard(), CARD_ROOM);
        setContentPane(root);

        btnCreate.addActionListener(e -> onCreate.run());
        btnRefresh.addActionListener(e -> onRefresh.run());
        btnLeave.addActionListener(e -> onLeave.run());
        btnJoin.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                showMessage("Hãy chọn một phòng trong danh sách");
                return;
            }
            onJoin.accept(shownRooms.get(row).getId());
        });
    }

    private JPanel buildLobbyCard(String username) {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JLabel title = new JLabel("Sảnh chờ - xin chào, " + username);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(26);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.add(btnCreate);
        buttons.add(btnJoin);
        buttons.add(btnRefresh);
        JPanel south = new JPanel(new BorderLayout(0, 6));
        south.add(buttons, BorderLayout.NORTH);
        south.add(lblStatus, BorderLayout.SOUTH);
        p.add(title, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildRoomCard() {
        JPanel p = new JPanel(new BorderLayout(8, 16));
        p.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        lblRoomTitle.setFont(lblRoomTitle.getFont().deriveFont(Font.BOLD, 22f));
        JPanel slots = new JPanel(new GridLayout(2, 1, 0, 12));
        for (JLabel l : new JLabel[] { lblSlot1, lblSlot2 }) {
            l.setFont(l.getFont().deriveFont(16f));
            l.setBorder(BorderFactory.createLineBorder(java.awt.Color.GRAY));
            l.setPreferredSize(new Dimension(300, 56));
            slots.add(l);
        }
        JPanel south = new JPanel(new GridLayout(2, 1, 0, 8));
        south.add(lblRoomHint);
        JPanel b = new JPanel();
        b.add(btnLeave);
        south.add(b);
        p.add(lblRoomTitle, BorderLayout.NORTH);
        p.add(slots, BorderLayout.CENTER);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    // ---- API cho controller (gọi trên EDT) ----
    public void showRooms(List<RoomInfo> rooms) {
        shownRooms = List.copyOf(rooms);
        tableModel.setRowCount(0);
        for (RoomInfo r : rooms) {
            tableModel.addRow(new Object[] { r.getId(), r.getHostName(),
                    r.getPlayerCount() + "/" + r.getMaxPlayers(), r.getStatus() });
        }
        lblStatus.setText(rooms.size() + " phòng");
    }

    public void showRoom(RoomInfo room, long myId) {
        lblRoomTitle.setText("Phòng " + room.getId());
        List<RoomPlayer> ps = room.getPlayers();
        lblSlot1.setText(slotText(ps, 0, room.getHostId(), myId));
        lblSlot2.setText(slotText(ps, 1, room.getHostId(), myId));
        lblRoomHint.setText(room.getPlayerCount() < room.getMaxPlayers()
                ? "Đang chờ đối thủ..."
                : "Đã đủ 2 người - sẵn sàng!");
        cards.show(root, CARD_ROOM);
    }

    private String slotText(List<RoomPlayer> ps, int i, long hostId, long myId) {
        if (i >= ps.size())
            return "(trống)";
        RoomPlayer p = ps.get(i);
        return p.getUsername() + (p.getUserId() == hostId ? "  [Chủ phòng]" : "")
                + (p.getUserId() == myId ? "  (Bạn)" : "");
    }

    public void showLobby() {
        cards.show(root, CARD_LOBBY);
    }

    public void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    public void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public void setBusy(boolean busy) {
        btnCreate.setEnabled(!busy);
        btnJoin.setEnabled(!busy);
        btnRefresh.setEnabled(!busy);
        btnLeave.setEnabled(!busy);
    }

    public void onCreate(Runnable r) {
        this.onCreate = r;
    }

    public void onRefresh(Runnable r) {
        this.onRefresh = r;
    }

    public void onLeave(Runnable r) {
        this.onLeave = r;
    }

    public void onJoin(Consumer<String> c) {
        this.onJoin = c;
    }
}