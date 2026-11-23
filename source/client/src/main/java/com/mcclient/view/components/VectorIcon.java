package com.mcclient.view.components;

import java.awt.*;
import javax.swing.Icon;

public class VectorIcon implements Icon {
        private final String type;
        private final int size;

        public VectorIcon(String type, int size) {
            this.type = type;
            this.size = size;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(
                Component c,
                Graphics g,
                int x,
                int y
        ) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            IconPainter.draw(g2, type, x, y, size);
            g2.dispose();
        }
    }
