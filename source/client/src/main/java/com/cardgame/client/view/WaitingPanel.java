package com.cardgame.client.view;

import com.cardgame.client.view.components.RoundedPanel;
import com.cardgame.client.view.components.VectorIcon;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import static com.cardgame.client.view.UiTheme.*;
import java.awt.geom.RoundRectangle2D;
import java.util.function.BiConsumer;

public final class WaitingPanel {
    private WaitingPanel() {
    }

    /** Holder cuộn dọc khi cửa sổ quá thấp, vẫn giữ nội dung ở giữa khi đủ chỗ. */
    private static final class Holder extends JPanel implements Scrollable {
        Holder() {
            super(new GridBagLayout());
            setOpaque(false);
        }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 18; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 80; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() {
            return !(getParent() instanceof JViewport vp) || vp.getHeight() >= getPreferredSize().height;
        }
    }

    private static JPanel section(Color fill, Color line) {
        RoundedPanel panel = new RoundedPanel(fill) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(line);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 26, 26);
                g2.dispose();
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(12, 18, 14, 18));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        return panel;
    }

    private static void centered(JComponent c) {
        c.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private static void limitButton(MainFrame owner, JButton b, int height) {
        b.setPreferredSize(new Dimension(360, height));
        b.setMinimumSize(new Dimension(0, height));
        b.setMaximumSize(new Dimension(360, height));
        centered(b);
    }

    public static JPanel create(MainFrame owner) {
        JPanel p = new JPanel(null);
        p.setBackground(BG);

        // ===== Khung phòng chờ nổi bật =====
        InlineElevatedCard box = new InlineElevatedCard(WHITE, PURPLE, LAVENDER, 150, 34, 30, 22);
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel(new VectorIcon("hourglass", 52));
        centered(icon);
        JLabel heading = owner.title("PHÒNG CHỜ", 26);
        centered(heading);
        JLabel info = new JLabel("Chia sẻ mã phòng cho bạn bè ngay", SwingConstants.CENTER);
        info.setForeground(PURPLE_DARK);
        info.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        centered(info);

        // Mã phòng: huy hiệu lớn
        JPanel codeBadge = section(LAVENDER, PURPLE);
        JLabel codeEyebrow = owner.eyebrow("MÃ PHÒNG");
        centered(codeEyebrow);
        codeEyebrow.setHorizontalAlignment(SwingConstants.CENTER);
        owner.roomLabel = owner.title("Mã phòng: ----", 31);
        owner.roomLabel.setForeground(PURPLE_DARK);
        centered(owner.roomLabel);
        owner.roomLabel.setHorizontalAlignment(SwingConstants.CENTER);
        codeBadge.add(codeEyebrow);
        codeBadge.add(Box.createVerticalStrut(4));
        codeBadge.add(owner.roomLabel);

        // Danh sách người chơi
        JPanel playersBox = section(new Color(250, 248, 255), BORDER);
        JLabel playersEyebrow = owner.eyebrow("NGƯỜI CHƠI");
        centered(playersEyebrow);
        playersEyebrow.setHorizontalAlignment(SwingConstants.CENTER);
        owner.waitingPlayersLabel = owner.subtitle("Đang chờ người chơi tham gia...");
        owner.waitingPlayersLabel.setHorizontalAlignment(SwingConstants.CENTER);
        centered(owner.waitingPlayersLabel);
        playersBox.add(playersEyebrow);
        playersBox.add(Box.createVerticalStrut(6));
        playersBox.add(owner.waitingPlayersLabel);

        JButton copy = owner.iconButton("SAO CHÉP MÃ PHÒNG", "copy");
        limitButton(owner, copy, 44);
        owner.readyButton = owner.primaryButton("SẴN SÀNG");
        limitButton(owner, owner.readyButton, 50);
        JButton cancel = owner.secondaryButton("RỜI PHÒNG");
        limitButton(owner, cancel, 44);

        content.add(icon);
        content.add(Box.createVerticalStrut(8));
        content.add(heading);
        content.add(Box.createVerticalStrut(4));
        content.add(info);
        content.add(Box.createVerticalStrut(30));
        content.add(codeBadge);
        content.add(Box.createVerticalStrut(14));
        content.add(playersBox);
        content.add(Box.createVerticalStrut(20));
        content.add(copy);
        content.add(Box.createVerticalStrut(10));
        content.add(owner.readyButton);
        content.add(Box.createVerticalStrut(8));
        content.add(cancel);

        Holder holder = new Holder();
        GridBagConstraints gc = new GridBagConstraints();
        gc.weightx = 1;
        gc.weighty = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTH;
        holder.add(content, gc);

        JScrollPane scroll = new JScrollPane(holder,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        box.add(scroll, BorderLayout.CENTER);

        copy.addActionListener(e -> {
            if (!owner.session.roomId.isBlank()) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(owner.session.roomId), null);
                owner.popup("Đã sao chép mã phòng.");
            }
        });
        owner.readyButton.addActionListener(e -> {
            if (owner.readyButton.getText().equals("SẴN SÀNG")) {
                owner.send("READY");
                owner.readyButton.setText("BỎ SẴN SÀNG");
            } else {
                owner.send("UNREADY");
                owner.readyButton.setText("SẴN SÀNG");
            }
        });
        cancel.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(owner.frame, "Bạn có chắc chắn muốn rời phòng không?", "Rời phòng", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) owner.send("LEAVE_ROOM");
        });

        p.setLayout(new InlineCenterFitLayout(700, 570, 10, null));
        p.add(box);
        return p;
    }


    // Helpers embedded to preserve the original file count.
    /**
     * Đặt một khung con ở giữa, kích thước tối đa maxW x maxH nhưng tự co lại cho vừa cửa sổ.
     * onSize (nếu có) được gọi trước khi đặt kích thước để khung con đổi bố cục theo không gian còn lại.
     */
    private static final class InlineCenterFitLayout implements LayoutManager {
        private final int maxW, maxH, margin;
        private final BiConsumer<Integer, Integer> onSize;

        public InlineCenterFitLayout(int maxW, int maxH, int margin, BiConsumer<Integer, Integer> onSize) {
            this.maxW = maxW;
            this.maxH = maxH;
            this.margin = margin;
            this.onSize = onSize;
        }

        @Override public void addLayoutComponent(String name, Component comp) { }
        @Override public void removeLayoutComponent(Component comp) { }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return new Dimension(maxW + margin * 2, maxH + margin * 2);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return new Dimension(0, 0);
        }

        @Override
        public void layoutContainer(Container parent) {
            Insets in = parent.getInsets();
            int availW = parent.getWidth() - in.left - in.right - margin * 2;
            int availH = parent.getHeight() - in.top - in.bottom - margin * 2;
            int w = Math.max(0, Math.min(maxW, availW));
            int h = Math.max(0, Math.min(maxH, availH));
            if (onSize != null) onSize.accept(w, h);
            int x = in.left + (parent.getWidth() - in.left - in.right - w) / 2;
            int y = in.top + (parent.getHeight() - in.top - in.bottom - h) / 2;
            for (Component c : parent.getComponents()) {
                if (c.isVisible()) c.setBounds(x, y, w, h);
            }
        }
    }

    /** Thẻ nổi: bóng đổ mềm, viền đậm màu tím và dải tiêu đề tùy chọn. Dùng cho khung Phòng chờ. */
    private static class InlineElevatedCard extends JPanel {
        private static final int SHADOW = 14;
        private final Color fill, border, headerColor;
        private final int arc, headerHeight;

        public InlineElevatedCard(Color fill, Color border, Color headerColor, int headerHeight, int arc, int padH, int padV) {
            super(new BorderLayout());
            this.fill = fill;
            this.border = border;
            this.headerColor = headerColor;
            this.headerHeight = headerHeight;
            this.arc = arc;
            setOpaque(false);
            setBorder(new EmptyBorder(SHADOW + padV, SHADOW + padH, SHADOW + padV, SHADOW + padH));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int x = SHADOW, y = SHADOW, w = getWidth() - SHADOW * 2, h = getHeight() - SHADOW * 2;
            if (w <= 0 || h <= 0) {
                g2.dispose();
                return;
            }
            for (int i = SHADOW; i >= 1; i--) {
                g2.setColor(new Color(100, 80, 170, 7));
                g2.fillRoundRect(x - i, y - i + 5, w + i * 2, h + i * 2, arc + i * 2, arc + i * 2);
            }
            g2.setColor(fill);
            g2.fillRoundRect(x, y, w, h, arc, arc);
            if (headerHeight > 0 && headerColor != null) {
                Shape old = g2.getClip();
                g2.clip(new RoundRectangle2D.Float(x, y, w, h, arc, arc));
                g2.setColor(headerColor);
                g2.fillRect(x, y, w, headerHeight);
                g2.setClip(old);
            }
            g2.setStroke(new BasicStroke(3f));
            g2.setColor(border);
            g2.drawRoundRect(x + 1, y + 1, w - 3, h - 3, arc, arc);
            g2.dispose();
        }
    }

}
