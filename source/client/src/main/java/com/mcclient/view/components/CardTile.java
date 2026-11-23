package com.mcclient.view.components;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import static com.mcclient.view.UITheme.TEXT;
import static com.mcclient.view.UITheme.LAVENDER;
import static com.mcclient.view.UITheme.PINK;
import static com.mcclient.view.UITheme.SYMBOL_NAMES;

public class CardTile extends JButton {
        private boolean hover = false;
        private int symbolIndex = -1;
        private boolean everFlipped = false;

        public void setEverFlipped(boolean value) { everFlipped = value; repaint(); }

        public CardTile() {
            setFont(new Font("Segoe UI", Font.BOLD, 30));
            setForeground(TEXT);
            setBackground(LAVENDER);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(100, 125));
            setMinimumSize(new Dimension(65, 80));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        public void setSymbolIndex(int index) { symbolIndex = index; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            // Do not call JButton's disabled-state painter: it fades the card while
            // the two revealed cards are temporarily non-clickable after a miss.
            // The card artwork must remain exactly the same until BOARD_RESET.
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            int w = getWidth(), h = getHeight();
            double ratio = 0.80;
            int cardW = w, cardH = h;
            if ((double) w / Math.max(1, h) > ratio) cardW = (int) (h * ratio);
            else cardH = (int) (w / ratio);
            int x = (w - cardW) / 2, y = (h - cardH) / 2;
            int arc = Math.max(12, Math.min(22, cardW / 5));

            g2.setColor(new Color(39, 35, 67, 34));
            g2.fillRoundRect(x + 2, y + 4, cardW - 5, cardH - 7, arc, arc);

            boolean faceUp = symbolIndex >= 0 && symbolIndex < SYMBOL_NAMES.length;
            boolean lockedCard = !faceUp && PINK.equals(getBackground());
            if (faceUp) {
                Color face = getBackground();
                if (face == null || face.equals(LAVENDER)) face = new Color(255, 251, 242);
                g2.setColor(face);
            } else if (lockedCard) {
                g2.setColor(new Color(249, 225, 233));
            } else {
                g2.setColor(new Color(255, 249, 232));
            }
            g2.fillRoundRect(x + 1, y + 1, cardW - 5, cardH - 7, arc, arc);

            if (!faceUp && !lockedCard) {
                // Bright ivory-and-pastel card back with a soft center medallion.
                g2.setPaint(new GradientPaint(x, y, new Color(255, 253, 244), x + cardW, y + cardH, new Color(229, 241, 255)));
                g2.fillRoundRect(x + 1, y + 1, cardW - 5, cardH - 7, arc, arc);
                g2.setColor(new Color(205, 190, 232));
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawRoundRect(x + 6, y + 6, cardW - 15, cardH - 17, arc - 5, arc - 5);
                g2.setColor(new Color(239, 211, 151));
                g2.setStroke(new BasicStroke(1.1f));
                g2.drawRoundRect(x + 10, y + 10, cardW - 23, cardH - 25, arc - 7, arc - 7);
                int cx = x + cardW / 2, cy = y + cardH / 2;
                int r = Math.max(12, Math.min(cardW, cardH) / 4);
                g2.setColor(new Color(238, 229, 250));
                g2.fillOval(cx - r, cy - r, r * 2, r * 2);
                g2.setColor(new Color(255, 255, 255, 210));
                g2.drawOval(cx - r + 3, cy - r + 3, r * 2 - 6, r * 2 - 6);
                int star = Math.max(17, Math.min(34, cardW / 2));
                IconPainter.draw(g2, "sparkle", cx - star / 2, cy - star / 2, star);
                g2.setColor(new Color(213, 190, 145));
                int dot = Math.max(3, cardW / 17);
                g2.fillOval(x + 14, y + 14, dot, dot);
                g2.fillOval(x + cardW - 14 - dot, y + 14, dot, dot);
                g2.fillOval(x + 14, y + cardH - 19 - dot, dot, dot);
                g2.fillOval(x + cardW - 14 - dot, y + cardH - 19 - dot, dot, dot);
                g2.setColor(new Color(145, 126, 178));
                g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(7, Math.min(9, cardW / 8))));
                String label = "MEMORY";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label, cx - fm.stringWidth(label) / 2, y + cardH - 13);
                if (everFlipped) {
                    int mark = Math.max(5, cardW / 12);
                    g2.setColor(new Color(246, 207, 125));
                    g2.fillOval(x + cardW - mark - 12, y + 12, mark, mark);
                    g2.setColor(new Color(255, 245, 220));
                    g2.drawOval(x + cardW - mark - 12, y + 12, mark, mark);
                }
            } else if (lockedCard) {
                g2.setColor(new Color(225, 160, 180));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(x + 6, y + 6, cardW - 15, cardH - 17, arc - 5, arc - 5);
                int size = Math.min(34, Math.min(cardW, cardH) - 18);
                IconPainter.draw(g2, "lock", x + (cardW - size) / 2, y + (cardH - size) / 2, size);
            } else {
                // Front: warm paper surface, pastel halo and crisp, colorful sticker icon.
                g2.setColor(new Color(255, 255, 255, 210));
                g2.setStroke(new BasicStroke(1.3f));
                g2.drawRoundRect(x + 5, y + 5, cardW - 13, cardH - 15, arc - 4, arc - 4);
                Color[] accents = {
                    new Color(255, 225, 205), new Color(207, 229, 255), new Color(255, 236, 177), new Color(216, 241, 210),
                    new Color(237, 218, 255), new Color(255, 215, 225), new Color(205, 241, 235), new Color(255, 224, 190)
                };
                Color accent = accents[symbolIndex % accents.length];
                int cx = x + cardW / 2, cy = y + cardH / 2;
                int halo = Math.min(cardW - 8, cardH - 8);
                g2.setColor(accent);
                g2.fillOval(cx - halo / 2, cy - halo / 2, halo, halo);
                g2.setColor(new Color(255, 255, 255, 190));
                g2.drawOval(cx - halo / 2 + 3, cy - halo / 2 + 3, halo - 6, halo - 6);
                String name = SYMBOL_NAMES[symbolIndex];
                // Use crisp Java2D vector artwork instead of glossy sticker PNGs.
                int iconSize = Math.max(28, Math.min(64, Math.min(cardW, cardH) - 13));
                IconPainter.draw(g2, name, cx - iconSize / 2, cy - iconSize / 2, iconSize);
            }

            // Crisp outer edge, independent of enabled/disabled button state.
            g2.setColor(new Color(255, 255, 255, faceUp ? 220 : 95));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(x + 1, y + 1, cardW - 6, cardH - 8, arc, arc);
            if (hover && isEnabled() && !faceUp) {
                g2.setColor(new Color(255, 255, 255, 28));
                g2.fillRoundRect(x + 3, y + 3, cardW - 10, cardH - 12, arc - 2, arc - 2);
            }
            g2.dispose();
        }
    }


    // =====================================================
