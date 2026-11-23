package com.mcserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameType{
    public enum GameMode {ONE_ONE, SOLO_MATCH, POWER_UP_MATCH}
    
    private Long id;
    private String name;
    private String description;
    private int timeLimit; // in seconds
    private int level;

    // e.g., M x N
    private int boardSizeM; 
    private int boardSizeN;

    private GameMode gameMode;
}
