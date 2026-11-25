package com.cardgame.client.view;

import com.cardgame.client.controller.LoginController;
import com.cardgame.client.view.components.VectorIcon;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;
import java.awt.*;
import java.util.function.BiConsumer;
import static com.cardgame.client.view.UiTheme.*;

public final class LoginPanel {
    private LoginPanel() {
    }

    // Giữ tham chiếu để xóa dữ liệu / hiện lỗi từ bên ngoài
    private static JTextField userField;
    private static JPasswordField passField;
    private static JLabel statusLabel;

    /** Xóa toàn bộ ô nhập và thông báo lỗi. */
    public static void clearFields() {
        if (userField != null) {
            userField.setText("");
            userField.putClientProperty("JComponent.outline", null);
        }
        if (passField != null) {
            passField.setText("");
            passField.putClientProperty("JComponent.outline", null);
        }
        if (statusLabel != null) statusLabel.setText(" ");
    }

    /** Hiện lỗi đỏ ngay trên form; mật khẩu bị xóa và con trỏ quay lại ô mật khẩu (quy ước chung của web). */
    public static void showError(String message) {
        if (passField != null) {
            passField.setText("");
            passField.putClientProperty("JComponent.outline", "error");
            passField.requestFocusInWindow();
        }

        if (statusLabel != null) {
            statusLabel.setText(
                message == null || message.isBlank() ? " " : message
            );
        }
    }

    /** Đặt con trỏ vào ô tên đăng nhập khi mở trang. */
    public static void focus() {
        if (userField != null) SwingUtilities.invokeLater(() -> userField.requestFocusInWindow());
    }

    private static void fieldError(JTextField field, JLabel status, String message) {
        status.setText(message);
        field.putClientProperty("JComponent.outline", "error");
        field.requestFocusInWindow();
    }

    public static JPanel create(MainFrame owner) {
        // ===== CỘT TRÁI: minh họa =====
        JPanel art = new JPanel(new GridBagLayout());
        art.setBackground(new Color(235, 226, 255));
        JPanel artContent = owner.vertical();

        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 9, 0));
        logoRow.setOpaque(false);
        JLabel logoText = new JLabel("MEMORY DUEL");
        logoText.setFont(new Font("Segoe UI", Font.BOLD, 29));
        logoText.setForeground(TEXT);
        logoRow.add(new JLabel(new VectorIcon("sparkle", 29)));
        logoRow.add(logoText);
        logoRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel tagline = new InlineWrapLabel("THINK FAST. MATCH SMART. WIN THE DUEL.", 1000, SwingConstants.CENTER);
        tagline.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tagline.setForeground(PURPLE_DARK);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel hero = new JPanel(new GridBagLayout());
        hero.setOpaque(false);
        hero.setPreferredSize(new Dimension(300, 170));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));
        hero.setMinimumSize(new Dimension(0, 170));
        hero.add(new JLabel(new VectorIcon("playing-card", 145)));
        hero.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel slogan = new InlineWrapLabel("Một chút tinh mắt,<br>một chút may mắn!", 1000, SwingConstants.CENTER);
        slogan.setFont(new Font("Segoe UI", Font.BOLD, 24));
        slogan.setForeground(TEXT);
        slogan.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel description = new InlineWrapLabel("Thử thách trí nhớ trong trận đấu 1v1", 1000, SwingConstants.CENTER);
        description.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        description.setForeground(MUTED);
        description.setAlignmentX(Component.CENTER_ALIGNMENT);

        artContent.add(logoRow);
        artContent.add(Box.createVerticalStrut(12));
        artContent.add(tagline);
        artContent.add(Box.createVerticalStrut(20));
        artContent.add(hero);
        artContent.add(Box.createVerticalStrut(12));
        artContent.add(slogan);
        artContent.add(Box.createVerticalStrut(10));
        artContent.add(description);
        GridBagConstraints ac = new GridBagConstraints();
        ac.weightx = 1;
        ac.fill = GridBagConstraints.HORIZONTAL;
        ac.insets = new Insets(24, 24, 24, 24);
        art.add(artContent, ac);

        // ===== CỘT PHẢI: form =====
        JPanel form = owner.vertical();

        // Logo gọn, chỉ hiện khi cửa sổ hẹp và cột minh họa bị ẩn
        JPanel compactBrand = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        compactBrand.setOpaque(false);
        JLabel brandText = new JLabel("MEMORY DUEL");
        brandText.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brandText.setForeground(TEXT);
        compactBrand.add(new JLabel(new VectorIcon("sparkle", 22)));
        compactBrand.add(Box.createHorizontalStrut(8));
        compactBrand.add(brandText);
        compactBrand.setAlignmentX(Component.LEFT_ALIGNMENT);
        compactBrand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        compactBrand.setVisible(false);

        JLabel heading = owner.title("Chào mừng trở lại!", 27);
        JLabel hint = owner.wrappedLabel("Đăng nhập hoặc tạo tài khoản để tham gia những trận đấu thú vị.", 420);

        JTextField user = new JTextField();
        owner.styleField(user, "Nhập tên đăng nhập");
        user.putClientProperty("JTextField.showClearButton", true);

        JPasswordField pass = new JPasswordField();
        owner.styleField(pass, "Nhập mật khẩu");
        pass.putClientProperty("JPasswordField.showRevealButton", true); // nút hiện/ẩn mật khẩu

        JLabel status = owner.errorLabel();

        userField = user;
        passField = pass;
        statusLabel = status;

        JButton login = owner.primaryButton("ĐĂNG NHẬP");
        owner.loginButton = login;
        owner.fillWidth(login, 48);

        JButton goRegister = owner.linkButton("Đăng ký ngay");
        JPanel registerRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        registerRow.setOpaque(false);
        JLabel noAccount = new JLabel("Chưa có tài khoản?");
        noAccount.setForeground(MUTED);
        noAccount.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        registerRow.add(noAccount);
        registerRow.add(goRegister);
        registerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        LoginController controller = new LoginController(owner);

        // Gõ lại thì xóa trạng thái lỗi
        DocumentListener clearError = new DocumentListener() {
            private void reset() {
                user.putClientProperty("JComponent.outline", null);
                pass.putClientProperty("JComponent.outline", null);
                if (!status.getText().isBlank()) status.setText(" ");
            }
            @Override public void insertUpdate(DocumentEvent e) { reset(); }
            @Override public void removeUpdate(DocumentEvent e) { reset(); }
            @Override public void changedUpdate(DocumentEvent e) { reset(); }
        };
        user.getDocument().addDocumentListener(clearError);
        pass.getDocument().addDocumentListener(clearError);

        login.addActionListener(e -> {
            String username = user.getText().trim();
            String password = new String(pass.getPassword());
            if (username.isEmpty()) {
                fieldError(user, status, "Vui lòng nhập tên đăng nhập.");
                return;
            }
            if (password.isEmpty()) {
                fieldError(pass, status, "Vui lòng nhập mật khẩu.");
                return;
            }
            controller.login(username, password, login, status);
        });

        // Enter: tên đăng nhập -> mật khẩu -> đăng nhập
        user.addActionListener(e -> pass.requestFocusInWindow());
        pass.addActionListener(e -> login.doClick());

        goRegister.addActionListener(e -> {
            LoginPanel.clearFields();
            RegisterPanel.clearFields();
            owner.showPage("register");
        });

        form.add(compactBrand);
        form.add(Box.createVerticalStrut(10));
        form.add(heading);
        form.add(Box.createVerticalStrut(8));
        form.add(hint);
        form.add(Box.createVerticalStrut(24));
        form.add(owner.eyebrow("TÊN ĐĂNG NHẬP"));
        form.add(Box.createVerticalStrut(7));
        form.add(user);
        form.add(Box.createVerticalStrut(15));
        form.add(owner.eyebrow("MẬT KHẨU"));
        form.add(Box.createVerticalStrut(7));
        form.add(pass);
        form.add(Box.createVerticalStrut(8));
        form.add(status);
        form.add(Box.createVerticalStrut(8));
        form.add(login);
        form.add(Box.createVerticalStrut(12));
        form.add(registerRow);

        return InlineAuthShell.build(art, hero, form, compactBrand, BG, WHITE, BORDER, 920, 570);
    }


    // Helpers embedded to preserve the original file count.
    /**
     * Đặt một khung con ở giữa, kích thước tối đa maxW x maxH nhưng tự co lại cho vừa cửa sổ.
     * onSize (nếu có) được gọi trước khi đặt kích thước để khung con đổi bố cục theo không gian còn lại.
     */
    private static final class InlineCenterFitLayout implements LayoutManager {
        private final int maxW, maxH, margin;
        private final BiConsumer<Integer, Integer> onSize;

        public InlineCenterFitLayout(int maxW, int maxH, int margin, BiConsumer<Integer, Integer> onSize) {
            this.maxW = maxW;
            this.maxH = maxH;
            this.margin = margin;
            this.onSize = onSize;
        }

        @Override public void addLayoutComponent(String name, Component comp) { }
        @Override public void removeLayoutComponent(Component comp) { }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return new Dimension(maxW + margin * 2, maxH + margin * 2);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return new Dimension(0, 0);
        }

        @Override
        public void layoutContainer(Container parent) {
            Insets in = parent.getInsets();
            int availW = parent.getWidth() - in.left - in.right - margin * 2;
            int availH = parent.getHeight() - in.top - in.bottom - margin * 2;
            int w = Math.max(0, Math.min(maxW, availW));
            int h = Math.max(0, Math.min(maxH, availH));
            if (onSize != null) onSize.accept(w, h);
            int x = in.left + (parent.getWidth() - in.left - in.right - w) / 2;
            int y = in.top + (parent.getHeight() - in.top - in.bottom - h) / 2;
            for (Component c : parent.getComponents()) {
                if (c.isVisible()) c.setBounds(x, y, w, h);
            }
        }
    }

    /**
     * Khung dùng chung cho Đăng nhập / Đăng ký, hành xử như một trang web responsive:
     * - Màn hình rộng: hai cột (minh họa | form).
     * - Màn hình hẹp: ẩn cột minh họa, form chiếm toàn bộ và hiện logo gọn phía trên.
     * - Màn hình thấp: ẩn hình minh họa lớn; nếu vẫn thiếu chỗ thì form cuộn dọc, không đè chữ.
     */
    private static final class InlineAuthShell {
        private InlineAuthShell() {
        }

        private static final class FormHolder extends JPanel implements Scrollable {
            FormHolder() {
                super(new GridBagLayout());
            }

            @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
            @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 18; }
            @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 80; }
            @Override public boolean getScrollableTracksViewportWidth() { return true; }

            @Override
            public boolean getScrollableTracksViewportHeight() {
                return !(getParent() instanceof JViewport vp) || vp.getHeight() >= getPreferredSize().height;
            }
        }

        public static JPanel build(JComponent art, JComponent heroToHide, JComponent formContent,
                                   JComponent compactBrand, Color pageBg, Color cardBg, Color borderColor,
                                   int maxW, int maxH) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(cardBg);
            card.setBorder(BorderFactory.createLineBorder(borderColor));

            FormHolder holder = new FormHolder();
            holder.setBackground(cardBg);
            GridBagConstraints gc = new GridBagConstraints();
            gc.gridx = 0;
            gc.gridy = 0;
            gc.weightx = 1;
            gc.weighty = 1;
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.anchor = GridBagConstraints.CENTER;
            gc.insets = new Insets(20, 36, 20, 36);
            holder.add(formContent, gc);

            JScrollPane formScroll = new JScrollPane(holder,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            formScroll.setBorder(null);
            formScroll.getViewport().setBackground(cardBg);
            formScroll.getVerticalScrollBar().setUnitIncrement(18);

            card.add(art, BorderLayout.WEST);
            card.add(formScroll, BorderLayout.CENTER);

            JPanel outer = new JPanel(null);
            outer.setBackground(pageBg);
            outer.setLayout(new InlineCenterFitLayout(maxW, maxH, 16, (w, h) -> {
                boolean wide = w >= 720;
                art.setVisible(wide);
                compactBrand.setVisible(!wide);
                heroToHide.setVisible(h >= 520);
                art.setPreferredSize(new Dimension(w / 2, 10));
            }));
            outer.add(card);
            return outer;
        }
    }

    /**
     * JLabel tự xuống dòng theo bề rộng thật của khung chứa.
     * Chiều cao ưu tiên được tính lại theo bề rộng hiện tại nên chữ không bao giờ bị cắt hay đè lên thành phần khác.
     */
    private static class InlineWrapLabel extends JLabel {
        private final int maxWidth;
        private final int align;
        private int knownWidth = -1;

        public InlineWrapLabel(String text, int maxWidth) {
            this(text, maxWidth, SwingConstants.LEFT);
        }

        public InlineWrapLabel(String text, int maxWidth, int alignment) {
            super((String) null, alignment);
            this.maxWidth = maxWidth;
            this.align = alignment;
            setText(text);
        }

        private String toHtml(String text) {
            if (text == null) return null;
            if (text.regionMatches(true, 0, "<html>", 0, 6)) return text;
            String a = align == SwingConstants.CENTER ? "center" : align == SwingConstants.RIGHT ? "right" : "left";
            return "<html><div style='text-align:" + a + "'>" + text + "</div></html>";
        }

        @Override
        public void setText(String text) {
            super.setText(toHtml(text));
        }

        private int availableWidth() {
            Container p = getParent();
            if (p == null || p.getWidth() <= 0) return maxWidth;
            Insets pi = p.getInsets();
            int w = p.getWidth() - pi.left - pi.right;
            return Math.min(maxWidth, Math.max(40, w));
        }

        @Override
        public Dimension getPreferredSize() {
            View view = (View) getClientProperty(BasicHTML.propertyKey);
            if (view == null) return super.getPreferredSize();
            Insets in = getInsets();
            int avail = Math.max(40, availableWidth() - in.left - in.right);
            view.setSize(avail, 0);
            int h = (int) Math.ceil(view.getPreferredSpan(View.Y_AXIS)) + in.top + in.bottom;
            return new Dimension(avail + in.left + in.right, Math.max(h, getFontMetrics(getFont()).getHeight()));
        }

        @Override
        public Dimension getMinimumSize() {
            return new Dimension(0, getPreferredSize().height);
        }

        @Override
        public Dimension getMaximumSize() {
            Insets in = getInsets();
            return new Dimension(maxWidth + in.left + in.right, getPreferredSize().height);
        }

        @Override
        public void setBounds(int x, int y, int w, int h) {
            boolean widthChanged = w != getWidth();
            super.setBounds(x, y, w, h);
            if (widthChanged && w != knownWidth) {
                knownWidth = w;
                SwingUtilities.invokeLater(this::revalidate);
            }
        }
    }

}
