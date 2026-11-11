package com.mcserver.model.game;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "cards", uniqueConstraints = {
        @UniqueConstraint(name = "uq_card_state_position", columnNames = { "game_state_id", "x_coordinate",
                "y_coordinate" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long pairId;
    private boolean isMatched;
    private String imgPath;

    @Column(name = "x_coordinate", nullable = false)
    private int x;

    @Column(name = "y_coordinate", nullable = false)
    private int y;

    @ManyToOne(optional = false)
    @JoinColumn(name = "game_state_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private GameState gameState;
}
