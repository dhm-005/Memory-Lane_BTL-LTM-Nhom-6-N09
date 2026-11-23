package com.mcclient.view.dialogs;

import javax.swing.*;
import java.awt.Component;

public final class MatchCancelledDialog {
    private MatchCancelledDialog() { }
    public static void show(Component parent, String reason, int coinChange) {
        JOptionPane.showMessageDialog(parent,
                "Trận đã bị hủy: " + reason + "\nXu thay đổi của bạn: " + coinChange,
                "Trận đấu bị hủy", JOptionPane.INFORMATION_MESSAGE);
    }
}
