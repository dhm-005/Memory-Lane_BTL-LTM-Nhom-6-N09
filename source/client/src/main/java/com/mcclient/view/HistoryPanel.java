package com.mcclient.view;

import javax.swing.*;

public final class HistoryPanel {
    private HistoryPanel() { }
    public static JPanel create(MainFrame owner) {
        JPanel panel = DataPagePanel.create(owner, "LỊCH SỬ TRẬN ĐẤU", "Theo dõi những trận đấu đã diễn ra.");
        owner.historyTable = (JTable) panel.getClientProperty("dataTable");
        return panel;
    }
}
