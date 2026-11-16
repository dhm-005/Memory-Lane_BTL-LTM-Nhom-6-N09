package com.mcserver.network;

import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Mỗi nhóm chức năng (tài khoản, phòng, game, lịch sử) viết 1 @Component implements TypeHandler.
 * MessageDispatcher tự gom tất cả handler nên các thành viên không phải sửa file của nhau.
 */
public interface TypeHandler {
    Set<String> supportedTypes();

    Message handle(String type, String requestId, JsonNode data, ClientSession session);
}