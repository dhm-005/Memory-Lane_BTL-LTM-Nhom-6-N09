package com.mcserver.network;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Một kiểu cho cả phản hồi lẫn sự kiện đẩy; field null sẽ không được ghi ra JSON. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Message(String type, String requestId, Boolean success, Object data) {

    public static Message ok(String type, String requestId, Object data) {
        return new Message(type, requestId, true, data);
    }

    public static Message error(String type, String requestId, String code, String message) {
        return new Message(type, requestId, false, Map.of("code", code, "message", message));
    }

    /** Sự kiện server chủ động đẩy: không có requestId, không có success. */
    public static Message push(String type, Object data) {
        return new Message(type, null, null, data);
    }
}