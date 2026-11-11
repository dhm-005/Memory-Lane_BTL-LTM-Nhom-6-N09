package com.mcserver.service.game;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mcserver.dto.response.MatchSummaryDTO;
import com.mcserver.model.game.GameMatch;
import com.mcserver.repository.game.GameMatchRepository;
import com.mcserver.repository.game.GameStateRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameMatchService {
    private final GameMatchRepository gameMatchRepository;
    private final GameStateRepository gameStateRepository;

    @Transactional
    public MatchSummaryDTO createMatch(long joinCode) {
        if (joinCode <= 0) {
            throw new IllegalArgumentException("joinCode must be positive");
        }

        GameMatch match = new GameMatch();
        match.setJoinCode(joinCode);
        match.setStartedAt(LocalDateTime.now());
        return toSummary(gameMatchRepository.save(match));
    }

    @Transactional(readOnly = true)
    public List<MatchSummaryDTO> listMatches() {
        return gameMatchRepository.findAll().stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countMatches() {
        return gameMatchRepository.count();
    }

    @Transactional
    public GameMatch finishMatch(long matchId) {
        GameMatch match = gameMatchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));

        if (match.getEndedAt() == null) {
            match.setEndedAt(LocalDateTime.now());
        }
        gameStateRepository.findByMatch_Id(matchId).ifPresent(gameStateRepository::delete);

        return gameMatchRepository.save(match);
    }

    @Transactional
    public MatchSummaryDTO finishMatchSummary(long matchId) {
        return toSummary(finishMatch(matchId));
    }

    private MatchSummaryDTO toSummary(GameMatch match) {
        return new MatchSummaryDTO(
                match.getId(),
                match.getJoinCode(),
                match.getGameType() == null ? null : match.getGameType().getId(),
                match.getStartedAt() == null ? null : match.getStartedAt().toString(),
                match.getEndedAt() == null ? null : match.getEndedAt().toString());
    }
}
