package com.mcclient.view;

import com.mcclient.util.JsonTemplates;
import com.mcclient.view.components.VectorIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static com.mcclient.view.UITheme.*;

public final class LobbyPanel {
    private LobbyPanel() { }

    public static JPanel create(MainFrame owner) {
        JPanel p = CommonUI.page();

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel brand = CommonUI.vertical();
        brand.add(CommonUI.title("MEMORY DUEL", 28));
        brand.add(CommonUI.subtitle("Sẵn sàng cho một trận đấu trí vui nhộn?"));

        top.add(brand, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actions.setOpaque(false);

        owner.coinLabel = new JLabel("Xu: " + owner.coins);
        owner.coinLabel.setForeground(PURPLE_DARK);
        owner.coinLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JButton history = CommonUI.iconButton("Lịch sử", "history");
        JButton rank = CommonUI.iconButton("Bảng xếp hạng", "trophy");
        JButton collection = CommonUI.iconButton("Bộ sưu tập thẻ", "playing-card-small");
        JButton changePassword = CommonUI.secondaryButton("Đổi mật khẩu");
        JButton logout = CommonUI.secondaryButton("Đăng xuất");
        JButton solo = CommonUI.primaryButton("CHƠI 1 NGƯỜI");

        actions.add(owner.coinLabel);
        actions.add(history);
        actions.add(rank);
        actions.add(collection);
        actions.add(changePassword);
        actions.add(logout);

        top.add(actions, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        // WELCOME
        JPanel welcome = CommonUI.panel(WHITE);

        JPanel welcomeText = CommonUI.vertical();
        welcomeText.add(CommonUI.eyebrow("KHU VỰC NGƯỜI CHƠI"));
        welcomeText.add(Box.createVerticalStrut(8));
        welcomeText.add(CommonUI.title("Xin chào, game thủ!", 25));
        welcomeText.add(Box.createVerticalStrut(5));
        welcomeText.add(CommonUI.wrappedLabel(
                "Tạo phòng mới hoặc tham gia một phòng đang chờ để bắt đầu trận đấu.",
                500
        ));

        welcome.add(welcomeText, BorderLayout.WEST);
        welcome.add(new JLabel(new VectorIcon("flower-star", 78)), BorderLayout.EAST);

        // CENTER
        JPanel center = new JPanel(new GridLayout(1, 2, 20, 0));
        center.setOpaque(false);

        // CREATE ROOM
        JPanel create = CommonUI.panel(LAVENDER);

        JPanel createText = CommonUI.vertical();
        JLabel target = new JLabel(new VectorIcon("target", 42));
        target.setAlignmentX(Component.LEFT_ALIGNMENT);

        createText.add(target);
        createText.add(Box.createVerticalStrut(8));
        createText.add(CommonUI.title("Tạo trận mới", 23));
        createText.add(Box.createVerticalStrut(8));
        createText.add(CommonUI.wrappedLabel(
                "Mở một phòng chơi và chờ bạn bè tham gia để bắt đầu trận đấu.",
                350
        ));

        JButton createBtn = CommonUI.primaryButton("TẠO PHÒNG");
        createBtn.setIcon(new VectorIcon("sparkle", 18));
        createBtn.setIconTextGap(9);

        create.add(createText, BorderLayout.CENTER);
        create.add(createBtn, BorderLayout.SOUTH);

        // JOIN ROOM
        JPanel join = CommonUI.panel(WHITE);

        JPanel joinHeader = new JPanel(new BorderLayout());
        joinHeader.setOpaque(false);
        joinHeader.add(CommonUI.title("Phòng đang chờ", 21), BorderLayout.WEST);

        JButton refresh = CommonUI.iconButton("LÀM MỚI", "refresh");
        joinHeader.add(refresh, BorderLayout.EAST);

        owner.roomModel.clear();
        owner.availableRooms.clear();

        owner.roomList = new JList<>(owner.roomModel);
        owner.roomList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        owner.roomList.setForeground(TEXT);
        owner.roomList.setBackground(new Color(250, 248, 255));
        owner.roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        owner.roomList.setFixedCellHeight(48);
        owner.roomList.setBorder(new EmptyBorder(5, 8, 5, 8));

        owner.roomList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
                JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus);
                c.setBorder(new EmptyBorder(10, 12, 10, 12));
                c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                c.setForeground(TEXT);
                c.setIcon(new VectorIcon("playing-card-small", 20));
                c.setIconTextGap(10);
                c.setBackground(selected ? LAVENDER : new Color(250, 248, 255));
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(owner.roomList);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));

        JButton joinBtn = CommonUI.primaryButton("THAM GIA PHÒNG");

        join.add(joinHeader, BorderLayout.NORTH);
        join.add(scroll, BorderLayout.CENTER);
        join.add(joinBtn, BorderLayout.SOUTH);

        center.add(create);
        center.add(join);

        JPanel middle = new JPanel(new BorderLayout(0, 20));
        middle.setOpaque(false);
        middle.add(welcome, BorderLayout.NORTH);
        middle.add(center, BorderLayout.CENTER);

        JPanel soloBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        soloBar.setOpaque(false);
        solo.setPreferredSize(new Dimension(260, 48));
        soloBar.add(solo);
        middle.add(soloBar, BorderLayout.SOUTH);

        p.add(middle, BorderLayout.CENTER);

        // EVENTS
        createBtn.addActionListener(e -> owner.send("CREATE_ROOM", JsonTemplates.empty()));
        refresh.addActionListener(e -> owner.send("LIST_ROOMS", JsonTemplates.empty()));

        joinBtn.addActionListener(e -> {
            int row = owner.roomList.getSelectedIndex();
            String id = row >= 0 && row < owner.availableRooms.size() ? owner.availableRooms.get(row).getId() : null;
            if (id == null) {
                id = JOptionPane.showInputDialog(owner.frame, "Nhập mã phòng:", "Vào phòng", JOptionPane.QUESTION_MESSAGE);
            }
            if (id != null && id.trim().matches("\\d{4}")) {
                owner.send("JOIN_ROOM", JsonTemplates.joinRoom(id.trim()));
            } else if (id != null) {
                owner.popup("Mã phòng gồm 4 chữ số.");
            }
        });

        solo.addActionListener(e -> owner.soloPanel.startCampaign());
        history.addActionListener(e -> owner.send("HISTORY", JsonTemplates.empty()));
        rank.addActionListener(e -> {
            owner.showingSoloLeaderboard = false;
            owner.updateLeaderboardTabs();
            owner.send("LEADERBOARD", JsonTemplates.empty());
        });
        collection.addActionListener(e -> owner.send("CARD_COLLECTION", JsonTemplates.empty()));
        changePassword.addActionListener(e -> owner.showChangePasswordDialog());

        logout.addActionListener(e -> {
            owner.send("LOGOUT", JsonTemplates.empty());
            owner.loggedIn = false;
            owner.username = "";
            owner.showPage("login");
        });

        return p;
    }
}