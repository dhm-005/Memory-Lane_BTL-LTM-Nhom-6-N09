
package com.mcclient;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;

import com.mcclient.view.MainFrame;

public class MCClient {

    private MCClient() { }

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
        });
    }
}
