package com.mcclient.view;

import javax.swing.*;

public final class CollectionPanel {
    private CollectionPanel() { }
    public static JPanel create(MainFrame owner) {
        JPanel panel = DataPagePanel.create(owner, "BỘ SƯU TẬP THẺ", "Thông tin 12 thẻ trong game. Đây chỉ là trang xem, không mua thẻ tại đây.");
        owner.collectionTable = (JTable) panel.getClientProperty("dataTable");
        return panel;
    }
}
