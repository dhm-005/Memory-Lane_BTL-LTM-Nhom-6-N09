package com.mcclient;

import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatLightLaf;
import com.mcclient.controller.LobbyController;
import com.mcclient.model.User;
import com.mcclient.network.NetworkClient;
import com.mcclient.view.LobbyFrame;

/** Launcher tạm để Người 2 tự test. Xoá khi đã có luồng LoginFrame -> LobbyFrame. */
public class LobbyDev {
    public static void main(String[] args) throws Exception {
        long userId = Long.parseLong(args[0]);
        String username = args[1];
        String host = args.length > 2 ? args[2] : "127.0.0.1";
        FlatLightLaf.setup();
        NetworkClient.getInstance().connect(host, 5000);
        SwingUtilities.invokeLater(() ->
                new LobbyController(new User(userId, username, "PLAYER"), new LobbyFrame(username)).start());
    }
}