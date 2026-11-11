package com.mcserver.model.game;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data 
@NoArgsConstructor 
@AllArgsConstructor 

public class Card {

    private long id;
    private long pairId;
    private boolean isMatched;
    private String imgPath;
    
}
