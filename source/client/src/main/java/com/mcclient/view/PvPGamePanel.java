package com.mcclient.view;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mcclient.util.JsonUtils;
import com.mcclient.view.components.CardTile;
import com.mcclient.view.dialogs.CardUseDialog;
import com.mcclient.util.JsonTemplates;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

import static com.mcclient.view.UITheme.*;

public class PvPGamePanel extends JPanel {
    private final MainFrame owner;
    private final JLabel gameStatus;
    private final JLabel scoreLabel;
    private final JPanel ownedCardPanel;
    private final JButton skipCardButton;
    private final JTextArea logArea;

    private final JButton[] cardButtons = new JButton[PVP_BOARD_SIZE];
    private final boolean[] revealed = new boolean[PVP_BOARD_SIZE];
    private final boolean[] matched = new boolean[PVP_BOARD_SIZE];
    private final boolean[] locked = new boolean[PVP_BOARD_SIZE];
    private final boolean[] privatePeek = new boolean[PVP_BOARD_SIZE];

    private final Set<String> availableCardCodes = new LinkedHashSet<>();
    private final Set<String> usedCardCodes = new HashSet<>();
    private final List<String> ownedCardCodes = new ArrayList<>();
    private final Map<String, String> privateCardNames = new HashMap<>();
    private final Map<String, String> privateCardDescriptions = new HashMap<>();

    private boolean myTurn = false;
    private String cardPromptPhase = "";
    private String pendingCardUseCode = "";
    private boolean selectingCardTarget = false;

    public PvPGamePanel(MainFrame owner) {
        super(new BorderLayout(0, 24));
        this.owner = owner;
        setBackground(BG);
        setBorder(new EmptyBorder(30, 38, 30, 38));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JPanel info = CommonUI.vertical();
        info.add(CommonUI.eyebrow("MEMORY DUEL  /  TRẬN ĐẤU"));
        info.add(Box.createVerticalStrut(4));
        info.add(CommonUI.title("Đấu trí 1 VS 1", 24));
        gameStatus = CommonUI.subtitle("Đang chờ dữ liệu trận đấu...");
        info.add(gameStatus);
        top.add(info, BorderLayout.WEST);

        scoreLabel = CommonUI.title("0   —   0", 27);
        scoreLabel.setForeground(PURPLE_DARK);
        top.add(scoreLabel, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(14, 0));
        center.setOpaque(false);

        JPanel boardShell = CommonUI.panel(WHITE);
        boardShell.setLayout(new BorderLayout(0, 8));
        JPanel boardTitle = new JPanel(new BorderLayout());
        boardTitle.setOpaque(false);
        boardTitle.add(CommonUI.title("Bàn chơi 6 × 6", 16), BorderLayout.WEST);
        boardTitle.add(CommonUI.subtitle("36 LÁ  •  18 CẶP"), BorderLayout.EAST);

        JPanel board = new JPanel(new GridLayout(6, 6, 7, 7));
        board.setOpaque(false);
        for (int i = 0; i < PVP_BOARD_SIZE; i++) {
            final int index = i;
            CardTile b = new CardTile();
            b.setPreferredSize(new Dimension(88, 96));
            b.setMinimumSize(new Dimension(58, 62));
            b.addActionListener(e -> onTileClicked(index));
            cardButtons[i] = b;
            board.add(b);
        }

        JButton leave = CommonUI.secondaryButton("RỜI TRẬN");
        leave.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(owner.frame, "Bạn chắc chắn muốn rời trận?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) owner.send("LEAVE_MATCH", JsonTemplates.empty());
        });
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(leave, BorderLayout.EAST);

        boardShell.add(boardTitle, BorderLayout.NORTH);
        boardShell.add(board, BorderLayout.CENTER);
        boardShell.add(bottom, BorderLayout.SOUTH);

        JPanel side = CommonUI.panel(new Color(250, 247, 255));
        side.setPreferredSize(new Dimension(245, 500));
        side.setLayout(new BorderLayout(0, 10));
        side.add(CommonUI.title("THẺ ĐÃ CHỌN", 16), BorderLayout.NORTH);

        ownedCardPanel = new JPanel();
        ownedCardPanel.setOpaque(false);
        ownedCardPanel.setLayout(new BoxLayout(ownedCardPanel, BoxLayout.Y_AXIS));
        JScrollPane ownedScroll = new JScrollPane(ownedCardPanel);
        ownedScroll.setBorder(null);
        ownedScroll.getViewport().setOpaque(false);
        ownedScroll.setOpaque(false);
        side.add(ownedScroll, BorderLayout.CENTER);

        skipCardButton = CommonUI.secondaryButton("BỎ QUA THẺ");
        skipCardButton.setVisible(false);
        skipCardButton.addActionListener(e -> {
            if (cardPromptPhase.equals("END_TURN") && !availableCardCodes.isEmpty()) {
                availableCardCodes.clear();
                selectingCardTarget = false;
                pendingCardUseCode = "";
                myTurn = false;
                owner.send("SKIP_CARD", JsonTemplates.empty());
                renderPrivateCards();
                refreshBoardEnabled();
            }
        });

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        logArea.setRows(7);
        logArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setOpaque(false);
        logPanel.add(CommonUI.eyebrow("NHẬT KÝ TRẬN ĐẤU"), BorderLayout.NORTH);
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        JPanel sideBottom = new JPanel(new BorderLayout(0, 8));
        sideBottom.setOpaque(false);
        sideBottom.add(skipCardButton, BorderLayout.NORTH);
        sideBottom.add(logPanel, BorderLayout.CENTER);
        side.add(sideBottom, BorderLayout.SOUTH);

        center.add(boardShell, BorderLayout.CENTER);
        center.add(side, BorderLayout.EAST);
        add(center, BorderLayout.CENTER);
    }

    private void onTileClicked(int index) {
        if (selectingCardTarget) {
            if (matched[index] || revealed[index] || locked[index]) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            String code = pendingCardUseCode;
            selectingCardTarget = false;
            pendingCardUseCode = "";
            myTurn = false;
            owner.send("USE_CARD", JsonTemplates.useCard(code, index));
            setTurnIndicator(true);
            refreshBoardEnabled();
            return;
        }
        if (matched[index] || revealed[index] || locked[index]) return;
        if (!myTurn) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        owner.send("FLIP", JsonTemplates.flip(index));
    }

    // ==================== API GỌI TỪ SERVER MESSAGE HANDLER ====================

    public void setStatusText(String text) {
        gameStatus.setText(text);
    }

    public void onGameStart(String currentPlayer) {
        Arrays.fill(revealed, false);
        Arrays.fill(matched, false);
        Arrays.fill(locked, false);
        Arrays.fill(privatePeek, false);
        ownedCardCodes.clear();
        availableCardCodes.clear();
        usedCardCodes.clear();
        cardPromptPhase = "";
        selectingCardTarget = false;
        pendingCardUseCode = "";
        logArea.setText("");

        for (int i = 0; i < PVP_BOARD_SIZE; i++) {
            CardTile tile = (CardTile) cardButtons[i];
            tile.setSymbolIndex(-1);
            tile.setBackground(LAVENDER);
            tile.setEnabled(true);
            tile.repaint();
        }
        myTurn = owner.username.equals(currentPlayer);
        scoreLabel.setText("0   —   0");
        setTurnIndicator(myTurn);
        owner.showPage("game");
        appendLog("Trận đấu bắt đầu. Bàn chơi có 18 cặp.");
        refreshBoardEnabled();
    }

    public void onCardFlip(int index, int value, String by) {
        if (index >= 0 && index < PVP_BOARD_SIZE) {
            revealed[index] = true;
            privatePeek[index] = false;
            ((CardTile) cardButtons[index]).setSymbolIndex(value);
            cardButtons[index].setBackground(YELLOW);
            cardButtons[index].setEnabled(false);
            cardButtons[index].repaint();
        }
        if (!by.isBlank()) appendLog(by + " đã lật một lá bài.");
        if (owner.username.equals(by) && cardPromptPhase.equals("TURN")) {
            availableCardCodes.clear();
            cardPromptPhase = "";
            renderPrivateCards();
        }
        int count = 0;
        for (int j = 0; j < PVP_BOARD_SIZE; j++) if (revealed[j] && !privatePeek[j]) count++;
        if (count >= 2) for (int j = 0; j < PVP_BOARD_SIZE; j++) cardButtons[j].setEnabled(false);
    }

    public void onRoundResult(JsonNode m) {
        scoreLabel.setText(scoreForMe(JsonUtils.getInt(m, "score1", 0), JsonUtils.getInt(m, "score2", 0), m));
        boolean match = m.path("matched").asBoolean(false);
        int a = JsonUtils.getInt(m, "indexA", -1), b = JsonUtils.getInt(m, "indexB", -1);
        if (match) {
            for (int idx : new int[]{a, b}) {
                if (idx >= 0 && idx < PVP_BOARD_SIZE) {
                    matched[idx] = true;
                    revealed[idx] = false;
                    cardButtons[idx].setBackground(MINT);
                }
            }
            appendLog(JsonUtils.getString(m, "player") + " đã ghép đúng một cặp!");
            refreshBoardEnabled();
        } else {
            appendLog(JsonUtils.getString(m, "player") + " lật sai. Hai lá sẽ úp lại sau 1,5 giây.");
        }
        myTurn = owner.username.equals(JsonUtils.getString(m, "currentPlayer"));
        setTurnIndicator(myTurn);
    }

    public void onBoardReset() {
        for (int i = 0; i < PVP_BOARD_SIZE; i++) {
            if (!matched[i]) {
                revealed[i] = false;
                privatePeek[i] = false;
                ((CardTile) cardButtons[i]).setSymbolIndex(-1);
                cardButtons[i].setBackground(locked[i] ? PINK : LAVENDER);
            }
        }
        refreshBoardEnabled();
    }

    public void onTurnChanged(JsonNode m) {
        myTurn = owner.username.equals(JsonUtils.getString(m, "currentPlayer"));
        boolean keepStartOptions = myTurn && cardPromptPhase.equals("TURN") && !availableCardCodes.isEmpty();
        if (!keepStartOptions) {
            availableCardCodes.clear();
            cardPromptPhase = "";
        }
        selectingCardTarget = false;
        pendingCardUseCode = "";
        renderPrivateCards();
        scoreLabel.setText(scoreForMe(JsonUtils.getInt(m, "score1", 0), JsonUtils.getInt(m, "score2", 0), m));
        Arrays.fill(locked, false);
        int li = JsonUtils.getInt(m, "lockedIndex", -1);
        if (li >= 0 && li < PVP_BOARD_SIZE) locked[li] = true;
        for (int i = 0; i < PVP_BOARD_SIZE; i++) {
            if (!matched[i] && !revealed[i]) cardButtons[i].setBackground(locked[i] ? PINK : LAVENDER);
        }
        setTurnIndicator(myTurn);
        refreshBoardEnabled();
    }

    public void onCardPrompt(ArrayNode eligible, String phase) {
        availableCardCodes.clear();
        if (eligible != null) for (JsonNode e : eligible) availableCardCodes.add(e.asText());
        cardPromptPhase = phase;
        myTurn = true;
        renderPrivateCards();
        refreshBoardEnabled();
        setTurnIndicator(true);
    }

    public void onCardUsed(String player, String code, String name) {
        appendLog(player + " đã sử dụng " + code + " – " + name + ".");
        if (player.equals(owner.username)) {
            usedCardCodes.add(code);
            availableCardCodes.remove(code);
            renderPrivateCards();
        }
    }

    public void onPrivateReveal(int index, int value) {
        if (index < 0 || index >= PVP_BOARD_SIZE) return;
        revealed[index] = true;
        privatePeek[index] = true;
        ((CardTile) cardButtons[index]).setSymbolIndex(value);
        cardButtons[index].setBackground(MINT);
        javax.swing.Timer hideTimer = new javax.swing.Timer(2000, e -> {
            revealed[index] = false;
            privatePeek[index] = false;
            ((CardTile) cardButtons[index]).setSymbolIndex(-1);
            cardButtons[index].setBackground(locked[index] ? PINK : LAVENDER);
            refreshBoardEnabled();
        });
        hideTimer.setRepeats(false);
        hideTimer.start();
    }

    public void onLuckyPair(String player, int a, int b) {
        for (int i : new int[]{a, b}) {
            if (i >= 0 && i < PVP_BOARD_SIZE) {
                matched[i] = true;
                revealed[i] = false;
                cardButtons[i].setBackground(MINT);
                cardButtons[i].repaint();
            }
        }
        appendLog(player + " đã được Ngôi sao may mắn ghép một cặp!");
        refreshBoardEnabled();
    }

    public void refreshPrivateCards(ArrayNode cards) {
        ownedCardCodes.clear();
        privateCardNames.clear();
        privateCardDescriptions.clear();
        usedCardCodes.clear();
        if (cards != null) {
            for (JsonNode c : cards) {
                String code = JsonUtils.getString(c, "code");
                ownedCardCodes.add(code);
                privateCardNames.put(code, JsonUtils.getString(c, "name"));
                String state = JsonUtils.getString(c, "category").equals("LUCK") ? "Tự động cuối trận" : "Chưa dùng";
                privateCardDescriptions.put(code, code + " – " + JsonUtils.getString(c, "name") + "\n" + JsonUtils.getString(c, "description") + "\nTrạng thái: " + state + "\n\n");
            }
        }
        renderPrivateCards();
    }

    public void appendLog(String message) {
        logArea.append("• " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void renderPrivateCards() {
        ownedCardPanel.removeAll();
        if (ownedCardCodes.isEmpty()) {
            JLabel empty = CommonUI.subtitle("Bạn chưa chọn thẻ nào.");
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            ownedCardPanel.add(empty);
        }
        for (String code : ownedCardCodes) {
            String name = privateCardNames.getOrDefault(code, code);
            boolean eligible = availableCardCodes.contains(code);
            boolean alreadyUsed = usedCardCodes.contains(code);
            String group = code.startsWith("A") ? "A · TẤN CÔNG" : code.startsWith("S") ? "S · HỖ TRỢ" : "L · MAY MẮN";

            JPanel card = new JPanel();
            card.setOpaque(true);
            card.setBackground(eligible ? new Color(255, 250, 231) : Color.WHITE);
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(eligible ? new Color(220, 180, 105) : BORDER, eligible ? 2 : 1, true),
                    new EmptyBorder(8, 9, 8, 9)
            ));

            JLabel category = new JLabel(group);
            category.setFont(new Font("Segoe UI", Font.BOLD, 10));
            category.setForeground(PURPLE_DARK);

            JLabel title = new JLabel(name);
            title.setFont(new Font("Segoe UI", Font.BOLD, 13));
            title.setForeground(TEXT);

            String state = eligible ? "Có thể dùng" : (code.startsWith("L") || code.equals("S02") ? "Tự động" : alreadyUsed ? "Đã dùng" : "Chờ điều kiện");
            JLabel detail = new JLabel(code + " · " + state);
            detail.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            detail.setForeground(MUTED);

            card.add(category);
            card.add(Box.createVerticalStrut(3));
            card.add(title);
            card.add(Box.createVerticalStrut(3));
            card.add(detail);
            card.setToolTipText(privateCardDescriptions.getOrDefault(code, name));

            if (eligible) {
                JButton use = CommonUI.primaryButton("DÙNG THẺ");
                use.setAlignmentX(Component.LEFT_ALIGNMENT);
                CommonUI.setFixedWidth(use, 190, 34);
                use.addActionListener(e -> useOwnedCard(code));
                card.add(Box.createVerticalStrut(7));
                card.add(use);
            }
            card.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.setMaximumSize(new Dimension(220, eligible ? 116 : 82));
            ownedCardPanel.add(card);
            ownedCardPanel.add(Box.createVerticalStrut(7));
        }
        boolean endOptions = cardPromptPhase.equals("END_TURN") && !availableCardCodes.isEmpty();
        skipCardButton.setVisible(endOptions);
        skipCardButton.setText("BỎ QUA THẺ");
        ownedCardPanel.revalidate();
        ownedCardPanel.repaint();
    }

    private void useOwnedCard(String code) {
        if (!availableCardCodes.contains(code)) return;
        if (code.equals("A02") || code.equals("S01")) {
            if (!CardUseDialog.confirmTargetCard(owner.frame, code)) return;
            selectingCardTarget = true;
            pendingCardUseCode = code;
            availableCardCodes.clear();
            renderPrivateCards();
            setTurnIndicator(true);
            refreshBoardEnabled();
            return;
        }
        if (CardUseDialog.confirmUse(owner.frame, privateCardNames.getOrDefault(code, code))) {
            availableCardCodes.clear();
            myTurn = false;
            renderPrivateCards();
            refreshBoardEnabled();
            owner.send("USE_CARD", JsonTemplates.useCard(code, -1));
        }
    }

    private void setTurnIndicator(boolean ownTurn) {
        gameStatus.setText(ownTurn ? "Lượt của bạn" : "Lượt đối thủ");
        gameStatus.setForeground(ownTurn ? new Color(35, 145, 82) : new Color(205, 67, 67));
        gameStatus.setFont(new Font("Segoe UI", Font.BOLD, 15));
    }

    private String scoreForMe(int score1, int score2, JsonNode message) {
        String p1 = JsonUtils.getString(message, "player1");
        return owner.username.equals(p1) ? score1 + "   —   " + score2 : score2 + "   —   " + score1;
    }

    private void refreshBoardEnabled() {
        int openCount = 0;
        for (int i = 0; i < PVP_BOARD_SIZE; i++) if (revealed[i] && !privatePeek[i]) openCount++;
        boolean waitingEndTurnChoice = cardPromptPhase.equals("END_TURN") && !availableCardCodes.isEmpty();
        for (int i = 0; i < PVP_BOARD_SIZE; i++) {
            cardButtons[i].setEnabled(selectingCardTarget
                    ? (!matched[i] && !revealed[i] && !locked[i])
                    : (myTurn && !waitingEndTurnChoice && !matched[i] && !revealed[i] && !locked[i] && openCount < 2));
        }
    }
}