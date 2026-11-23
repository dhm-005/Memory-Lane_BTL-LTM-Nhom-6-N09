package com.mcclient.view;

import com.mcclient.view.components.CardTile;
import com.mcclient.view.dialogs.SoloResultDialog;
import com.mcclient.util.JsonTemplates;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


import static com.mcclient.view.UITheme.*;

public class SoloGamePanel extends JPanel {
    private final MainFrame owner;
    private final JPanel soloBoard;
    private final JLabel soloLevelLabel, soloTimerLabel, soloPairsLabel, soloStatusLabel, soloRecordLabel;

    private final JButton[] soloTiles = new JButton[64];
    private int[] soloDeck = new int[64];
    private boolean[] soloMatched = new boolean[64];
    private boolean[] soloEverFlipped = new boolean[64];
    private int soloLevel = 1, soloPairsFound = 0, soloPairCount = 2;
    private int soloFirst = -1, soloSecond = -1;
    private long soloStartedAt = 0L;
    private Timer soloClock, soloResolveTimer;
    private boolean soloBusy = false, soloFinished = false;

    public SoloGamePanel(MainFrame owner) {
        super(new BorderLayout(0, 24));
        this.owner = owner;
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(30, 38, 30, 38));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel labels = CommonUI.vertical();
        labels.add(CommonUI.eyebrow("MEMORY DUEL  /  CHIẾN DỊCH ĐƠN"));
        labels.add(CommonUI.title("Chinh phục 5 cấp độ", 25));
        labels.add(CommonUI.subtitle("Một đồng hồ duy nhất cho toàn bộ hành trình."));
        header.add(labels, BorderLayout.WEST);

        JButton exit = CommonUI.secondaryButton("THOÁT CHẾ ĐỘ ĐƠN");
        exit.addActionListener(e -> {
            stopTimers();
            owner.showPage("lobby");
        });
        header.add(exit, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setOpaque(false);

        JPanel stats = CommonUI.panel(WHITE);
        stats.setLayout(new GridLayout(1, 3, 12, 0));
        soloLevelLabel = CommonUI.title("LEVEL 1 / 5", 19);
        soloTimerLabel = CommonUI.title("00:00", 22);
        soloPairsLabel = CommonUI.title("0 / 2 cặp", 19);
        stats.add(soloLevelLabel);
        stats.add(soloTimerLabel);
        stats.add(soloPairsLabel);

        soloStatusLabel = CommonUI.subtitle("Lật hai lá mỗi lượt và tìm tất cả các cặp.");
        soloRecordLabel = CommonUI.subtitle("Thành tích tốt nhất: chưa có");

        JPanel status = new JPanel(new BorderLayout(0, 5));
        status.setOpaque(false);
        status.add(soloStatusLabel, BorderLayout.NORTH);
        status.add(soloRecordLabel, BorderLayout.SOUTH);

        soloBoard = new JPanel(new GridLayout(2, 2, 7, 7));
        soloBoard.setOpaque(false);

        JPanel boardShell = CommonUI.panel(WHITE);
        boardShell.setLayout(new BorderLayout(0, 10));
        boardShell.add(status, BorderLayout.NORTH);
        boardShell.add(soloBoard, BorderLayout.CENTER);

        content.add(stats, BorderLayout.NORTH);
        content.add(boardShell, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    public void startCampaign() {
        soloLevel = 1;
        soloPairsFound = 0;
        soloStartedAt = System.currentTimeMillis();
        soloFinished = false;
        stopTimers();
        soloClock = new Timer(250, e -> updateClock());
        soloClock.start();
        owner.send("SOLO_START", JsonTemplates.empty());
        owner.send("SOLO_BEST", JsonTemplates.empty());
        owner.showPage("solo");
        setupLevel();
    }

    public void updateBestRecord(int bestLevel, long bestTimeMs) {
        soloRecordLabel.setText(bestLevel <= 0
                ? "Thành tích tốt nhất: chưa có"
                : (bestTimeMs >= 0
                        ? "Thành tích tốt nhất: hoàn thành 5 level · " + formatTime(bestTimeMs)
                        : "Thành tích tốt nhất: đã hoàn thành đến Level " + bestLevel));
    }

    public void onRewardGranted() {
        soloStatusLabel.setText(soloStatusLabel.getText() + "  (+20 xu đã cộng)");
    }

    private void setupLevel() {
        soloPairCount = switch (soloLevel) { case 1 -> 2; case 2 -> 4; case 3 -> 8; case 4 -> 12; default -> 18; };
        int[] dims = switch (soloLevel) { case 1 -> new int[]{2, 2}; case 2 -> new int[]{2, 4}; case 3 -> new int[]{4, 4}; case 4 -> new int[]{4, 6}; default -> new int[]{6, 6}; };
        soloPairsFound = 0;
        soloFirst = soloSecond = -1;
        soloBusy = false;
        soloMatched = new boolean[64];
        soloEverFlipped = new boolean[64];
        soloDeck = new int[64];

        List<Integer> values = new ArrayList<>();
        List<Integer> symbols = new ArrayList<>();
        for (int i = 0; i < SYMBOL_NAMES.length; i++) symbols.add(i);
        Collections.shuffle(symbols);
        for (int i = 0; i < soloPairCount; i++) {
            values.add(symbols.get(i));
            values.add(symbols.get(i));
        }
        Collections.shuffle(values);
        Arrays.fill(soloDeck, -1);
        for (int i = 0; i < values.size(); i++) soloDeck[i] = values.get(i);

        soloBoard.removeAll();
        soloBoard.setLayout(new GridLayout(dims[0], dims[1], 7, 7));
        for (int i = 0; i < values.size(); i++) {
            final int index = i;
            CardTile tile = new CardTile();
            tile.setPreferredSize(new Dimension(70, 76));
            tile.addActionListener(e -> flip(index));
            soloTiles[i] = tile;
            soloBoard.add(tile);
        }
        for (int i = values.size(); i < 64; i++) soloTiles[i] = null;

        soloLevelLabel.setText("LEVEL " + soloLevel + " / 5 · " + dims[0] + "×" + dims[1]);
        soloPairsLabel.setText("0 / " + soloPairCount + " cặp");
        soloStatusLabel.setText("Tìm đủ " + soloPairCount + " cặp để mở cấp tiếp theo.");
        soloBoard.revalidate();
        soloBoard.repaint();
        updateClock();
    }

    private void flip(int index) {
        if (soloFinished || soloBusy || soloMatched[index] || soloTiles[index] == null || index == soloFirst) return;
        CardTile tile = (CardTile) soloTiles[index];
        soloEverFlipped[index] = true;
        tile.setEverFlipped(true);
        tile.setSymbolIndex(soloDeck[index]);
        tile.setBackground(Color.WHITE);
        tile.repaint();

        if (soloFirst < 0) {
            soloFirst = index;
            return;
        }
        soloSecond = index;
        soloBusy = true;

        if (soloDeck[soloFirst] == soloDeck[soloSecond]) {
            soloMatched[soloFirst] = soloMatched[soloSecond] = true;
            soloPairsFound++;
            soloTiles[soloFirst].setBackground(MINT);
            soloTiles[soloSecond].setBackground(MINT);
            soloFirst = soloSecond = -1;
            soloBusy = false;
            soloPairsLabel.setText(soloPairsFound + " / " + soloPairCount + " cặp");
            if (soloPairsFound == soloPairCount) finishLevel();
        } else {
            soloResolveTimer = new Timer(850, e -> {
                CardTile first = (CardTile) soloTiles[soloFirst];
                CardTile second = (CardTile) soloTiles[soloSecond];
                first.setSymbolIndex(-1);
                second.setSymbolIndex(-1);
                first.setEverFlipped(soloEverFlipped[soloFirst]);
                second.setEverFlipped(soloEverFlipped[soloSecond]);
                first.setBackground(LAVENDER);
                second.setBackground(LAVENDER);
                first.repaint();
                second.repaint();
                soloFirst = soloSecond = -1;
                soloBusy = false;
            });
            soloResolveTimer.setRepeats(false);
            soloResolveTimer.start();
        }
    }

    private void finishLevel() {
        long elapsed = System.currentTimeMillis() - soloStartedAt;
        soloRecordLabel.setText(soloLevel == 5
                ? "Thành tích tốt nhất hiện tại: hoàn thành 5 level · " + formatTime(elapsed)
                : "Thành tích tốt nhất hiện tại: đã hoàn thành đến Level " + soloLevel);
        owner.send(
            "SOLO_LEVEL_COMPLETE", 
            JsonTemplates.soloLevelComplete(
                    soloLevel,
                    soloLevel == 5 ? (int) Math.min(Integer.MAX_VALUE, elapsed) : -1
            )
        );

        if (soloLevel == 4) soloStatusLabel.setText("Hoàn thành Level 4! Bạn nhận mốc thưởng +20 xu.");
        if (soloLevel == 5) {
            soloFinished = true;
            stopTimers();
            soloStatusLabel.setText("XUẤT SẮC! Hoàn thành cả 5 level trong " + formatTime(elapsed) + ". Tổng thưởng: 40 xu.");
            SoloResultDialog.show(owner.frame, formatTime(elapsed), 40);
            return;
        }
        int next = soloLevel + 1;
        Timer advance = new Timer(1000, e -> {
            soloLevel = next;
            setupLevel();
        });
        advance.setRepeats(false);
        advance.start();
        if (soloLevel < 4) soloStatusLabel.setText("Hoàn thành Level " + soloLevel + "! Chuẩn bị sang cấp tiếp theo...");
    }

    private void updateClock() {
        if (soloStartedAt > 0) soloTimerLabel.setText(formatTime(System.currentTimeMillis() - soloStartedAt));
    }

    private String formatTime(long millis) {
        long sec = millis / 1000;
        return String.format("%02d:%02d", sec / 60, sec % 60);
    }

    public void stopTimers() {
        if (soloClock != null) soloClock.stop();
        if (soloResolveTimer != null) soloResolveTimer.stop();
    }
}