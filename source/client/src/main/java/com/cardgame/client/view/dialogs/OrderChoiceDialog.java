package com.cardgame.client.view.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;
public final class OrderChoiceDialog {
    private static volatile JDialog activeDialog;
    public static void closeActive() {
        JDialog dialog = activeDialog;
        if (dialog != null) SwingUtilities.invokeLater(dialog::dispose);
    }
    private static final Color BACKGROUND = new Color(255, 249, 241);
    private OrderChoiceDialog() {
    }
    public static void show(Window owner, Consumer<String> onChoice) {
        JDialog dialog = new JDialog(owner, "Chọn thứ tự đánh", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        JPanel root = new JPanel(new BorderLayout(12, 16));
        root.setBorder(new EmptyBorder(22, 24, 22, 24));
        root.setBackground(BACKGROUND);
        JLabel heading = new JLabel("BẠN THẮNG OẲN TÙ TÌ!", SwingConstants.CENTER);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 23));
        heading.setForeground(new Color(55, 50, 75));
        JLabel hint = new JLabel("Chọn một lá bài để quyết định thứ tự chơi", SwingConstants.CENTER);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        hint.setForeground(new Color(145, 139, 158));
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(heading);
        header.add(Box.createVerticalStrut(5));
        header.add(hint);
        root.add(header, BorderLayout.NORTH);
        JPanel choices = new JPanel(new GridLayout(1, 2, 16, 0));
        choices.setOpaque(false);
        String[] names = {
            "ĐÁNH TRƯỚC", "ĐÁNH SAU"
        };
        String[] descriptions = {
            "Bạn được lật bài đầu tiên", "Đối thủ được lật bài đầu tiên"
        };
        String[] orders = {
            "FIRST", "SECOND"
        };
        Color[] colors = {
            new Color(255, 244, 218), new Color(232, 239, 255)
        };
        for (int i = 0; i < 2; i++) {
            final String order = orders[i];
            JButton card = new JButton("<html><div style='text-align:center;width:170px;padding:12px'>"
            + "<div style='font-size:27px;color:#8B75C2;font-weight:bold'>" + (i == 0 ? "01" : "02") + "</div><br>"
            + "<div style='font-size:16px;font-weight:bold;color:#403957'>" + names[i] + "</div><br>"
            + "<div style='font-size:11px;color:#777080'>" + descriptions[i] + "</div></div></html>");
            card.setPreferredSize(new Dimension(220, 190));
            card.setBackground(colors[i]);
            card.setFocusPainted(false);
            card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(218, 207, 236), 2, true),
            new EmptyBorder(12, 10, 12, 10)));
            card.addActionListener(e -> {
                onChoice.accept(order); dialog.dispose();
            }
            );
            choices.add(card);
        }
        root.add(choices, BorderLayout.CENTER);
        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        activeDialog = dialog;
        dialog.setVisible(true);
        if (activeDialog == dialog) activeDialog = null;
    }
}
