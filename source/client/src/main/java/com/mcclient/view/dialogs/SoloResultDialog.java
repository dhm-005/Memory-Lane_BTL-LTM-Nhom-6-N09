package com.mcclient.view.dialogs;

import javax.swing.*;
import java.awt.Component;

public final class SoloResultDialog {
    private SoloResultDialog() { }
    public static void show(Component parent, String elapsed, int reward) {
        JOptionPane.showMessageDialog(parent,
                "Bạn đã hoàn thành cả 5 level!\nThời gian: " + elapsed + "\nThưởng: " + reward + " xu",
                "Chiến thắng", JOptionPane.INFORMATION_MESSAGE);
    }
}
