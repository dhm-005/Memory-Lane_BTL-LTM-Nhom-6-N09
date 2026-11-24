package com.cardgame.client.view.dialogs;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
public final class ChangePasswordDialog {
    private ChangePasswordDialog() {
    }
    public static void show(Component parent, BiConsumer<String, String> onSubmit, Consumer<String> onError) {
        JPasswordField oldField = new JPasswordField(18);
        JPasswordField newField = new JPasswordField(18);
        JPasswordField confirmField = new JPasswordField(18);
        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.add(new JLabel("Mật khẩu hiện tại:"));
        form.add(oldField);
        form.add(new JLabel("Mật khẩu mới (ít nhất 4 ký tự):"));
        form.add(newField);
        form.add(new JLabel("Nhập lại mật khẩu mới:"));
        form.add(confirmField);
        int result = JOptionPane.showConfirmDialog(parent, form, "ĐỔI MẬT KHẨU",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;
        String oldPassword = new String(oldField.getPassword());
        String newPassword = new String(newField.getPassword());
        String confirmation = new String(confirmField.getPassword());
        if (oldPassword.isEmpty() || newPassword.length() < 4 || !newPassword.equals(confirmation)) {
            onError.accept("Vui lòng nhập đúng mật khẩu hiện tại, mật khẩu mới tối thiểu 4 ký tự và xác nhận trùng khớp.");
            return;
        }
        onSubmit.accept(oldPassword, newPassword);
    }
}
