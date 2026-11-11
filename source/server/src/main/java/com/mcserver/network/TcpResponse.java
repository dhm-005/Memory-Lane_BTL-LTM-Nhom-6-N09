package com.mcserver.network;

public record TcpResponse(boolean ok, Object data, String errorCode, String error) {

    public static TcpResponse success(Object data) {
        return new TcpResponse(true, data, null, null);
    }

    public static TcpResponse failure(String errorCode, String error) {
        return new TcpResponse(false, null, errorCode, error);
    }
}
