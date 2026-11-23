package com.mcclient.view;

import com.mcclient.view.components.RoundedPanel;
import com.mcclient.view.components.VectorIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.mcclient.view.UITheme.*;

public final class CommonUI {
    private CommonUI() { }

    // =====================================================
    // LAYOUT & TEXT
    // =====================================================
    public static JPanel page() {
        JPanel p = new JPanel(new BorderLayout(0, 24));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(30, 38, 30, 38));
        return p;
    }

    public static JPanel vertical() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    public static JLabel title(String text, int size) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT);
        l.setFont(new Font("Segoe UI", Font.BOLD, size));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    public static JLabel subtitle(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(MUTED);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    public static JLabel wrappedLabel(String text, int width) {
        JLabel l = new JLabel("<html><div style='width:" + width + "px;'>" + text + "</div></html>");
        l.setForeground(MUTED);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    public static JLabel eyebrow(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(PURPLE_DARK);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    public static JLabel iconLabel(String text, String icon, int size) {
        JLabel l = new JLabel(text, new VectorIcon(icon, size), SwingConstants.LEFT);
        l.setFont(new Font("Segoe UI", Font.BOLD, 19));
        l.setForeground(TEXT);
        l.setIconTextGap(10);
        return l;
    }

    // =====================================================
    // CONTAINERS, BUTTONS & INPUTS
    // =====================================================
    public static JPanel panel(Color color) {
        RoundedPanel p = new RoundedPanel(color);
        p.setLayout(new BorderLayout(0, 14));
        p.setBorder(new EmptyBorder(24, 26, 24, 26));
        return p;
    }

    public static JButton primaryButton(String text) {
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
            public void mouseEntered(MouseEvent e) { b.setBackground(PURPLE_DARK); }
            @Override
            public void mouseExited(MouseEvent e) { b.setBackground(PURPLE); }
        });
        return b;
    }

    public static JButton secondaryButton(String text) {
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

    public static JButton iconButton(String text, String icon) {
        JButton b = secondaryButton(text);
        b.setIcon(new VectorIcon(icon, 18));
        b.setIconTextGap(8);
        return b;
    }

    public static void setFixedWidth(JComponent c, int width, int height) {
        Dimension d = new Dimension(width, height);
        c.setPreferredSize(d);
        c.setMinimumSize(d);
        c.setMaximumSize(d);
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    public static void styleField(JTextField field, String placeholder) {
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT);
        field.setBackground(Color.WHITE);
        setFixedWidth(field, 310, 46);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 12, 8, 12)
        ));
    }

    // =====================================================
    // TABLE STYLING & DECORATIONS
    // =====================================================
    public static void styleTable(JTable table) {
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

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setForeground(PURPLE_DARK);
        table.getTableHeader().setBackground(new Color(243, 237, 255));
        table.getTableHeader().setPreferredSize(new Dimension(0, 46));
        table.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, selected, focus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setVerticalAlignment(SwingConstants.CENTER);
                label.setBorder(new EmptyBorder(5, 8, 5, 8));
                label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                if (!selected) {
                    label.setBackground(row % 2 == 0 ? WHITE : new Color(250, 247, 255));
                    label.setForeground(TEXT);
                }
                label.setToolTipText(value == null ? null : value.toString());
                return label;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
            table.getColumnModel().getColumn(i).setPreferredWidth(160);
        }
        table.setAutoCreateRowSorter(true);
    }

    public static void decorateHistory(JTable table) {
        if (table.getColumnCount() == 0) return;
        table.getColumnModel().getColumn(0).setPreferredWidth(180);

        DefaultTableCellRenderer historyRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, selected, focus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setIcon(new VectorIcon("history", 17));
                label.setIconTextGap(7);
                label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                if (!selected) {
                    label.setBackground(row % 2 == 0 ? WHITE : new Color(250, 247, 255));
                    label.setForeground(TEXT);
                }
                return label;
            }
        };
        table.getColumnModel().getColumn(0).setCellRenderer(historyRenderer);
    }

    public static void decorateLeaderboard(JTable table) {
        if (table.getColumnCount() == 0) return;
        table.getColumnModel().getColumn(0).setPreferredWidth(250);
        if (table.getColumnCount() > 1) table.getColumnModel().getColumn(1).setPreferredWidth(150);
        if (table.getColumnCount() > 2) table.getColumnModel().getColumn(2).setPreferredWidth(150);
        if (table.getColumnCount() > 3) table.getColumnModel().getColumn(3).setPreferredWidth(150);

        DefaultTableCellRenderer rankRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, selected, focus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 14));
                if (!selected) {
                    label.setBackground(row % 2 == 0 ? WHITE : new Color(250, 247, 255));
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
        table.getColumnModel().getColumn(0).setCellRenderer(rankRenderer);
    }
}