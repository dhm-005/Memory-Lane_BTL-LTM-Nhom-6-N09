package com.mcclient.view;

import com.mcclient.view.components.VectorIcon;
import com.mcclient.controller.LoginController;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import static com.mcclient.view.UITheme.*;

public final class RegisterPanel {

    private RegisterPanel() {
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
        JPanel form = CommonUI.vertical();
        form.setBorder(new EmptyBorder(28, 0, 28, 0));
        JLabel heading = CommonUI.title("Tạo tài khoản", 27);
        JLabel hint = CommonUI.wrappedLabel("Đăng ký tài khoản để lưu xu, thành tích và tham gia các trận đấu.", 310);
        JTextField user = new JTextField();
        CommonUI.styleField(user, "Nhập tên đăng nhập");
        JPasswordField pass = new JPasswordField();
        CommonUI.styleField(pass, "Nhập mật khẩu");
        JPasswordField confirm = new JPasswordField();
        CommonUI.styleField(confirm, "Nhập lại mật khẩu");
        JLabel status = CommonUI.subtitle(" ");
        CommonUI.setFixedWidth(status, 310, 38);
        JButton submit = CommonUI.primaryButton("ĐĂNG KÝ");
        owner.registerButton = submit;
        CommonUI.setFixedWidth(submit, 310, 48);
        JButton back = CommonUI.secondaryButton("ĐÃ CÓ TÀI KHOẢN? ĐĂNG NHẬP");
        CommonUI.setFixedWidth(back, 310, 44);
        LoginController controller = new LoginController(owner);
        submit.addActionListener(e -> controller.register(
                user.getText(), new String(pass.getPassword()), new String(confirm.getPassword()), submit, status));
        back.addActionListener(e -> owner.showPage("login"));
        form.add(heading);
        form.add(Box.createVerticalStrut(8));
        form.add(hint);
        form.add(Box.createVerticalStrut(22));
        form.add(CommonUI.eyebrow("TÊN ĐĂNG NHẬP"));
        form.add(Box.createVerticalStrut(7));
        form.add(user);
        form.add(Box.createVerticalStrut(13));
        form.add(CommonUI.eyebrow("MẬT KHẨU"));
        form.add(Box.createVerticalStrut(7));
        form.add(pass);
        form.add(Box.createVerticalStrut(13));
        form.add(CommonUI.eyebrow("XÁC NHẬN MẬT KHẨU"));
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
