package com.mcserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchPlayer {
    private Long id;
    private GameMatch match;
    private User player;
    private int score;
    private int coinsEarned;
    private int flipCount;
    private int correctPairsCount;
    private int maxStreak;
}