package com.cardgame.client.view.dialogs;

import javax.swing.*;
import java.awt.Component;
public final class MatchCancelledDialog {
    private MatchCancelledDialog() {
    }
    private static String formatCoins(int value) {
        if (value > 0) {
            return "+" + value;
        }
        if (value < 0) {
            return "-" + Math.abs(value);
        }
        return "0";

    }
    public static void show(Component parent, String reason, int purchaseCoinChange, int finishCoinChange) {
        JOptionPane.showMessageDialog(parent,
        "Trận đã bị hủy: " + reason
        + "\nXu mua thẻ: " + formatCoins(purchaseCoinChange) + " xu"
        + "\nXu hoàn thành trận: " + formatCoins(finishCoinChange) + " xu",
        "Trận đấu bị hủy", JOptionPane.INFORMATION_MESSAGE);
    }
}
