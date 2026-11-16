package com.mcclient.network;

import java.util.concurrent.CompletionException;

public class ServerException extends RuntimeException {
    private final String code;

    public ServerException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static Throwable unwrap(Throwable t) {
        return (t instanceof CompletionException && t.getCause() != null) ? t.getCause() : t;
    }
}