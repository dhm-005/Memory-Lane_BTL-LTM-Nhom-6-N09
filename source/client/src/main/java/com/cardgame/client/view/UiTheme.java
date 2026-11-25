package com.cardgame.client.view;

import java.awt.Color;
/** Shared UI palette and icon catalogue. */
public final class UiTheme {
    private UiTheme() {
    }
    public static final Color BG = new Color(255, 249, 241);
    public static final Color WHITE = Color.WHITE;
    public static final Color TEXT = new Color(55, 50, 75);
    public static final Color MUTED = new Color(145, 139, 158);
    public static final Color PURPLE = new Color(155, 132, 225);
    public static final Color PURPLE_DARK = new Color(125, 101, 195);
    public static final Color LAVENDER = new Color(239, 232, 255);
    public static final Color MINT = new Color(220, 245, 232);
    public static final Color YELLOW = new Color(255, 237, 190);
    public static final Color PINK = new Color(255, 228, 235);
    public static final Color BORDER = new Color(237, 229, 220);
    /** Màu báo lỗi dùng cho thông báo ngay trên form. */
    public static final Color DANGER = new Color(200, 45, 55);
    public static final String[] SYMBOL_NAMES = {
        "apple", "rocket", "headphones", "soccer", "gamepad", "moon", "star", "panda",
        "book", "coffee", "crown", "diamond", "flower", "gift", "heart", "leaf",
        "lightning", "music", "plane", "rainbow", "sun", "trophy", "umbrella", "camera",
        "bell", "cake", "cat", "cloud", "key", "lock", "ship", "tree"
    };
}
