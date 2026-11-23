package com.mcserver.model.ramOnly;


import com.mcserver.model.GameMatch;
import com.mcserver.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameState {
    private GameMatch match;
    private User currentPlayer;
    private BoardCard[][] cards;
}