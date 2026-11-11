package com.mcserver.model.game;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "game_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;
    private String description;
    private int timeLimit; // in seconds

    // e.g., M x N
    private int boardSizeM; 
    private int boardSizeN;

    public enum GameMode {FRIENDLY_MATCH, ONE_ONE, SPEED_MATCH, POWER_UP_MATCH}
    private GameMode gameMode;

    public enum Difficulty {EASY, MEDIUM, HARD}
    private Difficulty difficulty; // e.g., EASY, MEDIUM, HARD
}
