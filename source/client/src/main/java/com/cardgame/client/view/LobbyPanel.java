package com.cardgame.client.view;

import com.cardgame.client.view.components.CardTile;
import com.cardgame.client.view.components.RoundedPanel;
import com.cardgame.client.view.components.VectorIcon;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import static com.cardgame.client.view.MainFrame.*;
import static com.cardgame.client.view.UiTheme.*;
public final class LobbyPanel {
    private LobbyPanel() {
    }
    public static JPanel create(MainFrame owner) {
        JPanel p = owner.page();
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JPanel brand = owner.vertical();
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.add(owner.title("MEMORY DUEL", 32));
        brand.add(owner.subtitle("Sẵn sàng cho một trận đấu trí vui nhộn?"));

        // WrapLayout: các nút tự xuống hàng khi cửa sổ hẹp, không đè lên nhau
        JPanel actions = new JPanel(
                new InlineWrapLayout(FlowLayout.RIGHT, 8, 4)
        );
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);

        owner.coinLabel = new JLabel("Xu: " + owner.session.coins);
        owner.coinLabel.setForeground(PURPLE_DARK);
        owner.coinLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JButton history = owner.iconButton("Lịch sử", "history");
        JButton rank = owner.iconButton("Bảng xếp hạng", "trophy");
        JButton collection = owner.iconButton("Bộ sưu tập thẻ", "playing-card-small");
        JButton changePassword = owner.secondaryButton("Đổi mật khẩu");
        JButton logout = owner.secondaryButton("Đăng xuất");
        JButton solo = owner.primaryButton("CHƠI 1 NGƯỜI");

        actions.add(owner.coinLabel);
        actions.add(history);
        actions.add(rank);
        actions.add(collection);
        actions.add(changePassword);
        actions.add(logout);

        top.add(brand);
        top.add(actions);
        actions.addComponentListener(new ComponentAdapter() {
            private int lastWidth = -1;
            @Override public void componentResized(ComponentEvent e) {
                if (actions.getWidth() != lastWidth) {
                    lastWidth = actions.getWidth();
                    actions.revalidate();
                }
            }
        });

        p.add(top, BorderLayout.NORTH);
        // WELCOME
        JPanel welcome = owner.panel(WHITE);
        JPanel welcomeText = owner.vertical();
        welcomeText.add(owner.eyebrow("KHU VỰC NGƯỜI CHƠI"));
        welcomeText.add(Box.createVerticalStrut(8));
        welcomeText.add(owner.title("Xin chào, game thủ!", 25));
        welcomeText.add(Box.createVerticalStrut(5));
        welcomeText.add(owner.wrappedLabel(
        "Tạo phòng mới hoặc tham gia một phòng "
        + "chơi để bắt đầu trận đấu.",
        500
        ));
        welcome.add(welcomeText, BorderLayout.CENTER);
        JLabel decoration = new JLabel(
        new VectorIcon("flower-star", 78)
        );
        welcome.add(decoration, BorderLayout.EAST);
        // CENTER
        // Bề rộng tối thiểu nhỏ để trang có thể co lại và tự xếp chồng hai khung khi cửa sổ hẹp
        JPanel center = new JPanel(new GridLayout(1, 2, 20, 0)) {
            @Override public Dimension getMinimumSize() {
                Dimension d = super.getMinimumSize();
                return new Dimension(Math.min(d.width, 280), d.height);
            }
        };
        center.setOpaque(false);
        // CREATE ROOM
        JPanel create = owner.panel(LAVENDER);
        JPanel createText = owner.vertical();
        JLabel target = new JLabel(new VectorIcon("target", 42));
        target.setAlignmentX(Component.LEFT_ALIGNMENT);
        createText.add(target);
        createText.add(Box.createVerticalStrut(8));
        createText.add(owner.title("Tạo trận mới", 23));
        createText.add(Box.createVerticalStrut(8));
        createText.add(owner.wrappedLabel(
        "Mở một phòng chơi và chờ bạn bè "
        + "tham gia để bắt đầu trận đấu.",
        350
        ));
        JButton createBtn = owner.primaryButton("TẠO PHÒNG");
        createBtn.setIcon(new VectorIcon("sparkle", 18));
        createBtn.setIconTextGap(9);
        create.add(createText, BorderLayout.CENTER);
        create.add(createBtn, BorderLayout.SOUTH);
        // JOIN ROOM
        JPanel join = owner.panel(WHITE);
        JPanel joinHeader = new JPanel(new BorderLayout());
        joinHeader.setOpaque(false);
        joinHeader.add(owner.title("Phòng đang chờ", 21), BorderLayout.CENTER);
        JButton refresh = owner.iconButton("LÀM MỚI", "refresh");
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
            public Component getListCellRendererComponent(
            JList<?> list,
            Object value,
            int index,
            boolean selected,
            boolean focus
            ) {
                JLabel c = (JLabel) super.getListCellRendererComponent(
                list, value, index, selected, focus
                );
                c.setBorder(new EmptyBorder(10, 12, 10, 12));
                c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                c.setForeground(TEXT);
                c.setIcon(new VectorIcon("playing-card-small", 20));
                c.setIconTextGap(10);
                c.setBackground(selected
                ? LAVENDER
                : new Color(250, 248, 255));
                return c;
            }
        }
        );
        JScrollPane scroll = new JScrollPane(owner.roomList);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.setMinimumSize(new Dimension(200, 120));
        JButton joinBtn = owner.primaryButton("THAM GIA PHÒNG");
        join.add(joinHeader, BorderLayout.NORTH);
        join.add(scroll, BorderLayout.CENTER);
        join.add(joinBtn, BorderLayout.SOUTH);
        center.add(create);
        center.add(join);
        // Cửa sổ hẹp: xếp hai khung theo chiều dọc thay vì ép sát nhau
        center.addComponentListener(new ComponentAdapter() {
            private boolean stacked = false;
            @Override public void componentResized(ComponentEvent e) {
                boolean shouldStack = center.getWidth() < 760;
                if (shouldStack != stacked) {
                    stacked = shouldStack;
                    center.setLayout(shouldStack ? new GridLayout(2, 1, 0, 16) : new GridLayout(1, 2, 20, 0));
                    center.revalidate();
                }
            }
        });
        JPanel middle = new JPanel(new BorderLayout(0, 16));
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
        createBtn.addActionListener(e -> owner.send("CREATE_ROOM"));
        refresh.addActionListener(e -> owner.send("LIST_ROOMS"));
        joinBtn.addActionListener(e -> {
            int row = owner.roomList.getSelectedIndex();
            String id = row >= 0 && row < owner.availableRooms.size() ? owner.availableRooms.get(row).roomId() : null;
            if (id == null) id = JOptionPane.showInputDialog(owner.frame,"Nhập mã phòng:","Vào phòng",JOptionPane.QUESTION_MESSAGE);
            if (id != null && id.trim().matches("\\d{4}")) owner.send("JOIN_ROOM","roomId",id.trim());
            else if (id != null) owner.popup("Mã phòng gồm 4 chữ số.");
        }
        );
        solo.addActionListener(e -> owner.startSoloCampaign());
        history.addActionListener(e -> owner.send("HISTORY"));
        rank.addActionListener(e -> {
            owner.showingSoloLeaderboard = false; owner.updateLeaderboardTabs(); owner.send("LEADERBOARD");
        }
        );
        collection.addActionListener(e -> owner.send("CARD_COLLECTION"));
        changePassword.addActionListener(e -> owner.showChangePasswordDialog());
        logout.addActionListener(e -> {
            owner.send("LOGOUT");
            LoginPanel.clearFields();
            RegisterPanel.clearFields();
            owner.session.loggedIn = false;
            owner.session.username = "";
            owner.showPage("login");
        }
        );
        return p;
    }


    // Helpers embedded to preserve the original file count.
    /** FlowLayout tự xuống hàng khi hết chỗ và báo đúng chiều cao cho khung chứa. */
    private static class InlineWrapLayout extends FlowLayout {
        public InlineWrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            // Bề rộng tối thiểu chỉ là thành phần rộng nhất (không ép cả hàng nằm ngang),
            // chiều cao tối thiểu tính theo số hàng sau khi xuống dòng.
            Dimension min = layoutSize(target, false);
            int widest = 0;
            for (int i = 0; i < target.getComponentCount(); i++) {
                Component m = target.getComponent(i);
                if (m.isVisible()) widest = Math.max(widest, m.getMinimumSize().width);
            }
            Insets in = target.getInsets();
            min.width = widest + in.left + in.right + getHgap() * 2;
            return min;
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                Container c = target;
                while (c.getSize().width == 0 && c.getParent() != null) c = c.getParent();
                int targetWidth = c.getSize().width;
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;

                int hgap = getHgap(), vgap = getVgap();
                Insets insets = target.getInsets();
                int horizontal = insets.left + insets.right + hgap * 2;
                int maxWidth = targetWidth - horizontal;

                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0, rowHeight = 0;
                for (int i = 0; i < target.getComponentCount(); i++) {
                    Component m = target.getComponent(i);
                    if (!m.isVisible()) continue;
                    Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                    if (rowWidth + d.width > maxWidth && rowWidth > 0) {
                        addRow(dim, rowWidth, rowHeight);
                        rowWidth = 0;
                        rowHeight = 0;
                    }
                    if (rowWidth != 0) rowWidth += hgap;
                    rowWidth += d.width;
                    rowHeight = Math.max(rowHeight, d.height);
                }
                addRow(dim, rowWidth, rowHeight);
                dim.width += horizontal;
                dim.height += insets.top + insets.bottom + vgap * 2;
                if (SwingUtilities.getAncestorOfClass(JScrollPane.class, target) != null && target.isValid()) {
                    dim.width -= hgap + 1;
                }
                return dim;
            }
        }

        private void addRow(Dimension dim, int rowWidth, int rowHeight) {
            dim.width = Math.max(dim.width, rowWidth);
            if (dim.height > 0) dim.height += getVgap();
            dim.height += rowHeight;
        }
    }

}
