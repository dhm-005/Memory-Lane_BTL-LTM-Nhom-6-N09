package com.mcclient.view;

import javax.swing.*;

public final class LeaderboardPanel {
    private LeaderboardPanel() { }
    public static JPanel create(MainFrame owner) {
        JPanel panel = DataPagePanel.create(owner, "BẢNG XẾP HẠNG", "Thành tích và điểm số của người chơi.");
        owner.leaderboardTable = (JTable) panel.getClientProperty("dataTable");
        return panel;
    }
}
