package com.cardgame.client.view.dialogs;

import javax.swing.*;
import java.awt.Component;

public final class MatchResultDialog {
    private MatchResultDialog() {
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
    public static void show(Component parent, String result, int myScore, int opponentScore,
    int purchaseCoinChange, int finishCoinChange, int rankChange) {
        String message = result + "\nTỉ số (bạn - đối thủ): " + myScore + " - " + opponentScore
        + "\nXu mua thẻ: " + formatCoins(purchaseCoinChange) + " xu"
        + "\nXu hoàn thành trận: " + formatCoins(finishCoinChange) + " xu"
        + "\nĐiểm xếp hạng: +" + rankChange;
        JOptionPane.showMessageDialog(parent, message, "Kết thúc trận đấu", JOptionPane.INFORMATION_MESSAGE);
    }
}
