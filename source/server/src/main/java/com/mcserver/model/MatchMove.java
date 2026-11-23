package com.mcserver.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchMove {
    private Long id;
    private GameMatch match;
    private User player;

    private int card1X;
    private int card1Y;
    private long card1PairId;

    private int card2X;
    private int card2Y;
    private long card2PairId;

    private boolean isMatch;
    private LocalDateTime timestamp;
    private int streakCount;
}