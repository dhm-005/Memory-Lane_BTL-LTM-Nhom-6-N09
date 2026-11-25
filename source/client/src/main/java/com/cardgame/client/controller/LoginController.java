package com.cardgame.client.controller;

import com.cardgame.client.view.MainFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import static com.cardgame.client.view.UiTheme.DANGER;

public final class LoginController {
    private final MainFrame mainFrame;

    public LoginController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    private static void fail(JLabel label, String message) {
        label.setForeground(DANGER);
        label.setText(message);
    }

    public void login(String username, String password, JButton button, JLabel validationMessage) {
        if (username.isBlank() || password.isEmpty()) {
            fail(validationMessage, "Vui lòng nhập đầy đủ thông tin.");
            return;
        }
        validationMessage.setText(" ");
        button.setEnabled(false);
        mainFrame.send("LOGIN", "username", username.trim(), "password", password);
    }

    public void register(String username, String password, String confirmation,
                         JButton button, JLabel validationMessage) {
        if (username.isBlank() || password.isBlank() || confirmation.isBlank()) {
            fail(validationMessage, "Vui lòng nhập đầy đủ thông tin.");
            return;
        }
        if (username.trim().length() < 3 || username.trim().length() > 24 || password.length() < 4) {
            fail(validationMessage, "Tên đăng nhập cần 3-24 ký tự, mật khẩu ít nhất 4 ký tự.");
            return;
        }
        if (!password.equals(confirmation)) {
            fail(validationMessage, "Mật khẩu xác nhận không khớp.");
            return;
        }
        validationMessage.setText(" ");
        button.setEnabled(false);
        mainFrame.send("REGISTER", "username", username.trim(), "password", password);
    }
}
