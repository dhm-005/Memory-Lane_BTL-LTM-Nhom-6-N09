package com.mcserver.dto.response;

public record MatchSummaryDTO(
        long id,
        long joinCode,
        Long gameTypeId,
        String startedAt,
        String endedAt) {
}
