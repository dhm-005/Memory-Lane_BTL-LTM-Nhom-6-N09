package com.mcserver.repository.game;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcserver.model.game.GameMatch;

public interface GameMatchRepository extends JpaRepository<GameMatch, Long> {
}
