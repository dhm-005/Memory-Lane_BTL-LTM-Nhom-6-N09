package com.mcclient;

import java.util.Map;

import com.mcclient.network.NetworkClient;

public class main {
    public static void main(String[] args) throws Exception {
        NetworkClient.getInstance().connect("127.0.0.1", 5000);

        var response = NetworkClient.getInstance()
                .request("LIST_ROOMS", Map.of(
                        "userId", 1L,
                        "username", "alice"))
                .get();

        System.out.println(response);
    }
}
