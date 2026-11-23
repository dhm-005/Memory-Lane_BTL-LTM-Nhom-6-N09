package com.mcclient.view;

import com.mcclient.util.JsonTemplates;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import static com.mcclient.view.UITheme.BORDER;
import static com.mcclient.view.UITheme.WHITE;

final class DataPagePanel {
    private DataPagePanel() { }

    static JPanel create(MainFrame owner, String heading, String description) {
        JPanel p = CommonUI.page();

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel labels = CommonUI.vertical();
        labels.add(CommonUI.eyebrow("MEMORY DUEL  /  THỐNG KÊ"));
        labels.add(Box.createVerticalStrut(5));
        labels.add(CommonUI.title(heading, 27));
        labels.add(Box.createVerticalStrut(5));
        labels.add(CommonUI.wrappedLabel(description, 500));

        top.add(labels, BorderLayout.WEST);

        JButton back = CommonUI.iconButton("QUAY LẠI SẢNH", "back");
        back.addActionListener(e -> owner.showPage("lobby"));
        top.add(back, BorderLayout.EAST);

        p.add(top, BorderLayout.NORTH);

        JPanel content = CommonUI.panel(WHITE);
        content.setLayout(new BorderLayout(0, 15));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);

        boolean isRank = heading.equals("BẢNG XẾP HẠNG");
        boolean isCollection = heading.equals("BỘ SƯU TẬP THẺ");
        tableHeader.add(
                CommonUI.iconLabel(isRank ? "TOP PLAYERS" : isCollection ? "CARD COLLECTION" : "MATCH HISTORY",
                        isRank ? "trophy" : isCollection ? "playing-card-small" : "history", 24),
                BorderLayout.WEST
        );
        tableHeader.add(CommonUI.subtitle("DỮ LIỆU MÁY CHỦ"), BorderLayout.EAST);

        JTable table = new JTable(new DefaultTableModel(new Object[][]{}, new String[]{"Đang tải dữ liệu..."}));
        CommonUI.styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        if (isRank) {
            JPanel rankTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            rankTabs.setOpaque(false);
            owner.soloLeaderboardTab = CommonUI.secondaryButton("BXH SOLO");
            owner.pvpLeaderboardTab = CommonUI.secondaryButton("BXH 1V1");
            owner.soloLeaderboardTab.addActionListener(e -> {
                owner.showingSoloLeaderboard = true;
                owner.updateLeaderboardTabs();
                owner.send("SOLO_LEADERBOARD", JsonTemplates.empty());
            });
            owner.pvpLeaderboardTab.addActionListener(e -> {
                owner.showingSoloLeaderboard = false;
                owner.updateLeaderboardTabs();
                owner.send("LEADERBOARD", JsonTemplates.empty());
            });
            rankTabs.add(owner.soloLeaderboardTab);
            rankTabs.add(owner.pvpLeaderboardTab);

            JPanel rankHeader = new JPanel();
            rankHeader.setOpaque(false);
            rankHeader.setLayout(new BoxLayout(rankHeader, BoxLayout.Y_AXIS));
            rankHeader.add(tableHeader);
            rankHeader.add(Box.createVerticalStrut(10));
            rankHeader.add(rankTabs);
            content.add(rankHeader, BorderLayout.NORTH);
            owner.updateLeaderboardTabs();
        } else {
            content.add(tableHeader, BorderLayout.NORTH);
        }
        content.add(scroll, BorderLayout.CENTER);

        p.add(content, BorderLayout.CENTER);
        p.putClientProperty("dataTable", table);

        return p;
    }
}