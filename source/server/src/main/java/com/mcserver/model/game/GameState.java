package com.mcserver.model.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.mcserver.model.entity.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "game_states")
@Data
@NoArgsConstructor
public class GameState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    @JoinColumn(name = "match_id", nullable = false, unique = true)
    private GameMatch match;

    @ManyToOne
    @JoinColumn(name = "current_player_id")
    private User currentPlayer;

    @OneToMany(mappedBy = "gameState", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Card> cardEntities = new ArrayList<>();

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Card[][] cards;

    public void setCards(Card[][] cards) {
        Objects.requireNonNull(cards, "Cards cannot be null");
        if (match == null || match.getGameType() == null) {
            throw new IllegalStateException("A match and game type must be set before the board");
        }

        int rows = match.getGameType().getBoardSizeM();
        int columns = match.getGameType().getBoardSizeN();
        if (cards.length != rows) {
            throw new IllegalArgumentException("Board row count does not match the game type");
        }

        List<Card> boardCards = new ArrayList<>(rows * columns);
        for (int x = 0; x < rows; x++) {
            if (cards[x] == null || cards[x].length != columns) {
                throw new IllegalArgumentException("Board column count does not match the game type");
            }
            for (int y = 0; y < columns; y++) {
                Card card = Objects.requireNonNull(cards[x][y], "Every board position must contain a card");
                card.setX(x);
                card.setY(y);
                card.setGameState(this);
                boardCards.add(card);
            }
        }

        this.cards = cards;
        this.cardEntities.clear();
        this.cardEntities.addAll(boardCards);
    }

    @PostLoad
    private void restoreBoard() {
        int rows = match.getGameType().getBoardSizeM();
        int columns = match.getGameType().getBoardSizeN();
        cards = new Card[rows][columns];

        for (Card card : cardEntities) {
            int x = card.getX();
            int y = card.getY();
            if (x < 0 || x >= rows || y < 0 || y >= columns || cards[x][y] != null) {
                throw new IllegalStateException("Stored card coordinates are invalid or duplicated");
            }
            cards[x][y] = card;
        }

        for (Card[] row : cards) {
            for (Card card : row) {
                if (card == null) {
                    throw new IllegalStateException("Stored board is missing a card position");
                }
            }
        }
    }
}
