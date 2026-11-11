package com.mcserver.repository.game;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcserver.model.game.GameState;

public interface GameStateRepository extends JpaRepository<GameState, Long> {
    Optional<GameState> findByMatch_Id(long matchId);
}
