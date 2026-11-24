package com.cardgame.client.controller;

import com.cardgame.client.view.MainFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
public final class LoginController {
    private final MainFrame mainFrame;
    public LoginController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }
    public void login(String username, String password, JButton button, JLabel validationMessage) {
        if (username.isBlank() || password.isEmpty()) {
            validationMessage.setText("Vui lòng nhập đầy đủ thông tin.");
            return;
        }
        button.setEnabled(false);
        mainFrame.send("LOGIN", "username", username.trim(), "password", password);
    }
    public void register(String username, String password, String confirmation,
    JButton button, JLabel validationMessage) {
        if (username.isBlank() || password.isBlank() || confirmation.isBlank()) {
            validationMessage.setText("Vui lòng nhập đầy đủ thông tin.");
            return;
        }
        if (!password.equals(confirmation)) {
            validationMessage.setText("Mật khẩu xác nhận không khớp.");
            return;
        }
        button.setEnabled(false);
        mainFrame.send("REGISTER", "username", username.trim(), "password", password);
    }
}
