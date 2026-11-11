package com.mcserver.model.game;

import java.time.LocalDateTime;

import com.mcserver.model.entity.User;

import jakarta.persistence.Column;
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
@Table(name = "match_moves")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchMove {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private GameMatch match;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private User player;

    @Column(name = "card1_x", nullable = false)
    private int card1X;

    @Column(name = "card1_y", nullable = false)
    private int card1Y;

    @Column(name = "card1_pair_id", nullable = false)
    private long card1PairId;

    @Column(name = "card2_x", nullable = false)
    private int card2X;

    @Column(name = "card2_y", nullable = false)
    private int card2Y;

    @Column(name = "card2_pair_id", nullable = false)
    private long card2PairId;

    @Column(name = "is_match", nullable = false)
    private boolean isMatch;

    @Column(name = "moved_at", nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private int streakCount; // Number of consecutive matches made by the player
}
