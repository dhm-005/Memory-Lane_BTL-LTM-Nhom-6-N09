package com.mcserver.model.game;

import com.mcserver.model.entity.User;

import org.hibernate.annotations.Check;

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
@Table(name = "match_players")
@Check(constraints = "flip_count >= 0 AND correct_pairs_count >= 0 AND max_streak >= 0")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchPlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private GameMatch match;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private User player;

    private int score;

    private int flipCount;

    private int correctPairsCount;

    private int maxStreak;
}
