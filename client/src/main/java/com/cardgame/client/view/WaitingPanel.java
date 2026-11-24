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
public final class WaitingPanel {
    private WaitingPanel() {
    }
    public static JPanel create(MainFrame owner) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG);
        JPanel box = owner.panel(WHITE);
        box.setPreferredSize(new Dimension(650, 470));
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        JLabel icon = new JLabel(new VectorIcon("hourglass", 60));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel heading = owner.title("PHÒNG CHỜ", 25);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        owner.roomLabel = owner.title("Mã phòng: ----", 31);
        owner.roomLabel.setForeground(PURPLE_DARK);
        owner.roomLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        owner.waitingPlayersLabel = owner.subtitle("Đang chờ người chơi tham gia...");
        owner.waitingPlayersLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel info = new JLabel("Chia sẻ mã phòng cho bạn bè ngay", SwingConstants.CENTER);
        info.setForeground(MUTED);
        info.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        info.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton copy = owner.iconButton("SAO CHÉP MÃ PHÒNG", "copy");
        copy.setAlignmentX(Component.CENTER_ALIGNMENT);
        owner.readyButton = owner.primaryButton("SẴN SÀNG");
        owner.readyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton cancel = owner.secondaryButton("RỜI PHÒNG");
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
            if(!owner.session.roomId.isBlank()){
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(owner.session.roomId),null);owner.popup("Đã sao chép mã phòng.");
            }
        }
        );
        owner.readyButton.addActionListener(e -> {
            if(owner.readyButton.getText().equals("SẴN SÀNG")){
                owner.send("READY");owner.readyButton.setText("BỎ SẴN SÀNG");
            } else{
                owner.send("UNREADY");owner.readyButton.setText("SẴN SÀNG");
            }
        }
        );
        cancel.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(owner.frame, "Bạn có chắc chắn muốn rời phòng không?", "Rời phòng", JOptionPane.YES_NO_OPTION); if (choice == JOptionPane.YES_OPTION) owner.send("LEAVE_ROOM");
        }
        );
        p.add(box);
        return p;
    }
}
