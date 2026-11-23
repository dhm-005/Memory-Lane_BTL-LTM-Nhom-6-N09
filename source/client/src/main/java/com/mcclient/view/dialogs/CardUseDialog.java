package com.mcclient.view.dialogs;

import javax.swing.*;
import java.awt.Component;

public final class CardUseDialog {
    private CardUseDialog() { }

    public static boolean confirmTargetCard(Component parent, String cardCode) {
        String question = cardCode.equals("A02")
                ? "Dùng Khóa bài? Sau khi xác nhận, hãy bấm vào một lá úp để khóa."
                : "Dùng Con mắt tinh anh? Sau khi xác nhận, hãy bấm vào một lá úp để xem riêng.";
        return JOptionPane.showConfirmDialog(parent, question, "Xác nhận sử dụng thẻ",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static boolean confirmUse(Component parent, String cardName) {
        return JOptionPane.showConfirmDialog(parent,
                "Bạn chắc chắn muốn sử dụng " + cardName + "?", "Xác nhận sử dụng thẻ",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static boolean confirmShield(Component parent, String cardCode) {
        return JOptionPane.showConfirmDialog(parent,
                "Đối thủ dùng " + cardCode + ". Bạn có muốn dùng Khiên bảo vệ để chặn không?",
                "Khiên bảo vệ", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
