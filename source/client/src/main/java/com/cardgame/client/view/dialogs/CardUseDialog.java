package com.cardgame.client.view.dialogs;

import javax.swing.*;
import java.awt.Component;

public final class CardUseDialog {
    private static volatile JDialog activeDialog;

    private CardUseDialog() { }

    public static void closeActive() {
        JDialog dialog = activeDialog;
        if (dialog != null) SwingUtilities.invokeLater(dialog::dispose);
    }

    public static boolean confirmTargetCard(Component parent, String cardCode) {
        String question = cardCode.equals("A02")
                ? "Dùng Khóa bài? Sau khi xác nhận, hãy bấm vào một lá úp để khóa."
                : "Dùng Con mắt tinh anh? Sau khi xác nhận, hãy bấm vào một lá úp để xem riêng.";
        return confirm(parent, question, "Xác nhận sử dụng thẻ");
    }

    public static boolean confirmUse(Component parent, String cardName) {
        return confirm(parent, "Bạn chắc chắn muốn sử dụng " + cardName + "?", "Xác nhận sử dụng thẻ");
    }

    public static boolean confirmShield(Component parent, String cardCode) {
        return confirm(parent, "Đối thủ dùng " + cardCode + ". Bạn có muốn dùng Khiên bảo vệ để chặn không?", "Khiên bảo vệ");
    }

    private static boolean confirm(Component parent, String message, String title) {
        JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
        JDialog dialog = pane.createDialog(parent, title);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        activeDialog = dialog;
        dialog.setVisible(true);
        if (activeDialog == dialog) activeDialog = null;
        Object value = pane.getValue();
        return value instanceof Integer result && result == JOptionPane.YES_OPTION;
    }
}
