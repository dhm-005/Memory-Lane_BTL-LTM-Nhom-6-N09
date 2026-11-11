package com.mcclient.model;

import java.util.ArrayList;
import java.util.List;

public class ClientGameState {

    private List<ClientCard> cards = new ArrayList<>();
    private Long currentPlayerId;
    private String status;
    private int currentPlayerScore;
    private int opponentScore;

    public ClientGameState() {
    }

    public ClientGameState(List<ClientCard> cards, Long currentPlayerId, String status,
            int currentPlayerScore, int opponentScore) {
        setCards(cards);
        this.currentPlayerId = currentPlayerId;
        this.status = status;
        this.currentPlayerScore = currentPlayerScore;
        this.opponentScore = opponentScore;
    }

    public List<ClientCard> getCards() {
        return new ArrayList<>(cards);
    }

    public void setCards(List<ClientCard> cards) {
        this.cards = new ArrayList<>(cards);
    }

    public Long getCurrentPlayerId() {
        return currentPlayerId;
    }

    public void setCurrentPlayerId(Long currentPlayerId) {
        this.currentPlayerId = currentPlayerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCurrentPlayerScore() {
        return currentPlayerScore;
    }

    public void setCurrentPlayerScore(int currentPlayerScore) {
        this.currentPlayerScore = currentPlayerScore;
    }

    public int getOpponentScore() {
        return opponentScore;
    }

    public void setOpponentScore(int opponentScore) {
        this.opponentScore = opponentScore;
    }
}
