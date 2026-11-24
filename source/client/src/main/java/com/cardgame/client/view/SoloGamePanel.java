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
public final class SoloGamePanel {
    private SoloGamePanel() {
    }
    public static JPanel create(MainFrame owner) {
        JPanel p = owner.page();
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel labels = owner.vertical();
        labels.add(owner.eyebrow("MEMORY DUEL  /  SOLO"));
        labels.add(owner.title("Chinh phục 5 cấp độ", 25));
        labels.add(owner.subtitle(""));
        header.add(labels, BorderLayout.WEST);
        JButton exit = owner.secondaryButton("THOÁT CHẾ ĐỘ ĐƠN");
        exit.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    owner.frame,
                    "Bạn có chắc chắn muốn thoát không? ",
                    "Xác nhận thoát",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (choice == JOptionPane.YES_OPTION) {
                owner.stopSoloTimers();
                owner.showPage("lobby");
            }
        });
        header.add(exit, BorderLayout.EAST);
        p.add(header, BorderLayout.NORTH);
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setOpaque(false);
        JPanel stats = owner.panel(WHITE);
        stats.setLayout(new GridLayout(1, 3, 12, 0));
        owner.soloLevelLabel = owner.title("LEVEL 1 / 5", 19);
        owner.soloTimerLabel = owner.title("00:00", 22);
        owner.soloPairsLabel = owner.title("0 / 2 cặp", 19);
        stats.add(owner.soloLevelLabel);
        stats.add(owner.soloTimerLabel);
        stats.add(owner.soloPairsLabel);
        owner.soloStatusLabel = owner.subtitle("Lật thẻ bài và tìm tất cả các cặp.");
        JPanel status = new JPanel(new BorderLayout(0,5));
        status.setOpaque(false);
        status.add(owner.soloStatusLabel, BorderLayout.NORTH);
        owner.soloRecordLabel = owner.subtitle("Thành tích tốt nhất: chưa có");
        status.add(owner.soloRecordLabel, BorderLayout.SOUTH);
        owner.soloBoard = new JPanel(new GridLayout(2, 2, 7, 7));
        owner.soloBoard.setOpaque(false);
        JPanel boardShell = owner.panel(WHITE);
        boardShell.setLayout(new BorderLayout(0, 10));
        boardShell.add(status, BorderLayout.NORTH);
        boardShell.add(owner.soloBoard, BorderLayout.CENTER);
        content.add(stats, BorderLayout.NORTH);
        content.add(boardShell, BorderLayout.CENTER);
        p.add(content, BorderLayout.CENTER);
        return p;
    }
}
