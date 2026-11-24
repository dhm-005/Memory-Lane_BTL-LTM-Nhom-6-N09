package com.cardgame.client;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.cardgame.client.view.MainFrame;
import com.formdev.flatlaf.FlatLightLaf;
/** Application entry point for the desktop client. */
public final class MainClient {
    private MainClient() {
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                FlatLightLaf.setup();
                UIManager.put("Component.arc", 18);
                UIManager.put("Button.arc", 18);
                UIManager.put("TextComponent.arc", 14);
                UIManager.put("Table.rowHeight", 44);
                new MainFrame().show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        );
    }
}
