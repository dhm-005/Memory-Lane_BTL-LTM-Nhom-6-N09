package com.mcclient.view.components;

import java.awt.*;
import java.awt.geom.*;

import static com.mcclient.view.UITheme.PURPLE_DARK;
import static com.mcclient.view.UITheme.TEXT;

public final class IconPainter {
    private IconPainter() { }

    public static void draw(Graphics2D g, String type, int x, int y, int size) {

        Graphics2D g2 = (Graphics2D) g.create();

        g2.translate(x, y);
        g2.scale(size / 100.0, size / 100.0);

        g2.setStroke(new BasicStroke(
                5f,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        ));

        switch (type) {

            case "rock" -> {
                g2.setColor(new Color(230, 184, 132));
                g2.fillRoundRect(22, 34, 58, 48, 24, 24);
                g2.fillOval(17, 28, 23, 31); g2.fillOval(34, 17, 23, 34); g2.fillOval(51, 19, 22, 33); g2.fillOval(67, 30, 18, 28);
                g2.setColor(new Color(177, 126, 86)); g2.setStroke(new BasicStroke(3)); g2.drawRoundRect(22,34,58,48,24,24);
            }
            case "paper" -> {
                g2.setColor(new Color(255, 220, 177));
                g2.fillRoundRect(23, 38, 54, 47, 18, 18);
                g2.fillRoundRect(18, 14, 15, 42, 10, 10); g2.fillRoundRect(35, 8, 15, 48, 10, 10); g2.fillRoundRect(52, 12, 15, 44, 10, 10); g2.fillRoundRect(69, 25, 13, 31, 10, 10);
                g2.setColor(new Color(198, 148, 111)); g2.setStroke(new BasicStroke(3)); g2.drawRoundRect(23,38,54,47,18,18);
            }
            case "scissors" -> {
                g2.setColor(new Color(245, 198, 153));
                g2.fillRoundRect(22, 43, 56, 42, 20, 20);
                g2.fillRoundRect(25, 9, 17, 48, 10, 10); g2.fillRoundRect(58, 9, 17, 48, 10, 10);
                g2.setColor(new Color(189, 131, 91)); g2.setStroke(new BasicStroke(3)); g2.drawRoundRect(22,43,56,42,20,20);
            }
            case "sparkle" -> {
                g2.setColor(new Color(255, 194, 83));
                Polygon star = new Polygon(
                        new int[]{50, 60, 92, 60, 50, 40, 8, 40},
                        new int[]{5, 40, 50, 60, 95, 60, 50, 40},
                        8
                );
                g2.fillPolygon(star);

                g2.setColor(Color.WHITE);
                g2.fillOval(45, 45, 10, 10);
            }

            case "playing-card", "playing-card-small" -> {
                int offset = type.equals("playing-card") ? 0 : 10;
                int w = 70 - offset;
                int h = 88 - offset;

                g2.setColor(new Color(255, 255, 255, 110));
                g2.fillRoundRect(20 + offset, 9 + offset, w, h, 12, 12);

                g2.setColor(new Color(255, 255, 255));
                g2.fillRoundRect(12, 5, w, h, 12, 12);

                g2.setColor(new Color(151, 128, 222));
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(12, 5, w, h, 12, 12);

                g2.setColor(new Color(255, 128, 157));
                g2.fillOval(28, 28, 20, 20);

                g2.setColor(new Color(255, 194, 83));
                g2.fillOval(43, 45, 18, 18);

                g2.setColor(new Color(151, 128, 222));
                g2.fillOval(25, 62, 14, 14);
            }

            case "apple" -> {
                g2.setColor(new Color(231, 83, 100));
                g2.fillOval(15, 28, 40, 53);
                g2.fillOval(45, 28, 40, 53);

                g2.setColor(new Color(105, 174, 104));
                g2.fill(new Ellipse2D.Double(49, 8, 32, 18));

                g2.setColor(new Color(116, 78, 54));
                g2.setStroke(new BasicStroke(6));
                g2.drawLine(49, 27, 55, 13);

                g2.setColor(new Color(255, 255, 255, 180));
                g2.fillOval(26, 42, 8, 15);
            }

            case "rocket" -> {
                g2.setColor(new Color(151, 128, 222));
                Polygon body = new Polygon(
                        new int[]{50, 72, 70, 50, 30, 28},
                        new int[]{5, 38, 65, 88, 65, 38},
                        6
                );
                g2.fillPolygon(body);

                g2.setColor(new Color(255, 255, 255));
                g2.fillOval(41, 32, 18, 18);

                g2.setColor(new Color(255, 143, 113));
                g2.fillOval(44, 35, 12, 12);

                g2.setColor(new Color(255, 180, 79));
                g2.fillOval(42, 77, 16, 18);

                g2.setColor(new Color(255, 119, 143));
                g2.fillPolygon(
                        new int[]{30, 12, 29},
                        new int[]{57, 78, 72},
                        3
                );
                g2.fillPolygon(
                        new int[]{70, 88, 71},
                        new int[]{57, 78, 72},
                        3
                );
            }

            case "headphones" -> {
                g2.setColor(new Color(151, 128, 222));
                g2.setStroke(new BasicStroke(12));
                g2.drawArc(15, 8, 70, 80, 180, 180);

                g2.setColor(new Color(255, 143, 169));
                g2.fillRoundRect(9, 47, 23, 38, 10, 10);
                g2.fillRoundRect(68, 47, 23, 38, 10, 10);

                g2.setColor(new Color(255, 255, 255));
                g2.fillRoundRect(15, 53, 10, 25, 7, 7);
                g2.fillRoundRect(75, 53, 10, 25, 7, 7);
            }

            case "soccer" -> {
                g2.setColor(Color.WHITE);
                g2.fillOval(8, 8, 84, 84);

                g2.setColor(new Color(70, 75, 91));
                g2.setStroke(new BasicStroke(4));
                g2.drawOval(8, 8, 84, 84);

                g2.fillPolygon(
                        new int[]{50, 64, 59, 41, 36},
                        new int[]{30, 40, 58, 58, 40},
                        5
                );

                g2.drawLine(50, 30, 50, 9);
                g2.drawLine(64, 40, 88, 31);
                g2.drawLine(59, 58, 75, 82);
                g2.drawLine(41, 58, 25, 82);
                g2.drawLine(36, 40, 12, 31);
            }

            case "gamepad" -> {
                g2.setColor(new Color(151, 128, 222));
                g2.fillRoundRect(7, 28, 86, 48, 20, 20);

                g2.setColor(Color.WHITE);
                g2.fillRect(25, 43, 6, 22);
                g2.fillRect(17, 51, 22, 6);

                g2.setColor(new Color(255, 210, 104));
                g2.fillOval(63, 43, 10, 10);

                g2.setColor(new Color(255, 143, 169));
                g2.fillOval(76, 55, 10, 10);
            }

            case "moon" -> {
                g2.setColor(new Color(255, 204, 94));
                g2.fillOval(15, 12, 72, 76);

                g2.setColor(new Color(239, 232, 255));
                g2.fillOval(39, 0, 65, 65);

                g2.setColor(new Color(255, 190, 94));
                g2.fillOval(18, 25, 5, 5);
                g2.fillOval(75, 72, 5, 5);
            }

            case "star" -> {
                g2.setColor(new Color(255, 195, 76));
                Polygon star = new Polygon();

                for (int i = 0; i < 10; i++) {
                    double angle = Math.PI * i / 5 - Math.PI / 2;
                    int r = i % 2 == 0 ? 46 : 21;

                    star.addPoint(
                            50 + (int) (Math.cos(angle) * r),
                            50 + (int) (Math.sin(angle) * r)
                    );
                }

                g2.fillPolygon(star);
            }

            case "panda" -> {
                g2.setColor(Color.BLACK);
                g2.fillOval(10, 8, 32, 32);
                g2.fillOval(58, 8, 32, 32);

                g2.setColor(Color.WHITE);
                g2.fillOval(13, 20, 74, 70);

                g2.setColor(Color.BLACK);
                g2.fillOval(23, 35, 22, 27);
                g2.fillOval(55, 35, 22, 27);

                g2.setColor(Color.WHITE);
                g2.fillOval(29, 42, 8, 8);
                g2.fillOval(62, 42, 8, 8);

                g2.setColor(Color.BLACK);
                g2.fillOval(43, 63, 14, 10);
            }

            case "book" -> {
                g2.setColor(new Color(90,150,220)); g2.fillRoundRect(12,18,76,66,9,9);
                g2.setColor(Color.WHITE); g2.drawLine(50,22,50,80); g2.drawLine(22,35,42,35); g2.drawLine(58,35,78,35); g2.drawLine(22,48,42,48); g2.drawLine(58,48,78,48);
            }
            case "coffee" -> {
                g2.setColor(new Color(160,105,70)); g2.fillRoundRect(18,35,58,48,10,10); g2.setColor(new Color(115,75,50)); g2.drawArc(65,43,27,30,270,180);
                g2.setColor(new Color(255,245,225)); g2.fillRoundRect(23,40,48,7,5,5); g2.setColor(new Color(120,190,220)); g2.drawArc(32,8,12,24,70,180); g2.drawArc(51,5,12,27,70,180);
            }
            case "crown" -> {
                g2.setColor(new Color(255,190,55)); g2.fillPolygon(new int[]{12,20,38,50,65,82,90,82,18},new int[]{28,60,40,62,35,60,28,84,84},9);
                g2.setColor(new Color(255,245,190)); g2.fillOval(16,19,10,10); g2.fillOval(45,51,10,10); g2.fillOval(77,19,10,10);
            }
            case "diamond" -> {
                g2.setColor(new Color(80,190,220)); g2.fillPolygon(new int[]{50,85,72,50,28,15},new int[]{8,38,82,94,82,38},6);
                g2.setColor(new Color(205,250,255)); g2.fillPolygon(new int[]{50,68,50,32},new int[]{12,38,78,38},4); g2.setColor(new Color(50,145,190)); g2.drawPolygon(new int[]{50,85,72,50,28,15},new int[]{8,38,82,94,82,38},6);
            }
            case "flower" -> {
                Color[] petals={new Color(255,120,160),new Color(255,190,70),new Color(160,135,235),new Color(255,145,100)};
                for(int i=0;i<8;i++){double a=Math.PI*2*i/8;g2.setColor(petals[i%4]);g2.fillOval(39+(int)(Math.cos(a)*22)-13,39+(int)(Math.sin(a)*22)-13,26,26);} g2.setColor(new Color(255,215,70));g2.fillOval(38,38,24,24);
            }
            case "gift" -> {
                g2.setColor(new Color(255,115,145));g2.fillRoundRect(15,34,70,52,8,8);g2.setColor(new Color(255,215,75));g2.fillRect(44,34,12,52);g2.fillRect(15,48,70,10);g2.drawArc(27,8,25,34,180,180);g2.drawArc(49,8,25,34,180,180);
            }
            case "heart" -> {
                g2.setColor(new Color(240,80,120));g2.fillOval(13,15,46,48);g2.fillOval(41,15,46,48);g2.fillPolygon(new int[]{13,87,50},new int[]{45,45,91},3);
                g2.setColor(new Color(255,190,205));g2.fillOval(25,28,12,10);
            }
            case "leaf" -> {
                g2.setColor(new Color(95,180,105));g2.fill(new Ellipse2D.Double(15,12,72,78));g2.setColor(new Color(235,255,215));g2.setStroke(new BasicStroke(5));g2.drawLine(25,78,76,24);g2.drawLine(48,55,40,30);g2.drawLine(55,49,79,52);
            }
            case "lightning" -> {
                g2.setColor(new Color(255,195,45));g2.fillPolygon(new int[]{56,22,46,37,80,53,62},new int[]{5,54,49,95,39,45,5},7);g2.setColor(new Color(255,240,150));g2.drawPolygon(new int[]{56,22,46,37,80,53,62},new int[]{5,54,49,95,39,45,5},7);
            }
            case "music" -> {
                g2.setColor(new Color(150,115,220));g2.setStroke(new BasicStroke(8));g2.drawLine(62,15,62,70);g2.drawLine(62,15,85,10);g2.drawLine(85,10,85,62);g2.setColor(new Color(255,130,165));g2.fillOval(39,65,27,22);g2.fillOval(62,57,27,22);
            }
            case "plane" -> {
                g2.setColor(new Color(90,160,220));g2.fillPolygon(new int[]{5,94,55,48,40},new int[]{55,42,58,94,58},5);g2.setColor(Color.WHITE);g2.fillPolygon(new int[]{37,52,50,43},new int[]{52,20,54,65},4);
            }
            case "rainbow" -> {
                Color[] cs={new Color(245,95,105),new Color(255,175,70),new Color(255,220,90),new Color(95,190,125),new Color(90,155,225)};
                for(int i=0;i<5;i++){g2.setColor(cs[i]);g2.setStroke(new BasicStroke(9));g2.drawArc(12+i*8,18+i*8,76-i*16,76-i*16,0,180);} g2.setColor(new Color(255,255,255));g2.fillRect(8,55,84,40);
            }
            case "sun" -> {
                g2.setColor(new Color(255,190,55));for(int i=0;i<12;i++){double a=Math.PI*2*i/12;g2.setStroke(new BasicStroke(6));g2.drawLine(50+(int)(Math.cos(a)*34),50+(int)(Math.sin(a)*34),50+(int)(Math.cos(a)*45),50+(int)(Math.sin(a)*45));}g2.fillOval(22,22,56,56);
            }
            case "umbrella" -> {
                g2.setColor(new Color(100,155,225));g2.fillArc(8,12,84,65,0,180);g2.setColor(new Color(255,255,255));g2.fillPolygon(new int[]{8,29,50,71,92},new int[]{45,45,75,45,45},5);g2.setColor(new Color(115,90,70));g2.setStroke(new BasicStroke(6));g2.drawLine(50,43,50,83);g2.drawArc(37,73,26,20,180,180);
            }
            case "camera" -> {
                g2.setColor(new Color(90,100,125));g2.fillRoundRect(10,28,80,55,12,12);g2.fillRoundRect(28,18,30,15,5,5);g2.setColor(new Color(125,210,230));g2.fillOval(32,37,38,38);g2.setColor(Color.WHITE);g2.drawOval(38,43,26,26);g2.setColor(new Color(255,195,75));g2.fillOval(75,34,8,8);
            }
            case "bell" -> {
                g2.setColor(new Color(255,195,65));g2.fillArc(20,17,60,70,0,180);g2.fillRect(20,52,60,23);g2.fillOval(42,72,16,16);g2.setColor(new Color(255,235,165));g2.fillOval(45,12,10,10);
            }
            case "cake" -> {
                g2.setColor(new Color(255,145,175));g2.fillRoundRect(12,48,76,39,8,8);g2.setColor(new Color(255,220,225));g2.fillRect(12,43,76,13);g2.setColor(new Color(255,195,70));g2.fillRect(47,18,7,26);g2.setColor(new Color(255,130,65));g2.fillOval(43,7,15,18);
            }
            case "cat" -> {
                g2.setColor(new Color(235,165,105));g2.fillOval(14,25,72,65);g2.fillPolygon(new int[]{18,22,43,43},new int[]{43,8,25,45},4);g2.fillPolygon(new int[]{57,57,78,82},new int[]{25,45,8,43},4);g2.setColor(Color.WHITE);g2.fillOval(30,48,12,14);g2.fillOval(58,48,12,14);g2.setColor(new Color(65,65,75));g2.fillOval(35,53,5,7);g2.fillOval(63,53,5,7);g2.fillPolygon(new int[]{45,55,50},new int[]{68,68,74},3);
            }
            case "cloud" -> {
                g2.setColor(new Color(130,190,230));g2.fillOval(12,42,42,38);g2.fillOval(34,22,42,55);g2.fillOval(58,42,32,38);g2.fillRoundRect(20,52,65,28,12,12);
            }
            case "key" -> {
                g2.setColor(new Color(245,185,55));g2.setStroke(new BasicStroke(13));g2.drawOval(8,22,45,45);g2.drawLine(48,58,88,88);g2.setStroke(new BasicStroke(8));g2.drawLine(70,72,80,62);g2.drawLine(79,81,89,71);
            }
            case "lock" -> {
                g2.setColor(new Color(150,125,205));g2.setStroke(new BasicStroke(10));g2.drawArc(27,8,46,55,180,180);g2.setColor(new Color(255,190,80));g2.fillRoundRect(16,42,68,48,10,10);g2.setColor(Color.WHITE);g2.fillOval(45,55,10,10);g2.fillRect(48,63,5,14);
            }
            case "ship" -> {
                g2.setColor(new Color(105,145,190));g2.fillPolygon(new int[]{8,92,76,25},new int[]{62,62,85,85},4);g2.setColor(new Color(255,190,75));g2.fillPolygon(new int[]{47,47,15},new int[]{12,57,57},3);g2.setColor(new Color(240,115,125));g2.fillPolygon(new int[]{55,55,86},new int[]{20,57,57},3);g2.setColor(new Color(80,165,210));g2.drawArc(8,73,84,25,0,180);
            }
            case "tree" -> {
                g2.setColor(new Color(135,90,60));g2.fillRoundRect(43,48,15,43,5,5);g2.setColor(new Color(85,175,105));g2.fillOval(13,10,48,48);g2.fillOval(40,20,47,48);g2.fillOval(27,34,48,43);
            }

            case "target" -> {
                g2.setColor(new Color(255, 143, 169));
                g2.fillOval(7, 7, 86, 86);

                g2.setColor(Color.WHITE);
                g2.fillOval(23, 23, 54, 54);

                g2.setColor(new Color(151, 128, 222));
                g2.fillOval(39, 39, 22, 22);

                g2.setColor(new Color(255, 194, 83));
                g2.setStroke(new BasicStroke(6));
                g2.drawLine(52, 48, 88, 12);
                g2.drawLine(74, 12, 88, 12);
                g2.drawLine(88, 12, 88, 27);
            }

            case "flower-star" -> {
                Color[] colors = {
                        new Color(255, 143, 169),
                        new Color(255, 194, 83),
                        new Color(151, 128, 222),
                        new Color(112, 190, 158)
                };

                for (int i = 0; i < 8; i++) {
                    double a = Math.PI * 2 * i / 8;
                    int px = 50 + (int) (Math.cos(a) * 24) - 13;
                    int py = 50 + (int) (Math.sin(a) * 24) - 13;

                    g2.setColor(colors[i % colors.length]);
                    g2.fillOval(px, py, 27, 27);
                }

                g2.setColor(new Color(255, 213, 104));
                g2.fillOval(37, 37, 26, 26);
            }

            case "trophy" -> {
                g2.setColor(new Color(255, 194, 68));
                g2.fillRoundRect(28, 12, 44, 49, 8, 8);

                g2.setColor(new Color(255, 194, 68));
                g2.setStroke(new BasicStroke(9));
                g2.drawArc(8, 18, 35, 39, 270, 180);
                g2.drawArc(57, 18, 35, 39, 90, 180);

                g2.fillRect(44, 59, 12, 18);
                g2.fillRoundRect(29, 76, 42, 10, 5, 5);

                g2.setColor(Color.WHITE);
                Polygon star = new Polygon(
                        new int[]{50, 55, 68, 57, 50, 43, 32, 45},
                        new int[]{20, 34, 38, 45, 57, 45, 38, 34},
                        8
                );
                g2.fillPolygon(star);
            }

            case "history" -> {
                g2.setColor(new Color(151, 128, 222));
                g2.setStroke(new BasicStroke(8));
                g2.drawArc(13, 13, 74, 74, 35, 300);

                g2.setColor(new Color(255, 143, 169));
                g2.fillPolygon(
                        new int[]{14, 14, 34},
                        new int[]{17, 40, 29},
                        3
                );

                g2.setColor(TEXT);
                g2.setStroke(new BasicStroke(5));
                g2.drawLine(50, 26, 50, 51);
                g2.drawLine(50, 51, 68, 61);

                g2.setColor(new Color(255, 194, 83));
                g2.fillOval(42, 42, 16, 16);
            }

            case "refresh" -> {
                g2.setColor(PURPLE_DARK);
                g2.setStroke(new BasicStroke(9));
                g2.drawArc(13, 13, 74, 74, 45, 285);

                g2.fillPolygon(
                        new int[]{72, 94, 88},
                        new int[]{8, 25, 42},
                        3
                );
            }

            case "copy" -> {
                g2.setColor(new Color(190, 174, 232));
                g2.setStroke(new BasicStroke(5));
                g2.drawRoundRect(30, 10, 55, 65, 9, 9);

                g2.setColor(Color.WHITE);
                g2.fillRoundRect(12, 27, 55, 65, 9, 9);

                g2.setColor(PURPLE_DARK);
                g2.drawRoundRect(12, 27, 55, 65, 9, 9);
                g2.drawLine(25, 45, 53, 45);
                g2.drawLine(25, 57, 53, 57);
                g2.drawLine(25, 69, 45, 69);
            }

            case "hourglass" -> {
                g2.setColor(new Color(151, 128, 222));
                g2.setStroke(new BasicStroke(8));
                g2.drawLine(22, 12, 78, 12);
                g2.drawLine(22, 88, 78, 88);
                g2.drawLine(27, 15, 73, 85);
                g2.drawLine(73, 15, 27, 85);

                g2.setColor(new Color(255, 194, 83));
                g2.fillPolygon(
                        new int[]{31, 69, 50},
                        new int[]{22, 22, 49},
                        3
                );
                g2.fillPolygon(
                        new int[]{31, 69, 50},
                        new int[]{78, 78, 54},
                        3
                );
            }

            case "medal-gold", "medal-silver",
                    "medal-bronze", "medal" -> {

                Color medalColor = switch (type) {
                    case "medal-gold" -> new Color(255, 193, 65);
                    case "medal-silver" -> new Color(180, 190, 205);
                    case "medal-bronze" -> new Color(205, 139, 95);
                    default -> new Color(183, 169, 218);
                };

                g2.setColor(new Color(151, 128, 222));
                g2.fillPolygon(
                        new int[]{25, 43, 48, 35},
                        new int[]{8, 8, 46, 55},
                        4
                );

                g2.setColor(new Color(255, 143, 169));
                g2.fillPolygon(
                        new int[]{57, 75, 65, 52},
                        new int[]{8, 8, 55, 46},
                        4
                );

                g2.setColor(medalColor);
                g2.fillOval(17, 32, 66, 66);

                g2.setColor(new Color(255, 255, 255, 180));
                g2.setStroke(new BasicStroke(3));
                g2.drawOval(23, 38, 54, 54);

                g2.setColor(Color.WHITE);
                Polygon star = new Polygon(
                        new int[]{50, 56, 70, 58, 50, 42, 30, 44},
                        new int[]{43, 55, 57, 65, 78, 65, 57, 55},
                        8
                );
                g2.fillPolygon(star);
            }

            case "back" -> {
                g2.setColor(PURPLE_DARK);
                g2.setStroke(new BasicStroke(7));
                g2.drawLine(80, 50, 20, 50);
                g2.drawLine(20, 50, 44, 26);
                g2.drawLine(20, 50, 44, 74);
            }

            default -> {
                int hash = Math.abs(type.hashCode());
                Color c = Color.getHSBColor((hash % 360) / 360f, 0.48f, 0.92f);
                g2.setColor(c); g2.fillRoundRect(12, 12, 76, 76, 24, 24);
                g2.setColor(Color.WHITE); g2.setStroke(new BasicStroke(5));
                if ((hash & 1) == 0) { g2.drawOval(28, 25, 44, 44); g2.drawLine(50, 15, 50, 85); }
                else { g2.drawPolygon(new int[]{50,82,68,32,18},new int[]{12,42,82,82,42},5); g2.drawOval(38,38,24,24); }
            }
        }

        g2.dispose();
    
    }
}
