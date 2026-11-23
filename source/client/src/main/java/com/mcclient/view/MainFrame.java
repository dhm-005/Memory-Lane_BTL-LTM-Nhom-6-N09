package com.mcclient.view;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mcclient.model.RoomInfo;
import com.mcclient.network.NetworkClient;
import com.mcclient.network.ServerMessageHandler;
import com.mcclient.util.JsonTemplates;
import com.mcclient.util.JsonUtils;
import com.mcclient.view.dialogs.CardPurchaseDialog;
import com.mcclient.view.dialogs.ChangePasswordDialog;
import com.mcclient.view.dialogs.OrderChoiceDialog;
import com.mcclient.view.dialogs.RpsDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mcclient.view.UITheme.*;

public class MainFrame {

    // =====================================================
    // STATE & REFERENCES
    // =====================================================
    public JFrame frame;
    CardLayout layout;
    JPanel root;

    public JButton loginButton;
    public JButton registerButton;

    public String username = "";
    public String roomId = "";
    public boolean loggedIn = false;
    public int coins = 50;
    public int rankingPoints = 0;

    public JLabel roomLabel;
    public JLabel coinLabel;
    public JLabel waitingPlayersLabel;
    public JButton readyButton;

    public final DefaultListModel<String> roomModel = new DefaultListModel<>();
    JList<String> roomList;
    public final List<RoomInfo> availableRooms = new ArrayList<>();

    JTable historyTable;
    JTable leaderboardTable;
    JTable collectionTable;
    JButton soloLeaderboardTab, pvpLeaderboardTab;
    boolean showingSoloLeaderboard = false;

    public SoloGamePanel soloPanel;
    public PvPGamePanel pvpPanel;

    // =====================================================
    // MAIN
    // =====================================================
    public void show() {
        frame = new JFrame("Memory Duel | Solo & 1v1");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(950, 720));
        frame.setSize(1180, 800);
        frame.setLocationRelativeTo(null);

        layout = new CardLayout();
        root = new JPanel(layout);
        root.setBackground(BG);

        pvpPanel = new PvPGamePanel(this);
        soloPanel = new SoloGamePanel(this);

        root.add(LoginPanel.create(this), "login");
        root.add(RegisterPanel.create(this), "register");
        root.add(LobbyPanel.create(this), "lobby");
        root.add(WaitingPanel.create(this), "waiting");
        root.add(pvpPanel, "game");
        root.add(soloPanel, "solo");
        root.add(HistoryPanel.create(this), "history");
        root.add(LeaderboardPanel.create(this), "leaderboard");
        root.add(CollectionPanel.create(this), "collection");

        frame.setContentPane(root);
        frame.setVisible(true);

        connect();
    }

    public void showPage(String name) {
        layout.show(root, name);
    }

    // =====================================================
    // DATA PAGES
    // =====================================================
    void updateLeaderboardTabs() {
        if (soloLeaderboardTab == null || pvpLeaderboardTab == null) return;
        soloLeaderboardTab.setBackground(showingSoloLeaderboard ? PURPLE_DARK : LAVENDER);
        soloLeaderboardTab.setForeground(showingSoloLeaderboard ? Color.WHITE : PURPLE_DARK);
        pvpLeaderboardTab.setBackground(!showingSoloLeaderboard ? PURPLE_DARK : LAVENDER);
        pvpLeaderboardTab.setForeground(!showingSoloLeaderboard ? Color.WHITE : PURPLE_DARK);
    }

    public void updateDataPage(String pageName, ArrayNode rows, String[] keys) {
        JTable table;
        String[] labels;

        if (pageName.equals("history")) {
            table = historyTable;
            labels = new String[]{"Đối thủ", "Tỉ số", "Kết quả", "Xu thay đổi", "Thời gian"};
        } else {
            table = leaderboardTable;
            labels = pageName.equals("soloLeaderboard")
                    ? new String[]{"Người chơi", "Level cao nhất", "Thành tích", "Thời gian tốt nhất"}
                    : new String[]{"Người chơi", "Điểm", "Số trận thắng", "Tỷ lệ thắng (%)"};
        }
        if (table == null) return;

        DefaultTableModel model = new DefaultTableModel(labels, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        if (rows != null) {
            for (JsonNode obj : rows) {
                Object[] row = new Object[keys.length];
                for (int i = 0; i < keys.length; i++) {
                    row[i] = obj.hasNonNull(keys[i]) ? obj.get(keys[i]).asText() : "";
                }
                model.addRow(row);
            }
        }

        table.setModel(model);
        CommonUI.styleTable(table);

        if (pageName.equals("leaderboard")) {
            CommonUI.decorateLeaderboard(table);
            showingSoloLeaderboard = false;
            updateLeaderboardTabs();
        } else if (pageName.equals("soloLeaderboard")) {
            CommonUI.decorateLeaderboard(table);
            showingSoloLeaderboard = true;
            updateLeaderboardTabs();
        } else {
            CommonUI.decorateHistory(table);
        }

        showPage(pageName.equals("history") ? "history" : "leaderboard");
    }

    public void updateCollection(ArrayNode rows) {
        if (collectionTable == null) return;
        DefaultTableModel model = new DefaultTableModel(new String[]{"Mã thẻ", "Nhóm", "Tên thẻ", "Mô tả", "Giá (xu)"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        if (rows != null) {
            for (JsonNode o : rows) {
                String category = JsonUtils.getString(o, "category");
                category = category.equals("ATTACK") ? "Tấn công" : category.equals("SUPPORT") ? "Hỗ trợ" : "May mắn";
                model.addRow(new Object[]{
                        JsonUtils.getString(o, "code"),
                        category,
                        JsonUtils.getString(o, "name"),
                        JsonUtils.getString(o, "description"),
                        JsonUtils.getInt(o, "price", 0)
                });
            }
        }
        collectionTable.setModel(model);
        CommonUI.styleTable(collectionTable);
        collectionTable.setRowHeight(58);
        if (collectionTable.getColumnCount() > 3) {
            collectionTable.getColumnModel().getColumn(3).setPreferredWidth(520);
        }
        showPage("collection");
    }

    // =====================================================
    // DIALOGS
    // =====================================================
    void showChangePasswordDialog() {
        ChangePasswordDialog.show(frame,
                (oldPassword, newPassword) -> send("CHANGE_PASSWORD", JsonTemplates.changePassword(oldPassword, newPassword)),
                this::popup);
    }

    public void showCardPurchaseDialog(ArrayNode cards) {
        CardPurchaseDialog.show(frame, coins, cards, selected -> {
            send("BUY_SELECTION", JsonTemplates.buySelection(selected));
            send("BUY_CONFIRM", JsonTemplates.empty());
        });
    }

    public void showOrderChoiceDialog() {
        OrderChoiceDialog.show(frame, order -> send("CHOOSE_ORDER", JsonTemplates.chooseOrder(order)));
    }

    public void showRpsChoiceDialog(boolean tie) {
        RpsDialog.show(frame, tie, choice -> send("RPS_CHOICE", JsonTemplates.rpsChoice(choice)));
    }

    // =====================================================
    // NETWORK CONNECTION
    // =====================================================
    void connect() {
        String host = System.getProperty("cardgame.host", "127.0.0.1");
        int port = Integer.getInteger("cardgame.port", 5000);
        NetworkClient client = NetworkClient.getInstance();

        client.addPushListener((type, data)
                -> SwingUtilities.invokeLater(() -> ServerMessageHandler.handle(this, type, data))
        );

        try {
            client.connect(host, port);
        } catch (IOException e) {
            popup("Không thể kết nối Server: " + e.getMessage());
        }
    }

    public void send(String type, Map<String, Object> data) {
        NetworkClient.getInstance().request(type, data)
                .whenComplete((resData, err) -> SwingUtilities.invokeLater(() -> {
                    if (loginButton != null) loginButton.setEnabled(true);
                    if (registerButton != null) registerButton.setEnabled(true);
                    if (err != null) {
                        Throwable cause = err.getCause() != null ? err.getCause() : err;
                        popup(cause.getMessage());
                    } else if (resData != null) {
                        ServerMessageHandler.handle(this, type, resData);
                    }
                }));
    }

    public void popup(String message) {
        JOptionPane.showMessageDialog(frame, message, "Memory Duel", JOptionPane.INFORMATION_MESSAGE);
    }
}