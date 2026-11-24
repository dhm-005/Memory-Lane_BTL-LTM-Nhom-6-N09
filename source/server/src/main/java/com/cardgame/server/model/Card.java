package com.cardgame.server.model;

public final class Card {
    public final String code;
    public final String category;
    public final String name;
    public final String description;
    public final int price;
    public Card(String code, String category, String name, String description, int price) {
        this.code = code;
        this.category = category;
        this.name = name;
        this.description = description;
        this.price = price;
    }
}
