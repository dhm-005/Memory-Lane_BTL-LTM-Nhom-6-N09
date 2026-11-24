
package com.cardgame.client.view;

import com.cardgame.client.view.components.VectorIcon;
import com.cardgame.client.controller.LoginController;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import static com.cardgame.client.view.MainFrame.*;
import static com.cardgame.client.view.UiTheme.*;

public final class RegisterPanel {
    private RegisterPanel() {
    }

    // Thêm để có thể xóa dữ liệu đăng ký từ bên ngoài
    private static JTextField userField;
    private static JPasswordField passField;
    private static JPasswordField confirmField;
    private static JLabel statusLabel;

    public static void clearFields() {
        if (userField != null) userField.setText("");
        if (passField != null) passField.setText("");
        if (confirmField != null) confirmField.setText("");
        if (statusLabel != null) statusLabel.setText(" ");
    }

    public static JPanel create(MainFrame owner) {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG);
        JPanel shell = new JPanel(new GridLayout(1, 2));
        shell.setPreferredSize(new Dimension(920, 590));
        shell.setBackground(WHITE);
        shell.setBorder(BorderFactory.createLineBorder(BORDER));

        JPanel art = new JPanel(new GridBagLayout());
        art.setBackground(new Color(235, 226, 255));
        JPanel artContent = owner.vertical();
        artContent.setBorder(new EmptyBorder(35, 30, 35, 30));
        artContent.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        logoRow.setOpaque(false);
        JLabel logoIcon = new JLabel(new VectorIcon("sparkle", 29));
        JLabel logoText = new JLabel("MEMORY DUEL");
        logoText.setFont(new Font("Segoe UI", Font.BOLD, 29));
        logoText.setForeground(TEXT);
        logoRow.add(logoIcon);
        logoRow.add(Box.createHorizontalStrut(9));
        logoRow.add(logoText);
        logoRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagline = new JLabel("THINK FAST. MATCH SMART. WIN THE DUEL.", SwingConstants.CENTER);
        tagline.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tagline.setForeground(PURPLE_DARK);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel hero = new JPanel(new GridBagLayout());
        hero.setOpaque(false);
        hero.setPreferredSize(new Dimension(300, 170));
        hero.setMaximumSize(new Dimension(300, 170));
        hero.add(new JLabel(new VectorIcon("playing-card", 145)));

        JLabel slogan = new JLabel("<html><div style='text-align:center'>Tạo tài khoản mới<br>và bắt đầu thử thách!</div></html>", SwingConstants.CENTER);
        slogan.setFont(new Font("Segoe UI", Font.BOLD, 24));
        slogan.setForeground(TEXT);
        slogan.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel description = new JLabel("<html><div style='text-align:center'>Lưu thành tích và so tài cùng bạn bè</div></html>", SwingConstants.CENTER);
        description.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        description.setForeground(MUTED);
        description.setAlignmentX(Component.CENTER_ALIGNMENT);

        artContent.add(Box.createVerticalGlue());
        artContent.add(logoRow);
        artContent.add(Box.createVerticalStrut(12));
        artContent.add(tagline);
        artContent.add(Box.createVerticalStrut(22));
        artContent.add(hero);
        artContent.add(Box.createVerticalStrut(12));
        artContent.add(slogan);
        artContent.add(Box.createVerticalStrut(10));
        artContent.add(description);
        artContent.add(Box.createVerticalGlue());
        art.add(artContent);

        JPanel formSide = new JPanel(new GridBagLayout());
        formSide.setBackground(WHITE);
        JPanel form = owner.vertical();
        form.setBorder(new EmptyBorder(28, 0, 28, 0));

        JLabel heading = owner.title("Tạo tài khoản", 27);
        JLabel hint = owner.wrappedLabel("Đăng ký tài khoản và tham gia các trận đấu.", 310);

        JTextField user = new JTextField();
        owner.styleField(user, "Nhập tên đăng nhập");

        JPasswordField pass = new JPasswordField();
        owner.styleField(pass, "Nhập mật khẩu");

        JPasswordField confirm = new JPasswordField();
        owner.styleField(confirm, "Nhập lại mật khẩu");

        JLabel status = owner.subtitle(" ");
        owner.setFixedWidth(status, 310, 38);

        // Gán các ô nhập vào biến để hàm clearFields() sử dụng
        userField = user;
        passField = pass;
        confirmField = confirm;
        statusLabel = status;

        JButton submit = owner.primaryButton("ĐĂNG KÝ");
        owner.registerButton = submit;
        owner.setFixedWidth(submit, 310, 48);

        JButton back = owner.secondaryButton("ĐÃ CÓ TÀI KHOẢN? ĐĂNG NHẬP");
        owner.setFixedWidth(back, 310, 44);

        LoginController controller = new LoginController(owner);

        submit.addActionListener(e -> controller.register(
                user.getText(),
                new String(pass.getPassword()),
                new String(confirm.getPassword()),
                submit,
                status
        ));

        back.addActionListener(e -> {
            LoginPanel.clearFields();
            RegisterPanel.clearFields();
            owner.showPage("login");
        });

        form.add(heading);
        form.add(Box.createVerticalStrut(8));
        form.add(hint);
        form.add(Box.createVerticalStrut(22));
        form.add(owner.eyebrow("TÊN ĐĂNG NHẬP"));
        form.add(Box.createVerticalStrut(7));
        form.add(user);
        form.add(Box.createVerticalStrut(13));
        form.add(owner.eyebrow("MẬT KHẨU"));
        form.add(Box.createVerticalStrut(7));
        form.add(pass);
        form.add(Box.createVerticalStrut(13));
        form.add(owner.eyebrow("XÁC NHẬN MẬT KHẨU"));
        form.add(Box.createVerticalStrut(7));
        form.add(confirm);
        form.add(Box.createVerticalStrut(18));
        form.add(submit);
        form.add(Box.createVerticalStrut(9));
        form.add(back);
        form.add(Box.createVerticalStrut(8));
        form.add(status);

        formSide.add(form);
        shell.add(art);
        shell.add(formSide);
        outer.add(shell);

        return outer;
    }
}
