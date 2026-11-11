package com.mcserver.config;

import java.util.ResourceBundle;

public class Config {
    
    private static final ResourceBundle CONFIG =
            ResourceBundle.getBundle("application");

    public static String get(String key) {
        return CONFIG.getString(key);
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }
}
