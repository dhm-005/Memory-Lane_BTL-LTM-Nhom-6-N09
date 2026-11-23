package com.mcclient.view.dialogs;

import javax.swing.*;
import java.awt.Component;

public final class MatchResultDialog {
    private MatchResultDialog() { }
    public static void show(Component parent, String result, int myScore, int opponentScore,
                            int coinChange, int rankChange) {
        String message = result + "\nTỉ số (bạn - đối thủ): " + myScore + " - " + opponentScore
                + "\nXu thay đổi trong trận (đã tính tiền mua thẻ): " + coinChange
                + "\nĐiểm xếp hạng: +" + rankChange;
        JOptionPane.showMessageDialog(parent, message, "Kết thúc trận đấu", JOptionPane.INFORMATION_MESSAGE);
    }
}
