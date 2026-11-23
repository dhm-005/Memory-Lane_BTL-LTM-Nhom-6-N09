package com.mcclient.view;

import com.mcclient.controller.LoginController;
import com.mcclient.view.components.VectorIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static com.mcclient.view.UITheme.*;

public final class LoginPanel {
    private LoginPanel() { }

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

        JPanel artContent = CommonUI.vertical();
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
                        + "Thử thách trí nhớ trong trận đấu 1 VS 1"
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

        JPanel form = CommonUI.vertical();
        form.setBorder(new EmptyBorder(35, 0, 35, 0));

        JLabel heading = CommonUI.title("Chào mừng trở lại!", 27);
        JLabel hint = CommonUI.wrappedLabel(
                "Đăng nhập hoặc tạo tài khoản để "
                        + "tham gia những trận đấu thú vị.",
                310
        );

        JLabel userLabel = CommonUI.eyebrow("TÊN ĐĂNG NHẬP");
        JTextField user = new JTextField();
        CommonUI.styleField(user, "Nhập tên đăng nhập");

        JLabel passLabel = CommonUI.eyebrow("MẬT KHẨU");
        JPasswordField pass = new JPasswordField();
        CommonUI.styleField(pass, "Nhập mật khẩu");

        JLabel status = CommonUI.subtitle(" ");
        CommonUI.setFixedWidth(status, 310, 38);

        JButton login = CommonUI.primaryButton("ĐĂNG NHẬP");
        owner.loginButton = login;
        JButton register = CommonUI.secondaryButton("CHƯA CÓ TÀI KHOẢN? ĐĂNG KÝ");

        CommonUI.setFixedWidth(login, 310, 48);
        CommonUI.setFixedWidth(register, 310, 48);

        LoginController controller = new LoginController(owner);
        login.addActionListener(e -> controller.login(
                user.getText(), new String(pass.getPassword()), login, status));

        register.addActionListener(e -> owner.showPage("register"));

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