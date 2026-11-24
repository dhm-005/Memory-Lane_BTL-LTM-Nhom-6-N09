package com.cardgame.client.view.dialogs;


import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import com.cardgame.client.util.Json;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
public final class CardPurchaseDialog {
    private static volatile JDialog activeDialog;
    public static void closeActive() {
        JDialog dialog = activeDialog;
        if (dialog != null) SwingUtilities.invokeLater(dialog::dispose);
    }
    private static final Color BACKGROUND = new Color(255, 249, 241);
    private static final Color PURPLE = new Color(155, 132, 225);
    private static final Color PURPLE_DARK = new Color(125, 101, 195);
    private static final Color TEXT = new Color(55, 50, 75);
    private static final Color MUTED = new Color(145, 139, 158);
    private CardPurchaseDialog() {
    }
    public static void show(Window owner, int coins, ArrayNode cards, Consumer<ArrayNode> onSelection, Consumer<ArrayNode> onSubmit) {
        if (cards == null || cards.size() == 0) return;
        JDialog dialog = new JDialog(owner, "Chọn thẻ trước trận", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(new EmptyBorder(20, 22, 18, 22));
        content.setBackground(BACKGROUND);
        JLabel heading = label("CHỌN THẺ CỦA BẠN", 22, true);
        int spendableCoins = Math.max(0, coins - 10);
        JLabel hint = label("Chọn tối đa 2 trong 3 lá bài · Xu mua thẻ: " + spendableCoins, 14, false);
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(heading);
        header.add(Box.createVerticalStrut(5));
        header.add(hint);
        content.add(header, BorderLayout.NORTH);
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        cardsPanel.setOpaque(false);
        List<String> codes = new ArrayList<>();
        List<JToggleButton> choices = new ArrayList<>();
        JLabel totalLabel = label("Đã chọn: 0/2 · Tổng: 0 xu", 14, false);
        for (JsonNode element : cards) {
            ObjectNode cardData = (ObjectNode) element;
            String code = Json.str(cardData, "code");
            codes.add(code);
            String category = code.substring(0, 1);
            String name = Json.str(cardData, "name");
            int price = Json.integer(cardData, "price", 0);
            String description = Json.str(cardData, "description");
            JToggleButton card = new JToggleButton("<html><div style='text-align:center;padding:8px;width:145px'>"
            + "<div style='font-size:30px;color:#8C78C8;font-weight:bold'>" + category + "</div>"
            + "<div style='font-size:12px;color:#8B7BC1'>" + code + "</div><br>"
            + "<div style='font-size:15px;font-weight:bold;color:#403957'>" + name + "</div><br>"
            + "<div style='font-size:11px;color:#777080'>" + description + "</div><br>"
            + "<div style='font-size:13px;font-weight:bold;color:#B17A37'>" + price + " xu</div></div></html>");
            card.setHorizontalAlignment(SwingConstants.CENTER);
            card.setVerticalAlignment(SwingConstants.CENTER);
            card.setBackground(Color.WHITE);
            card.setFocusPainted(false);
            card.setBorder(cardBorder(false));
            card.setPreferredSize(new Dimension(190, 245));
            choices.add(card);
            cardsPanel.add(card);
        }
        Runnable refresh = () -> {
            int count = 0;
            int total = 0;
            for (int i = 0; i < choices.size(); i++) {
                boolean selected = choices.get(i).isSelected();
                if (selected) {
                    count++;
                    total += Json.integer((ObjectNode) cards.get(i), "price", 0);
                }
                choices.get(i).setBorder(cardBorder(selected));
            }
            totalLabel.setText("Đã chọn: " + count + "/2 · Tổng: " + total + " xu");
            ArrayNode current = Json.MAPPER.createArrayNode();
            for (int i = 0; i < choices.size(); i++) if (choices.get(i).isSelected()) current.add(codes.get(i));
            onSelection.accept(current);
        };
        for (JToggleButton button : choices) {
            button.addActionListener(event -> {
                int count = 0;
                int total = 0;
                for (int i = 0; i < choices.size(); i++) {
                    if (choices.get(i).isSelected()) {
                        count++;
                        total += Json.integer((ObjectNode) cards.get(i), "price", 0);
                    }
                }
                if (count > 2 || total > spendableCoins) {
                    button.setSelected(false);
                    Toolkit.getDefaultToolkit().beep();
                }
                refresh.run();
            }
            );
        }
        content.add(cardsPanel, BorderLayout.CENTER);
        JButton confirm = new JButton("XÁC NHẬN LỰA CHỌN");
        confirm.setFont(new Font("Segoe UI", Font.BOLD, 14));
        confirm.setForeground(Color.WHITE);
        confirm.setBackground(PURPLE);
        confirm.setFocusPainted(false);
        confirm.setBorderPainted(false);
        confirm.setBorder(new EmptyBorder(12, 20, 12, 20));
        JLabel timerLabel = label("Thời gian chọn: 30 giây", 14, false);
        JPanel footer = new JPanel(new BorderLayout(8, 8));
        footer.setOpaque(false);
        footer.add(totalLabel, BorderLayout.NORTH);
        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);
        actions.add(timerLabel, BorderLayout.WEST);
        actions.add(confirm, BorderLayout.EAST);
        footer.add(actions, BorderLayout.SOUTH);
        content.add(footer, BorderLayout.SOUTH);
        boolean[] submitted = {
            false
        };
        Runnable submit = () -> {
            if (submitted[0]) return;
            submitted[0] = true;
            ArrayNode selected = Json.MAPPER.createArrayNode();
            for (int i = 0; i < choices.size(); i++) if (choices.get(i).isSelected()) selected.add(codes.get(i));
            onSubmit.accept(selected);
            dialog.dispose();
        };
        confirm.addActionListener(event -> submit.run());
        dialog.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent event) {
                submit.run();
            }
        }
        );
        int[] seconds = {
            29
        };
        Timer countdown = new Timer(1000, event -> {
            seconds[0]--;
            timerLabel.setText("Còn " + seconds[0] + " giây");
            if (seconds[0] <= 0) {
                ((Timer) event.getSource()).stop();
                submit.run();
            }
        }
        );
        dialog.setContentPane(content);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(690, 360));
        dialog.setLocationRelativeTo(owner);
        countdown.start();
        activeDialog = dialog;
        dialog.setVisible(true);
        if (activeDialog == dialog) activeDialog = null;
        countdown.stop();
    }
    private static JLabel label(String text, int size, boolean bold) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size));
        label.setForeground(bold ? TEXT : MUTED);
        return label;
    }
    private static javax.swing.border.Border cardBorder(boolean selected) {
        return BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(selected ? PURPLE_DARK : new Color(226, 216, 244), selected ? 3 : 2, true),
        new EmptyBorder(8, 5, 8, 5));
    }
}
