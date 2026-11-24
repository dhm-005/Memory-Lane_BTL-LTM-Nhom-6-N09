
package com.cardgame.client.view;

import com.cardgame.client.view.components.VectorIcon;
import com.cardgame.client.controller.LoginController;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import static com.cardgame.client.view.MainFrame.*;
import static com.cardgame.client.view.UiTheme.*;

public final class LoginPanel {
    private LoginPanel() {
    }

    // Thêm để có thể xóa dữ liệu đăng nhập từ bên ngoài
    private static JTextField userField;
    private static JPasswordField passField;
    private static JLabel statusLabel;

    public static void clearFields() {
        if (userField != null) userField.setText("");
        if (passField != null) passField.setText("");
        if (statusLabel != null) statusLabel.setText(" ");
    }

    public static JPanel create(MainFrame owner) {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG);
        JPanel shell = new JPanel(new GridLayout(1, 2));
        shell.setPreferredSize(new Dimension(920, 570));
        shell.setBackground(WHITE);
        shell.setBorder(BorderFactory.createLineBorder(BORDER));

        // LEFT SIDE
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

        JLabel tagline = new JLabel(
                "THINK FAST. MATCH SMART. WIN THE DUEL.",
                SwingConstants.CENTER
        );
        tagline.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tagline.setForeground(PURPLE_DARK);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel hero = new JPanel(new GridBagLayout());
        hero.setOpaque(false);
        hero.setPreferredSize(new Dimension(300, 170));
        hero.setMaximumSize(new Dimension(300, 170));
        hero.add(new JLabel(new VectorIcon("playing-card", 145)));

        JLabel slogan = new JLabel(
                "<html><div style='text-align:center'>"
                        + "Một chút tinh mắt,<br>"
                        + "một chút may mắn!"
                        + "</div></html>",
                SwingConstants.CENTER
        );
        slogan.setFont(new Font("Segoe UI", Font.BOLD, 24));
        slogan.setForeground(TEXT);
        slogan.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel description = new JLabel(
                "<html><div style='text-align:center'>"
                        + "Thử thách trí nhớ trong trận đấu 1v1"
                        + "</div></html>",
                SwingConstants.CENTER
        );
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

        // RIGHT SIDE
        JPanel formSide = new JPanel(new GridBagLayout());
        formSide.setBackground(WHITE);
        JPanel form = owner.vertical();
        form.setBorder(new EmptyBorder(35, 0, 35, 0));

        JLabel heading = owner.title("Chào mừng trở lại!", 27);
        JLabel hint = owner.wrappedLabel(
                "Đăng nhập hoặc tạo tài khoản để "
                        + "tham gia những trận đấu thú vị.",
                310
        );

        JLabel userLabel = owner.eyebrow("TÊN ĐĂNG NHẬP");
        JTextField user = new JTextField();
        owner.styleField(user, "Nhập tên đăng nhập");

        JLabel passLabel = owner.eyebrow("MẬT KHẨU");
        JPasswordField pass = new JPasswordField();
        owner.styleField(pass, "Nhập mật khẩu");

        JLabel status = owner.subtitle(" ");
        owner.setFixedWidth(status, 310, 38);

        // Gán các ô nhập vào biến để hàm clearFields() sử dụng
        userField = user;
        passField = pass;
        statusLabel = status;

        JButton login = owner.primaryButton("ĐĂNG NHẬP");
        owner.loginButton = login;

        JButton register = owner.secondaryButton("CHƯA CÓ TÀI KHOẢN? ĐĂNG KÝ");
        owner.setFixedWidth(login, 310, 48);
        owner.setFixedWidth(register, 310, 48);

        LoginController controller = new LoginController(owner);

        login.addActionListener(e -> {
            String username = user.getText();
            String password = new String(pass.getPassword());

            pass.setText("");

            controller.login(username, password, login, status);
        });

        user.addActionListener(e -> pass.requestFocusInWindow());
        pass.addActionListener(e -> login.doClick());

        register.addActionListener(e -> {
            LoginPanel.clearFields();
            RegisterPanel.clearFields();
            owner.showPage("register");
        });

        form.add(heading);
        form.add(Box.createVerticalStrut(8));
        form.add(hint);
        form.add(Box.createVerticalStrut(28));
        form.add(userLabel);
        form.add(Box.createVerticalStrut(8));
        form.add(user);
        form.add(Box.createVerticalStrut(17));
        form.add(passLabel);
        form.add(Box.createVerticalStrut(8));
        form.add(pass);
        form.add(Box.createVerticalStrut(22));
        form.add(login);
        form.add(Box.createVerticalStrut(11));
        form.add(register);
        form.add(Box.createVerticalStrut(10));
        form.add(status);

        formSide.add(form);
        shell.add(art);
        shell.add(formSide);
        outer.add(shell);

        return outer;
    }
}
