package com.cardgame.server.model;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.security.SecureRandom;

/** Board and turn state for one PvP match. Room remains the synchronization monitor. */
public final class Match {
    private static final SecureRandom RANDOM = new SecureRandom();
    public final int[] scores = {0, 0};
    public final int[] cards;
    public final boolean[] matched;
    public final List<Integer> opened = new ArrayList<>();

    public int turn;
    public int matchedPairs;
    public boolean hasFlipped;
    public boolean turnHasFlipped;
    public boolean startTurnCardWindow;
    public int lockedIndex = -1;
    public int lockedPlayer = -1;
    public boolean bonusAttempt;
    public boolean turnCardUsed;
    public boolean skipNext0;
    public boolean skipNext1;
    public boolean curse0;
    public boolean curse1;
    public boolean waitingShield;
    public String pendingAttack = "";
    public int pendingAttacker = -1;
    public int pendingAttackIndex = -1;
    public boolean promptPending;
    public String promptType = "";

    public boolean beats(String first, String second) {
        return (first.equals("ROCK") && second.equals("SCISSORS"))
                || (first.equals("SCISSORS") && second.equals("PAPER"))
                || (first.equals("PAPER") && second.equals("ROCK"));
    }

    public int[] randomPair(int boardSize) {
        Map<Integer, List<Integer>> positionsBySymbol = new HashMap<>();
        for (int index = 0; index < boardSize; index++) {
            if (!matched[index] && index != lockedIndex) {
                positionsBySymbol.computeIfAbsent(cards[index], ignored -> new ArrayList<>()).add(index);
            }
        }

        List<int[]> availablePairs = new ArrayList<>();
        for (List<Integer> positions : positionsBySymbol.values()) {
            if (positions.size() >= 2) {
                availablePairs.add(new int[]{positions.get(0), positions.get(1)});
            }
        }
        if (availablePairs.isEmpty()) return null;
        return availablePairs.get(RANDOM.nextInt(availablePairs.size()));
    }

    public Match(int boardSize) {
        cards = new int[boardSize];
        matched = new boolean[boardSize];
    }
}
