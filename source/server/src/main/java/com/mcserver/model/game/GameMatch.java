package com.mcserver.model.game;

import java.time.LocalDateTime;

import com.mcserver.model.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "game_matches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long joinCode;
    
    @ManyToOne
    @JoinColumn(name = "game_type_id", referencedColumnName = "id")
    private GameType gameType;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    
    @ManyToOne
    @JoinColumn(name = "winner_id", referencedColumnName = "id")
    private User winner;

}
