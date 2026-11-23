package com.mcclient.view;

import com.mcclient.util.JsonTemplates;
import javax.swing.*;
import java.awt.*;
import static com.mcclient.view.UITheme.BG;
import static com.mcclient.view.UITheme.MUTED;
import static com.mcclient.view.UITheme.PURPLE_DARK;
import static com.mcclient.view.UITheme.WHITE;
import com.mcclient.view.components.VectorIcon;

public final class WaitingPanel {

    private WaitingPanel() {
    }

    public static JPanel create(MainFrame owner) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG);
        JPanel box = CommonUI.panel(WHITE);
        box.setPreferredSize(new Dimension(650, 470));
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        JLabel icon = new JLabel(new VectorIcon("hourglass", 60));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel heading = CommonUI.title("PHÒNG CHỜ", 25);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        owner.roomLabel = CommonUI.title("Mã phòng: ----", 31);
        owner.roomLabel.setForeground(PURPLE_DARK);
        owner.roomLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        owner.waitingPlayersLabel = CommonUI.subtitle("Đang chờ người chơi tham gia...");
        owner.waitingPlayersLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel info = new JLabel("Chia sẻ mã phòng, sau đó cả hai cùng bấm Sẵn sàng.", SwingConstants.CENTER);
        info.setForeground(MUTED);
        info.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        info.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton copy = CommonUI.iconButton("SAO CHÉP MÃ PHÒNG", "copy");
        copy.setAlignmentX(Component.CENTER_ALIGNMENT);
        owner.readyButton = CommonUI.primaryButton("SẴN SÀNG");
        owner.readyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton cancel = CommonUI.secondaryButton("RỜI PHÒNG");
        cancel.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.add(Box.createVerticalGlue());
        box.add(icon);
        box.add(Box.createVerticalStrut(12));
        box.add(heading);
        box.add(Box.createVerticalStrut(20));
        box.add(owner.roomLabel);
        box.add(Box.createVerticalStrut(12));
        box.add(owner.waitingPlayersLabel);
        box.add(Box.createVerticalStrut(12));
        box.add(info);
        box.add(Box.createVerticalGlue());
        box.add(copy);
        box.add(Box.createVerticalStrut(10));
        box.add(owner.readyButton);
        box.add(Box.createVerticalStrut(8));
        box.add(cancel);
        box.add(Box.createVerticalStrut(6));

        copy.addActionListener(e -> {
            if (!owner.roomId.isBlank()) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                        new java.awt.datatransfer.StringSelection(owner.roomId), null
                );
                owner.popup("Đã sao chép mã phòng.");
            }
        });

        owner.readyButton.addActionListener(e -> {
            if (owner.readyButton.getText().equals("SẴN SÀNG")) {
                owner.send("READY", JsonTemplates.empty());
                owner.readyButton.setText("BỎ SẴN SÀNG");
            } else {
                owner.send("UNREADY", JsonTemplates.empty());
                owner.readyButton.setText("SẴN SÀNG");
            }
        });

        cancel.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    owner.frame,
                    "Rời phòng chờ? Phòng vẫn được giữ nếu còn người chơi khác.",
                    "Rời phòng",
                    JOptionPane.YES_NO_OPTION
            );
            if (choice == JOptionPane.YES_OPTION) {
                owner.send("LEAVE_ROOM", JsonTemplates.empty());
            }
        });

        p.add(box);
        return p;

    }
}
