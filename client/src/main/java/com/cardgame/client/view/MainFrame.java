package com.cardgame.client.view;


import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import static com.cardgame.client.view.UiTheme.*;
import com.cardgame.client.model.Session;
import com.cardgame.client.network.NetworkClient;
import com.cardgame.client.model.RoomInfo;
import com.cardgame.client.view.components.CardTile;
import com.cardgame.client.view.components.RoundedPanel;
import com.cardgame.client.view.components.VectorIcon;
import com.cardgame.client.view.components.IconPainter;
import com.cardgame.client.view.dialogs.ChangePasswordDialog;
import com.cardgame.client.view.dialogs.CardPurchaseDialog;
import com.cardgame.client.view.dialogs.CardUseDialog;
import com.cardgame.client.view.dialogs.OrderChoiceDialog;
import com.cardgame.client.view.dialogs.RpsDialog;
import com.cardgame.client.view.dialogs.MatchResultDialog;
import com.cardgame.client.view.dialogs.MatchCancelledDialog;
import com.cardgame.client.view.dialogs.SoloResultDialog;
import com.cardgame.client.util.Json;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.Locale;
public class MainFrame {
    // Shared palette and symbol names are defined in UiTheme.
    static final int PVP_BOARD_SIZE = 36;
    // =====================================================
    // VARIABLES
    // =====================================================
    JFrame frame;
    CardLayout layout;
    JPanel root;
    NetworkClient networkClient;
    JButton loginButton;
    JButton registerButton;
    final Session session = new Session();
    boolean myTurn = false;
    final JButton[] cardButtons = new JButton[PVP_BOARD_SIZE];
    final boolean[] revealed = new boolean[PVP_BOARD_SIZE];
    final boolean[] matched = new boolean[PVP_BOARD_SIZE];
    final boolean[] locked = new boolean[PVP_BOARD_SIZE];
    final boolean[] privatePeek = new boolean[PVP_BOARD_SIZE];
    final java.util.Set<String> availableCardCodes = new java.util.LinkedHashSet<>();
    final java.util.Set<String> usedCardCodes = new java.util.HashSet<>();
    String cardPromptPhase = "";
    String pendingCardUseCode = "";
    boolean selectingCardTarget = false;
    JPanel ownedCardPanel;
    JButton skipCardButton;
    final java.util.List<String> ownedCardCodes = new java.util.ArrayList<>();
    final Map<String,String> privateCardNames = new HashMap<>();
    final Map<String,String> privateCardDescriptions = new HashMap<>();
    JLabel gameStatus;
    JLabel turnTimerLabel;
    javax.swing.Timer pvpTurnClock;
    long pvpTurnDeadline;
    JLabel playerNamesLabel;
    JLabel scoreLabel;
    JLabel roomLabel;
    JLabel coinLabel;
    JLabel waitingPlayersLabel;
    JButton readyButton;
    JTable collectionTable;
    JTextPane matchLogArea;
    // Solo campaign: one continuous timer across all five levels.
    JPanel soloBoard;
    JLabel soloLevelLabel, soloTimerLabel, soloPairsLabel, soloStatusLabel, soloRecordLabel;
    final JButton[] soloTiles = new JButton[64];
    int[] soloDeck = new int[64];
    boolean[] soloMatched = new boolean[64];
    boolean[] soloEverFlipped = new boolean[64];
    int soloLevel = 1, soloPairsFound = 0, soloPairCount = 2;
    int soloFirst = -1, soloSecond = -1;
    long soloStartedAt = 0L;
    javax.swing.Timer soloClock, soloResolveTimer;
    javax.swing.Timer reconnectTimer;
    boolean soloBusy = false, soloFinished = false, soloAwaitingServer = false;
    long soloPendingElapsed = 0L;
    DefaultListModel<String> roomModel =
    new DefaultListModel<>();
    JList<String> roomList;
    final java.util.List<RoomInfo> availableRooms = new java.util.ArrayList<>();
    JTable historyTable;
    JTable leaderboardTable;
    JButton soloLeaderboardTab, pvpLeaderboardTab;
    boolean showingSoloLeaderboard = false;
    // =====================================================
    // MAIN
    // =====================================================
    private JPanel dataPage(String heading, String description, String type) {
        JPanel panel = DataPagePanel.create(this, heading, description);
        JTable table = (JTable) panel.getClientProperty("dataTable");
        switch (type) {
            case "history" -> historyTable = table;
            case "leaderboard" -> leaderboardTable = table;
            case "collection" -> collectionTable = table;
            default -> throw new IllegalArgumentException("Unknown data page: " + type);
        }
        return panel;
    }
    public void show() {
        frame = new JFrame("Memory Duel");
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent event) {
                int choice = JOptionPane.showConfirmDialog(frame,
                        session.roomId.isBlank()
                                ? "Bạn có chắc muốn thoát Memory Duel?"
                                : "Bạn đang ở trong phòng/trận đấu. Thoát có thể khiến trận bị tính là rời trận. Bạn có chắc không?",
                        "Xác nhận thoát", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) {
                    if (networkClient != null) networkClient.close();
                    frame.dispose();
                    System.exit(0);
                }
            }
        });
        frame.setMinimumSize(new Dimension(950, 720));
        frame.setSize(1180, 800);
        frame.setLocationRelativeTo(null);
        layout = new CardLayout();
        root = new JPanel(layout);
        root.setBackground(BG);
        root.add(LoginPanel.create(this), "login");
        root.add(RegisterPanel.create(this), "register");
        root.add(LobbyPanel.create(this), "lobby");
        root.add(WaitingPanel.create(this), "waiting");
        root.add(PvPGamePanel.create(this), "game");
        root.add(SoloGamePanel.create(this), "solo");
        root.add(dataPage("LỊCH SỬ TRẬN ĐẤU", "Lịch sử những trận đấu gần đây của bạn.", "history"), "history");
        root.add(dataPage("BẢNG XẾP HẠNG", "Thành tích và điểm số của người chơi.", "leaderboard"), "leaderboard");
        root.add(dataPage("BỘ SƯU TẬP THẺ", "Thông tin các thẻ chức năng trong game.", "collection"), "collection");
        frame.setContentPane(root);
        frame.setVisible(true);
        connect();
    }
    // =====================================================
    // COMMON UI
    // =====================================================
    JPanel page() {
        JPanel p = new JPanel(new BorderLayout(0, 24));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(30, 38, 30, 38));
        return p;
    }
    JPanel vertical() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }
    JLabel title(String text, int size) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT);
        l.setFont(new Font("Segoe UI", Font.BOLD, size));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
    JLabel subtitle(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(MUTED);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
    JLabel wrappedLabel(String text, int width) {
        JLabel l = new JLabel(
        "<html><div style='width:" + width
        + "px;'>" + text + "</div></html>"
        );
        l.setForeground(MUTED);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
    JLabel eyebrow(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(PURPLE_DARK);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
    JLabel iconLabel(String text, String icon, int size) {
        JLabel l = new JLabel(text, new VectorIcon(icon, size),
        SwingConstants.LEFT);
        l.setFont(new Font("Segoe UI", Font.BOLD, 19));
        l.setForeground(TEXT);
        l.setIconTextGap(10);
        return l;
    }
    JPanel panel(Color color) {
        RoundedPanel p = new RoundedPanel(color);
        p.setLayout(new BorderLayout(0, 14));
        p.setBorder(new EmptyBorder(24, 26, 24, 26));
        return p;
    }
    JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setForeground(Color.WHITE);
        b.setBackground(PURPLE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(12, 20, 12, 20));
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setBackground(PURPLE_DARK);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                b.setBackground(PURPLE);
            }
        }
        );
        return b;
    }
    JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(PURPLE_DARK);
        b.setBackground(LAVENDER);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(11, 17, 11, 17));
        return b;
    }
    JButton iconButton(String text, String icon) {
        JButton b = secondaryButton(text);
        b.setIcon(new VectorIcon(icon, 18));
        b.setIconTextGap(8);
        return b;
    }
    void setFixedWidth(JComponent c, int width, int height) {
        Dimension d = new Dimension(width, height);
        c.setPreferredSize(d);
        c.setMinimumSize(d);
        c.setMaximumSize(d);
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
    public String currentPage = "login";
    void showPage(String name) {
        currentPage = name;
        layout.show(root, name);
    }
    // =====================================================
    // LOGIN / REGISTER
    // =====================================================
    void showChangePasswordDialog() {
        ChangePasswordDialog.show(frame,
        (oldPassword, newPassword) -> send("CHANGE_PASSWORD", "oldPassword", oldPassword, "newPassword", newPassword),
        this::popup);
    }
    void styleField(JTextField field, String placeholder) {
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT);
        field.setBackground(Color.WHITE);
        setFixedWidth(field, 310, 46);
        field.setBorder(
        BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(BORDER),
        new EmptyBorder(8, 12, 8, 12)
        )
        );
    }
    // =====================================================
    // LOBBY
    // =====================================================
    // =====================================================
    // WAITING ROOM
    // =====================================================
    // =====================================================
    // GAME
    // =====================================================
    // =====================================================
    // SOLO CAMPAIGN
    // =====================================================
    void startSoloCampaign() {
        soloLevel = 1;
        soloPairsFound = 0;
        soloStartedAt = System.currentTimeMillis();
        soloFinished = false;
        soloAwaitingServer = false;
        stopSoloTimers();
        soloClock = new javax.swing.Timer(250, e -> updateSoloClock());
        soloClock.start();
        send("SOLO_START");
        send("SOLO_BEST");
        showPage("solo");
        setupSoloLevel();
    }
    int pairsForLevel(int level) {
        return switch(level) {
            case 1 -> 2;
            case 2 -> 4;
            case 3 -> 8;
            case 4 -> 12;
            default -> 18;
        };
    }
    int[] boardDimensionsForLevel(int level) {
        return switch(level) {
            case 1 -> new int[]{
                2,2
            };
            case 2 -> new int[]{
                2,4
            };
            case 3 -> new int[]{
                4,4
            };
            case 4 -> new int[]{
                4,6
            };
            default -> new int[]{
                6,6
            };
        };
    }
    void setupSoloLevel() {
        soloPairCount = pairsForLevel(soloLevel);
        soloPairsFound = 0;
        soloFirst = soloSecond = -1;
        soloBusy = false;
        soloMatched = new boolean[64];
        soloEverFlipped = new boolean[64];
        soloDeck = new int[64];
        java.util.List<Integer> values = new java.util.ArrayList<>();
        java.util.List<Integer> symbols = new java.util.ArrayList<>();
        for (int i = 0; i < SYMBOL_NAMES.length; i++) symbols.add(i);
        java.util.Collections.shuffle(symbols);
        for (int i = 0; i < soloPairCount; i++) {
            values.add(symbols.get(i));
            values.add(symbols.get(i));
        }
        java.util.Collections.shuffle(values);
        java.util.Arrays.fill(soloDeck, -1);
        for (int i = 0; i < values.size(); i++) soloDeck[i] = values.get(i);
        soloBoard.removeAll();
        int[] dimensions = boardDimensionsForLevel(soloLevel);
        int rows = dimensions[0], cols = dimensions[1];
        soloBoard.setLayout(new GridLayout(rows, cols, 7, 7));
        for (int i = 0; i < values.size(); i++) {
            final int index = i;
            CardTile tile = new CardTile();
            tile.setPreferredSize(new Dimension(70, 76));
            tile.addActionListener(e -> soloFlip(index));
            soloTiles[i] = tile;
            soloBoard.add(tile);
        }
        for (int i = values.size(); i < 64; i++) soloTiles[i] = null;
        soloLevelLabel.setText("LEVEL " + soloLevel + " / 5 · " + rows + "×" + cols);
        soloPairsLabel.setText("0 / " + soloPairCount + " cặp");
        soloStatusLabel.setText("Tìm đủ " + soloPairCount + " cặp để mở cấp tiếp theo.");
        soloBoard.revalidate();
        soloBoard.repaint();
        updateSoloClock();
    }
    void soloFlip(int index) {
        if (soloFinished || soloBusy || soloMatched[index] || soloTiles[index] == null || index == soloFirst) return;
        CardTile tile = (CardTile) soloTiles[index];
        soloEverFlipped[index] = true;
        tile.setEverFlipped(true);
        tile.setSymbolIndex(soloDeck[index]);
        tile.setBackground(new Color(255,255,255));
        tile.repaint();
        if (soloFirst < 0) {
            soloFirst = index;
            return;
        }
        soloSecond = index;
        soloBusy = true;
        if (soloDeck[soloFirst] == soloDeck[soloSecond]) {
            soloMatched[soloFirst] = soloMatched[soloSecond] = true;
            soloPairsFound++;
            soloTiles[soloFirst].setBackground(MINT);
            soloTiles[soloSecond].setBackground(MINT);
            soloFirst = soloSecond = -1;
            soloBusy = false;
            soloPairsLabel.setText(soloPairsFound + " / " + soloPairCount + " cặp");
            if (soloPairsFound == soloPairCount) finishSoloLevel();
        } else {
            soloResolveTimer = new javax.swing.Timer(850, e -> {
                CardTile first = (CardTile) soloTiles[soloFirst];
                CardTile second = (CardTile) soloTiles[soloSecond];
                first.setSymbolIndex(-1);
                second.setSymbolIndex(-1);
                first.setEverFlipped(soloEverFlipped[soloFirst]);
                second.setEverFlipped(soloEverFlipped[soloSecond]);
                first.setBackground(LAVENDER);
                second.setBackground(LAVENDER);
                first.repaint();
                second.repaint();
                soloFirst = soloSecond = -1;
                soloBusy = false;
            }
            );
            soloResolveTimer.setRepeats(false);
            soloResolveTimer.start();
        }
    }
    void finishSoloLevel() {
        if (soloAwaitingServer) return;
        soloAwaitingServer = true;
        soloPendingElapsed = System.currentTimeMillis() - soloStartedAt;
        if (soloRecordLabel != null) soloRecordLabel.setText("Đang xác nhận kết quả với Server...");
        soloStatusLabel.setText("Đang xác nhận kết quả level...");
        send("SOLO_LEVEL_COMPLETE", "level", soloLevel, "timeMs", (int)Math.min(Integer.MAX_VALUE, soloPendingElapsed));
    }
    void acceptSoloLevel(int acceptedLevel) {
        if (!soloAwaitingServer || acceptedLevel != soloLevel) return;
        soloAwaitingServer = false;
        if (soloRecordLabel != null) soloRecordLabel.setText("Thành tích tốt nhất hiện tại: Level " + soloLevel + " · " + formatSoloTime(soloPendingElapsed));
        if (soloLevel == 5) {
            soloFinished = true;
            stopSoloTimers();

            soloStatusLabel.setText("XUẤT SẮC! Hoàn thành cả 5 level trong "
                    + formatSoloTime(soloPendingElapsed) + ". Tổng thưởng: 40 xu.");

            SoloResultDialog.show(frame, formatSoloTime(soloPendingElapsed), 40);

            showPage("lobby");
            return;
        }
        int next = soloLevel + 1;
        soloStatusLabel.setText(soloLevel == 4
                ? "Hoàn thành Level 4! Bạn đã nhận mốc thưởng +20 xu."
                : "Hoàn thành Level " + soloLevel + "! Chuẩn bị sang cấp tiếp theo...");
        javax.swing.Timer advance = new javax.swing.Timer(1000, e -> {
            soloLevel = next;
            setupSoloLevel();
        });
        advance.setRepeats(false);
        advance.start();
    }
    void updateSoloClock() {
        if (soloTimerLabel != null && soloStartedAt > 0) soloTimerLabel.setText(formatSoloTime(System.currentTimeMillis() - soloStartedAt));
    }
    String formatSoloTime(long millis) {
        long sec = millis / 1000;
        return String.format("%02d:%02d", sec / 60, sec % 60);
    }
    void stopSoloTimers() {
        if (soloClock != null) soloClock.stop();
        if (soloResolveTimer != null) soloResolveTimer.stop();
    }
    // =====================================================
    // HISTORY
    // =====================================================
    // =====================================================
    // LEADERBOARD
    // =====================================================
    void updateLeaderboardTabs() {
        if (soloLeaderboardTab == null || pvpLeaderboardTab == null) return;
        soloLeaderboardTab.setBackground(showingSoloLeaderboard ? PURPLE_DARK : LAVENDER);
        soloLeaderboardTab.setForeground(showingSoloLeaderboard ? Color.WHITE : PURPLE_DARK);
        pvpLeaderboardTab.setBackground(!showingSoloLeaderboard ? PURPLE_DARK : LAVENDER);
        pvpLeaderboardTab.setForeground(!showingSoloLeaderboard ? Color.WHITE : PURPLE_DARK);
    }
    // =====================================================
    // DATA PAGE
    // =====================================================
    // =====================================================
    // TABLE STYLE
    // =====================================================
    void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setForeground(TEXT);
        table.setBackground(WHITE);
        table.setGridColor(new Color(242, 236, 246));
        table.setSelectionBackground(LAVENDER);
        table.setSelectionForeground(TEXT);
        table.setRowHeight(46);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.getTableHeader().setFont(
        new Font("Segoe UI", Font.BOLD, 13)
        );
        table.getTableHeader().setForeground(PURPLE_DARK);
        table.getTableHeader().setBackground(
        new Color(243, 237, 255)
        );
        table.getTableHeader().setPreferredSize(
        new Dimension(0, 46)
        );
        table.getTableHeader().setReorderingAllowed(false);
        DefaultTableCellRenderer renderer =
        new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean selected,
            boolean focus,
            int row,
            int column
            ) {
                JLabel label = (JLabel)
                super.getTableCellRendererComponent(
                table, value, selected, focus, row, column
                );
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setVerticalAlignment(SwingConstants.CENTER);
                label.setBorder(new EmptyBorder(5, 8, 5, 8));
                label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                if (!selected) {
                    label.setBackground(
                    row % 2 == 0
                    ? WHITE
                    : new Color(250, 247, 255)
                    );
                    label.setForeground(TEXT);
                }
                label.setToolTipText(
                value == null ? null : value.toString()
                );
                return label;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel()
            .getColumn(i)
            .setCellRenderer(renderer);
            table.getColumnModel()
            .getColumn(i)
            .setPreferredWidth(160);
        }
        table.setAutoCreateRowSorter(false);
        table.setRowSorter(null);
    }
    // =====================================================
    // TABLE DECORATIONS
    // =====================================================
    void decorateHistory(JTable table) {
        if (table.getColumnCount() == 0) {
            return;
        }
        table.getColumnModel().getColumn(0).setPreferredWidth(180);
        DefaultTableCellRenderer historyRenderer =
        new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean selected,
            boolean focus,
            int row,
            int column
            ) {
                JLabel label = (JLabel)
                super.getTableCellRendererComponent(
                table, value, selected, focus, row, column
                );
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setIcon(new VectorIcon("history", 17));
                label.setIconTextGap(7);
                label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                if (!selected) {
                    label.setBackground(
                    row % 2 == 0
                    ? WHITE
                    : new Color(250, 247, 255)
                    );
                    label.setForeground(TEXT);
                }
                return label;
            }
        };
        table.getColumnModel()
        .getColumn(0)
        .setCellRenderer(historyRenderer);
    }
    void decorateLeaderboard(JTable table) {
        if (table.getColumnCount() == 0) {
            return;
        }
        table.getColumnModel().getColumn(0).setPreferredWidth(250);
        if (table.getColumnCount() > 1) {
            table.getColumnModel().getColumn(1).setPreferredWidth(150);
        }
        if (table.getColumnCount() > 2) {
            table.getColumnModel().getColumn(2).setPreferredWidth(150);
        }
        if (table.getColumnCount() > 3) {
            table.getColumnModel().getColumn(3).setPreferredWidth(150);
        }
        DefaultTableCellRenderer rankRenderer =
        new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean selected,
            boolean focus,
            int row,
            int column
            ) {
                JLabel label = (JLabel)
                super.getTableCellRendererComponent(
                table, value, selected, focus, row, column
                );
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 14));
                if (!selected) {
                    label.setBackground(
                    row % 2 == 0
                    ? WHITE
                    : new Color(250, 247, 255)
                    );
                    label.setForeground(TEXT);
                }
                String icon = switch (row) {
                    case 0 -> "medal-gold";
                    case 1 -> "medal-silver";
                    case 2 -> "medal-bronze";
                    default -> "medal";
                };
                label.setIcon(new VectorIcon(icon, 22));
                label.setIconTextGap(8);
                return label;
            }
        };
        table.getColumnModel()
        .getColumn(0)
        .setCellRenderer(rankRenderer);
    }
    // =====================================================
    // UPDATE TABLE DATA
    // =====================================================
    void updateDataPage(
    String pageName,
    ArrayNode rows,
    String[] keys
    ) {
        JTable table;
        String[] labels;
        if (pageName.equals("history")) {
            table = historyTable;
            labels = new String[]{
                "Đối thủ", "Tỉ số", "Kết quả", "Xu thay đổi", "Thời gian"
            };
        } else {
            table = leaderboardTable;
            labels = pageName.equals("soloLeaderboard")
            ? new String[]{
                "Người chơi", "Level cao nhất", "Thành tích", "Thời gian tốt nhất"
            }
            : new String[]{
                "Người chơi", "Điểm", "Số trận thắng", "Tỷ lệ thắng (%)"
            };
        }
        if (table == null) {
            return;
        }
        DefaultTableModel model = new DefaultTableModel(labels, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        if (rows != null) {
            for (JsonNode element : rows) {
                ObjectNode obj = (ObjectNode) element;
                Object[] row = new Object[keys.length];
                for (int i = 0; i < keys.length; i++) {
                    row[i] = obj.has(keys[i])
                    && !obj.get(keys[i]).isNull()
                    ? (keys[i].equals("winRate")
                        ? String.format(Locale.US, "%.2f", obj.get(keys[i]).asDouble())
                        : obj.get(keys[i]).asText())
                    : "";
                }

                if (pageName.equals("history") && row.length > 3 && row[3] != null) {
                    String coinChange = row[3].toString();

                    try {
                        int coins = Integer.parseInt(coinChange);

                        if (coins > 0) {
                            row[3] = "+" + coins;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
                model.addRow(row);
            }
        }
        table.setModel(model);
        styleTable(table);
        if (pageName.equals("leaderboard")) {
            decorateLeaderboard(table);
            showingSoloLeaderboard = false;
            updateLeaderboardTabs();
        } else if (pageName.equals("soloLeaderboard")) {
            decorateLeaderboard(table);
            showingSoloLeaderboard = true;
            updateLeaderboardTabs();
        } else {
            decorateHistory(table);
        }
        showPage(pageName.equals("history") ? "history" : "leaderboard");
    }
    void updateCollection(ArrayNode rows) {
        if (collectionTable == null) return;
        DefaultTableModel model = new DefaultTableModel(new String[]{
            "Mã thẻ", "Nhóm", "Tên thẻ", "Mô tả", "Giá (xu)"
        }
        , 0) {
            @Override public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        if (rows != null) for (JsonNode el : rows) {
            ObjectNode o = (ObjectNode) el;
            String category=Json.str(o,"category");
            category=category.equals("ATTACK")?"Tấn công":category.equals("SUPPORT")?"Hỗ trợ":"May mắn";
            model.addRow(new Object[]{
                Json.str(o,"code"), category, Json.str(o,"name"), Json.str(o,"description"), Json.integer(o,"price",0)
            }
            );
        }
        collectionTable.setModel(model);
        styleTable(collectionTable);
        collectionTable.setRowHeight(58);
        if(collectionTable.getColumnCount()>3)collectionTable.getColumnModel().getColumn(3).setPreferredWidth(520);
        showPage("collection");
    }
    void startPvpTurnClock(int seconds) {
        if (pvpTurnClock != null) pvpTurnClock.stop();
        int duration = Math.max(1, seconds);
        pvpTurnDeadline = System.currentTimeMillis() + duration * 1000L;
        updatePvpTurnClock();
        pvpTurnClock = new javax.swing.Timer(200, e -> updatePvpTurnClock());
        pvpTurnClock.start();
    }
    void updatePvpTurnClock() {
        if (turnTimerLabel == null || pvpTurnDeadline <= 0) return;
        long remaining = Math.max(0, (pvpTurnDeadline - System.currentTimeMillis() + 999) / 1000);
        turnTimerLabel.setText("Thời gian lượt: " + remaining + "s");
        turnTimerLabel.setForeground(remaining <= 10 ? new Color(200, 45, 55) : PURPLE_DARK);
        if (remaining == 0 && pvpTurnClock != null) pvpTurnClock.stop();
    }
    void stopPvpTurnClock() {
        if (pvpTurnClock != null) pvpTurnClock.stop();
        pvpTurnDeadline = 0;
        if (turnTimerLabel != null) turnTimerLabel.setText("Thời gian lượt: —");
    }
    void appendLog(String message) {
        appendLog(message, TEXT);
    }
    void appendLog(String message, Color color) {
        if (matchLogArea == null) return;
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setForeground(style, color);
        StyleConstants.setFontFamily(style, "Segoe UI");
        StyleConstants.setFontSize(style, 12);
        try {
            matchLogArea.getDocument().insertString(matchLogArea.getDocument().getLength(), "• " + message + "\n", style);
            matchLogArea.setCaretPosition(matchLogArea.getDocument().getLength());
        } catch (BadLocationException ignored) {
        }
    }
    void refreshPrivateCards(ArrayNode cards) {
        ownedCardCodes.clear();
        privateCardNames.clear();
        privateCardDescriptions.clear();
        usedCardCodes.clear();
        if(cards!=null) for(JsonNode el:cards) {
            ObjectNode c=(ObjectNode) el;
            String code=Json.str(c,"code");
            ownedCardCodes.add(code);
            privateCardNames.put(code,Json.str(c,"name"));
            String state=Json.str(c,"category").equals("LUCK")?"Tự động cuối trận":"Chưa dùng";
            privateCardDescriptions.put(code,code+" – "+Json.str(c,"name")+"\n"+Json.str(c,"description")+"\nTrạng thái: "+state+"\n\n");
        }
        renderPrivateCards();
    }
    void renderPrivateCards() {
        if(ownedCardPanel==null)return;
        ownedCardPanel.removeAll();
        if(ownedCardCodes.isEmpty()){
            JLabel empty=subtitle("Bạn chưa chọn thẻ nào.");
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            ownedCardPanel.add(empty);
        }
        for(String code:ownedCardCodes){
            String name=privateCardNames.getOrDefault(code,code);
            boolean eligible=availableCardCodes.contains(code);
            boolean alreadyUsed=usedCardCodes.contains(code);
            String group=code.startsWith("A")?"A · TẤN CÔNG":code.startsWith("S")?"S · HỖ TRỢ":"L · MAY MẮN";
            JPanel card=new JPanel();
            card.setOpaque(true);
            card.setBackground(eligible?new Color(255,250,231):Color.WHITE);
            card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS));
            card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(eligible?new Color(220,180,105):BORDER,eligible?2:1,true),new EmptyBorder(8,9,8,9)));
            JLabel category=new JLabel(group);
            category.setFont(new Font("Segoe UI",Font.BOLD,10));
            category.setForeground(PURPLE_DARK);
            JLabel title=new JLabel(name);
            title.setFont(new Font("Segoe UI",Font.BOLD,13));
            title.setForeground(TEXT);
            String state=eligible?"Có thể dùng":(code.startsWith("L")||code.equals("S02")?"Tự động":alreadyUsed?"Đã dùng":"Chờ điều kiện");
            JLabel detail=new JLabel(code+" · "+state);
            detail.setFont(new Font("Segoe UI",Font.PLAIN,10));
            detail.setForeground(MUTED);
            card.add(category);
            card.add(Box.createVerticalStrut(3));
            card.add(title);
            card.add(Box.createVerticalStrut(3));
            card.add(detail);
            card.setToolTipText(privateCardDescriptions.getOrDefault(code,name));
            if(eligible){
                JButton use=primaryButton("DÙNG THẺ");
                use.setAlignmentX(Component.LEFT_ALIGNMENT);
                setFixedWidth(use,190,34);
                use.addActionListener(e->useOwnedCard(code));
                card.add(Box.createVerticalStrut(7));
                card.add(use);
            }
            card.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.setMaximumSize(new Dimension(220,eligible?116:82));
            ownedCardPanel.add(card);
            ownedCardPanel.add(Box.createVerticalStrut(7));
        }
        if(skipCardButton!=null){
            boolean endOptions=cardPromptPhase.equals("END_TURN")&&!availableCardCodes.isEmpty();
            skipCardButton.setVisible(endOptions);
            skipCardButton.setText("BỎ QUA THẺ");
        }
        ownedCardPanel.revalidate();
        ownedCardPanel.repaint();
    }
    // =====================================================
    // NETWORK CONNECTION
    // =====================================================
    void connect() {
        if (networkClient != null) {
            networkClient.close();
        }

        String host = System.getProperty("cardgame.host",
                System.getenv().getOrDefault("CARDGAME_HOST", "10.199.133.77"));

        int port = Integer.getInteger("cardgame.port",
                Integer.parseInt(System.getenv().getOrDefault("CARDGAME_PORT", "5000")));

        networkClient = new NetworkClient(host, port,
                message -> SwingUtilities.invokeLater(() -> handle(message)),
                error -> SwingUtilities.invokeLater(() -> {
                    if (loginButton != null) loginButton.setEnabled(true);
                    if (registerButton != null) registerButton.setEnabled(true);

                    session.clear();
                    stopPvpTurnClock();
                    stopSoloTimers();

                    showPage("login");
                    popup(error);
                })
        );

        networkClient.connect();

        if (reconnectTimer == null) {
            reconnectTimer = new javax.swing.Timer(3000, e -> {
                if (networkClient != null && !networkClient.isConnected()) {
                    networkClient.connect();
                }
            });

            reconnectTimer.start();
        }
    }
    void handleSessionReplaced(String message) {
        session.clear();
        stopPvpTurnClock();
        RpsDialog.closeActive();
        OrderChoiceDialog.closeActive();
        CardPurchaseDialog.closeActive();
        CardUseDialog.closeActive();
        if (loginButton != null) loginButton.setEnabled(true);
        if (registerButton != null) registerButton.setEnabled(true);
        LoginPanel.clearFields();
        RegisterPanel.clearFields();
        showPage("login");
        popup(message == null || message.isBlank()
                ? "Tài khoản đã được đăng nhập trên thiết bị khác. Vui lòng đăng nhập lại."
                : message + " Vui lòng đăng nhập lại.");
        connect();
    }
    public void send(String type, Object... pairs) {
        if (networkClient == null || !networkClient.isConnected()) {
            popup("Chưa kết nối Server.");
            if (loginButton != null) loginButton.setEnabled(true);
            if (registerButton != null) registerButton.setEnabled(true);
            return;
        }
        try {
            networkClient.send(type, pairs);
        } catch (IOException e) {
            if (loginButton != null) loginButton.setEnabled(true);
            if (registerButton != null) registerButton.setEnabled(true);
            popup("Không gửi được yêu cầu: " + e.getMessage());
        }
    }
    // =====================================================
    // HANDLE SERVER MESSAGES
    // =====================================================
    void handle(ObjectNode message) {
        ServerMessageHandler.handle(this, message);
    }
    void setTurnIndicator(boolean ownTurn) {
        if (gameStatus == null) return;
        gameStatus.setText(ownTurn ? "Lượt của bạn" : "Lượt đối thủ");
        gameStatus.setForeground(ownTurn ? new Color(35, 145, 82) : new Color(205, 67, 67));
        gameStatus.setFont(new Font("Segoe UI", Font.BOLD, 15));
    }
    String scoreForMe(int score1, int score2, ObjectNode message) {
        String p1 = Json.str(message, "player1");
        return session.username.equals(p1) ? score1 + "   —   " + score2 : score2 + "   —   " + score1;
    }
    void refreshBoardEnabled() {
        int openCount=0;
        for(int i=0;i<PVP_BOARD_SIZE;i++)if(revealed[i]&&!privatePeek[i])openCount++;
        boolean waitingEndTurnChoice=cardPromptPhase.equals("END_TURN")&&!availableCardCodes.isEmpty();
        for(int i=0;i<PVP_BOARD_SIZE;i++)cardButtons[i].setEnabled(selectingCardTarget ? (!matched[i]&&!revealed[i]&&!locked[i]) : (myTurn&&!waitingEndTurnChoice&&!matched[i]&&!revealed[i]&&!locked[i]&&openCount<2));
    }
    void showCardPurchaseDialog(ArrayNode cards) {
        CardPurchaseDialog.show(frame, session.coins, cards,
            selected -> send("BUY_SELECTION", "codes", selected),
            selected -> {
                send("BUY_SELECTION", "codes", selected);
                send("BUY_CONFIRM");
            }
        );
    }
    void useOwnedCard(String code) {
        if(!availableCardCodes.contains(code)) return;
        if(code.equals("A02")||code.equals("S01")){
            if (!CardUseDialog.confirmTargetCard(frame, code)) return;
            selectingCardTarget=true;
            pendingCardUseCode=code;
            availableCardCodes.clear();
            renderPrivateCards();
            setTurnIndicator(true);
            refreshBoardEnabled();
            return;
        }
        if (CardUseDialog.confirmUse(frame, privateCardNames.getOrDefault(code, code))) {
            availableCardCodes.clear();
            myTurn=false;
            renderPrivateCards();
            refreshBoardEnabled();
            send("USE_CARD", "code", code, "index", -1);
        }
    }
    void showOrderChoiceDialog() {
        OrderChoiceDialog.show(frame, order -> send("CHOOSE_ORDER", "order", order));
    }
    void showRpsChoiceDialog(boolean tie) {
        RpsDialog.show(frame, tie, choice -> send("RPS_CHOICE", "choice", choice));
    }
    // =====================================================
    // POPUP
    // =====================================================
    void popup(String message) {
        JOptionPane pane = new JOptionPane(
                message,
                JOptionPane.INFORMATION_MESSAGE
        );

        JDialog dialog = pane.createDialog(frame, "Memory Duel");
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
        dialog.dispose();
    }

}
