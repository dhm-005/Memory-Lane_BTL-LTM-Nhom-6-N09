package com.mcclient.view.dialogs;

import com.mcclient.view.components.VectorIcon;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

public final class RpsDialog {
    private static final Color BACKGROUND = new Color(255, 249, 241);
    private static final Color TEXT = new Color(55, 50, 75);
    private RpsDialog() { }

    public static void show(Window owner, boolean tie, Consumer<String> onChoice) {
        JDialog dialog = new JDialog(owner, tie ? "Hòa rồi, chọn lại nhé!" : "Chọn Kéo – Búa – Bao",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        JPanel root = new JPanel(new BorderLayout(10, 14));
        root.setBorder(new EmptyBorder(22, 24, 22, 24));
        root.setBackground(BACKGROUND);
        JLabel heading = new JLabel(tie ? "CHỌN LẠI NÀO!" : "OẲN TÙ TÌ", SwingConstants.CENTER);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 24));
        heading.setForeground(TEXT);
        root.add(heading, BorderLayout.NORTH);
        JPanel picks = new JPanel(new GridLayout(1, 3, 14, 0));
        picks.setOpaque(false);
        String[] labels = {"BÚA", "BAO", "KÉO"};
        String[] codes = {"ROCK", "PAPER", "SCISSORS"};
        String[] icons = {"rock", "paper", "scissors"};
        Color[] colors = {new Color(255, 238, 211), new Color(220, 241, 255), new Color(244, 228, 255)};
        for (int i = 0; i < 3; i++) {
            final String code = codes[i];
            JButton button = new JButton(labels[i], new VectorIcon(icons[i], 58));
            button.setVerticalTextPosition(SwingConstants.BOTTOM);
            button.setHorizontalTextPosition(SwingConstants.CENTER);
            button.setIconTextGap(10);
            button.setFont(new Font("Segoe UI", Font.BOLD, 15));
            button.setForeground(TEXT);
            button.setBackground(colors[i]);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(222, 211, 238), 2, true),
                    new EmptyBorder(14, 12, 14, 12)));
            button.addActionListener(e -> { onChoice.accept(code); dialog.dispose(); });
            picks.add(button);
        }
        root.add(picks, BorderLayout.CENTER);
        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }
}
