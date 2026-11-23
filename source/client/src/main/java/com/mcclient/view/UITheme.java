package com.mcclient.view;

import java.awt.Color;

public final class UITheme {
    private UITheme() {}

    // =====================================================
    // COLORS
    // =====================================================
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

    // =====================================================
    // BOARD & SYMBOLS
    // =====================================================
    public static final int PVP_BOARD_SIZE = 36;
    public static final int PVP_PAIR_COUNT = 18;

    public static final String[] SYMBOL_NAMES = {
            "apple", "rocket", "headphones", "soccer", "gamepad", "moon", "star", "panda",
            "book", "coffee", "crown", "diamond", "flower", "gift", "heart", "leaf",
            "lightning", "music", "plane", "rainbow", "sun", "trophy", "umbrella", "camera",
            "bell", "cake", "cat", "cloud", "key", "lock", "ship", "tree"
    };
}