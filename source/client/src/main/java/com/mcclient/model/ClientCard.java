package com.mcclient.model;

public class ClientCard {

    private long id;
    private long pairId;
    private boolean matched;

    public ClientCard() {
    }

    public ClientCard(long id, long pairId, boolean matched) {
        this.id = id;
        this.pairId = pairId;
        this.matched = matched;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPairId() {
        return pairId;
    }

    public void setPairId(long pairId) {
        this.pairId = pairId;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }
}
