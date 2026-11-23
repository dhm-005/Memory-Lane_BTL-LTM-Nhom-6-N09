package com.mcserver.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mcserver.model.ActionCard;
import com.mcserver.model.ramOnly.BoardCard;
import com.mcserver.model.GameMatch;
import com.mcserver.model.ramOnly.GameState;
import com.mcserver.model.GameType;
import com.mcserver.model.MatchMove;
import com.mcserver.model.MatchPlayer;
import com.mcserver.model.ramOnly.Room;
import com.mcserver.model.User;
import com.mcserver.network.ClientSession;
import com.mcserver.network.SessionRegistry;
import com.mcserver.repository.ActionCardRepository;
import com.mcserver.repository.UserRepository;
import com.mcserver.util.JsonTemplates;

public final class GameMatchService {

    // Lưu trạng thái ván đấu đang chạy trong RAM (Key: roomId)
    private static final Map<String, GameState> ACTIVE_GAMES = new ConcurrentHashMap<>();

    // Dữ liệu phụ trợ trong RAM theo từng roomId (điểm, streak, lá bài đang lật, túi thẻ ActionCard)
    private static final Map<String, MatchRuntime> RUNTIMES = new ConcurrentHashMap<>();

    private static final class MatchRuntime {

        final User player1;
        final User player2;
        final Map<Long, MatchPlayer> stats = new HashMap<>();
        final Map<Long, List<ActionCard>> inventories = new HashMap<>();
        final Map<Long, Integer> currentStreak = new HashMap<>();
        BoardCard firstFlipped = null;
        boolean shieldActive = false;

        MatchRuntime(User p1, User p2, GameMatch match) {
            this.player1 = p1;
            this.player2 = p2;
            stats.put(p1.getId(), new MatchPlayer(0L, match, p1, 0, 0, 0, 0, 0));
            stats.put(p2.getId(), new MatchPlayer(0L, match, p2, 0, 0, 0, 0, 0));
            inventories.put(p1.getId(), new ArrayList<>());
            inventories.put(p2.getId(), new ArrayList<>());
            currentStreak.put(p1.getId(), 0);
            currentStreak.put(p2.getId(), 0);
        }

        User opponentOf(long userId) {
            return userId == player1.getId() ? player2 : player1;
        }
    }

    private GameMatchService() {
    }

    /**
     * 1. Bắt đầu trận đấu mới từ Room đủ 2 người
     */
    public static GameState startMatch(Room room, GameType gameType, User p1, User p2, SessionRegistry registry) {
        int rows = gameType.getBoardSizeM();
        int cols = gameType.getBoardSizeN();
        BoardCard[][] board = generateShuffledBoard(rows, cols);

        GameMatch match = new GameMatch();
        match.setJoinCode(room.getId());
        match.setGameType(gameType);
        match.setStartedAt(LocalDateTime.now());

        GameState state = new GameState(match, p1, board);
        MatchRuntime runtime = new MatchRuntime(p1, p2, match);

        ACTIVE_GAMES.put(room.getId(), state);
        RUNTIMES.put(room.getId(), runtime);

        var startMsg = JsonTemplates.matchStartedPush(match, state);
        registry.sendToUser(p1.getId(), startMsg);
        registry.sendToUser(p2.getId(), startMsg);
        return state;
    }

    /**
     * 2. Mua thẻ chức năng (trừ coins trong MySQL và thêm vào túi đồ trận đấu)
     */
    public static boolean buyActionCard(ClientSession session, long cardId) throws SQLException {
        if (session == null || !session.isAuthenticated()) {
            return false;
        }

        Room room = RoomService.getRoomByUserId(session.getUserId());
        if (room == null) {
            return false;
        }

        GameState state = ACTIVE_GAMES.get(room.getId());
        MatchRuntime runtime = RUNTIMES.get(room.getId());
        if (state == null || runtime == null) {
            return false;
        }

        ActionCard card = ActionCardRepository.findAll().get(cardId);
        if (card == null) {
            return false;
        }

        boolean deducted = UserRepository.deductCoins(session.getUserId(), card.getPrice());
        if (!deducted) {
            return false;
        }

        synchronized (state) {
            runtime.inventories.get(session.getUserId()).add(card);
        }
        return true;
    }

    /**
     * 3. Xử lý lật thẻ bài tại tọa độ (x, y)
     */
    public static void flipCard(ClientSession session, int x, int y, SessionRegistry registry) {
        if (session == null || !session.isAuthenticated()) {
            return;
        }

        long userId = session.getUserId();
        Room room = RoomService.getRoomByUserId(userId);
        if (room == null) {
            return;
        }

        GameState state = ACTIVE_GAMES.get(room.getId());
        MatchRuntime runtime = RUNTIMES.get(room.getId());
        if (state == null || runtime == null) {
            return;
        }

        synchronized (state) {
            // Kiểm tra đúng lượt
            if (state.getCurrentPlayer() == null || state.getCurrentPlayer().getId() != userId) {
                return;
            }

            BoardCard[][] cards = state.getCards();
            if (x < 0 || x >= cards.length || y < 0 || y >= cards[0].length) {
                return;
            }

            BoardCard picked = cards[x][y];
            if (picked == null || picked.isMatched()) {
                return;
            }

            // Gửi sự kiện lật 1 lá cho cả 2 người chơi
            var flipMsg = JsonTemplates.cardFlippedPush(picked, userId);
            registry.sendToUser(runtime.player1.getId(), flipMsg);
            registry.sendToUser(runtime.player2.getId(), flipMsg);

            // Lá thứ nhất trong lượt
            if (runtime.firstFlipped == null) {
                runtime.firstFlipped = picked;
                return;
            }

            // Không cho bấm trùng lại chính lá thứ nhất
            BoardCard first = runtime.firstFlipped;
            if (first.getX() == x && first.getY() == y) {
                return;
            }
            runtime.firstFlipped = null;

            MatchPlayer playerStat = runtime.stats.get(userId);
            playerStat.setFlipCount(playerStat.getFlipCount() + 1);

            boolean isMatch = (first.getPairId() == picked.getPairId());
            int streak;

            if (isMatch) {
                first.setMatched(true);
                picked.setMatched(true);

                streak = runtime.currentStreak.get(userId) + 1;
                runtime.currentStreak.put(userId, streak);

                int earnedPoints = 100 + (streak - 1) * 20;
                playerStat.setScore(playerStat.getScore() + earnedPoints);
                playerStat.setCorrectPairsCount(playerStat.getCorrectPairsCount() + 1);
                if (streak > playerStat.getMaxStreak()) {
                    playerStat.setMaxStreak(streak);
                }
                // Lật đúng -> giữ nguyên lượt
            } else {
                streak = 0;
                runtime.currentStreak.put(userId, 0);
                // Lật sai -> đổi lượt sang đối thủ
                state.setCurrentPlayer(runtime.opponentOf(userId));
            }

            MatchMove move = new MatchMove(
                    0L,
                    state.getMatch(),
                    playerStat.getPlayer(),
                    first.getX(), first.getY(), first.getPairId(),
                    picked.getX(), picked.getY(), picked.getPairId(),
                    isMatch,
                    LocalDateTime.now(),
                    streak
            );

            // Đẩy kết quả nước đi + trạng thái bàn cờ mới cho cả 2 người
            var moveMsg = JsonTemplates.moveResultPush(move, state);
            registry.sendToUser(runtime.player1.getId(), moveMsg);
            registry.sendToUser(runtime.player2.getId(), moveMsg);

            // Kiểm tra đã lật hết bàn cờ chưa
            if (isBoardCleared(cards)) {
                finishMatchInternal(room.getId(), state, runtime, null, registry);
            }
        }
    }

    /**
     * 4. Kích hoạt thẻ chức năng trong trận
     */
    public static void useActionCard(ClientSession session, long cardId, int targetX, int targetY, SessionRegistry registry) {
        if (session == null || !session.isAuthenticated()) {
            return;
        }

        long userId = session.getUserId();
        Room room = RoomService.getRoomByUserId(userId);
        if (room == null) {
            return;
        }

        GameState state = ACTIVE_GAMES.get(room.getId());
        MatchRuntime runtime = RUNTIMES.get(room.getId());
        if (state == null || runtime == null) {
            return;
        }

        synchronized (state) {
            if (state.getCurrentPlayer().getId() != userId) {
                return;
            }

            List<ActionCard> bag = runtime.inventories.get(userId);
            ActionCard owned = bag.stream().filter(c -> c.getId() == cardId).findFirst().orElse(null);
            if (owned == null) {
                return;
            }

            bag.remove(owned);

            String category = owned.getCategory() != null ? owned.getCategory().toUpperCase() : "";
            switch (category) {
                case "SHIELD" ->
                    runtime.shieldActive = true;
                case "SHUFFLE" -> {
                    if (!runtime.shieldActive) {
                        shuffleUnmatchedCards(state.getCards());
                    } else {
                        runtime.shieldActive = false;
                    }
                }
                default -> {
                    // Các hiệu ứng khác (SOI_BAI, CONG_DIEM...) cập nhật trực tiếp lên state
                }
            }

            var boardMsg = JsonTemplates.boardStatePush(state);
            registry.sendToUser(runtime.player1.getId(), boardMsg);
            registry.sendToUser(runtime.player2.getId(), boardMsg);
        }
    }

    /**
     * 5. Người chơi thoát giữa trận -> xử thua ngay lập tức
     */
    public static void leaveMatch(ClientSession session, SessionRegistry registry) {
        if (session == null || !session.isAuthenticated()) {
            return;
        }

        long quitterId = session.getUserId();
        Room room = RoomService.getRoomByUserId(quitterId);
        if (room == null) {
            return;
        }

        GameState state = ACTIVE_GAMES.get(room.getId());
        MatchRuntime runtime = RUNTIMES.get(room.getId());
        if (state == null || runtime == null) {
            return;
        }

        synchronized (state) {
            User winner = runtime.opponentOf(quitterId);
            finishMatchInternal(room.getId(), state, runtime, winner, registry);
        }
    }

    // ==================== HELPER NỘI BỘ ====================
    private static void finishMatchInternal(
            String roomId,
            GameState state,
            MatchRuntime runtime,
            User forcedWinner,
            SessionRegistry registry) {
        ACTIVE_GAMES.remove(roomId);
        RUNTIMES.remove(roomId);

        GameMatch match = state.getMatch();
        match.setEndedAt(LocalDateTime.now());

        MatchPlayer mp1 = runtime.stats.get(runtime.player1.getId());
        MatchPlayer mp2 = runtime.stats.get(runtime.player2.getId());

        User winner = forcedWinner;
        if (winner == null) {
            if (mp1.getScore() > mp2.getScore()) {
                winner = runtime.player1;
            } else if (mp2.getScore() > mp1.getScore()) {
                winner = runtime.player2;
            }
        }
        match.setWinner(winner);

        if (winner != null) {
            MatchPlayer winStat = runtime.stats.get(winner.getId());
            winStat.setCoinsEarned(30);
        } else {
            mp1.setCoinsEarned(10);
            mp2.setCoinsEarned(10);
        }

        // Lưu kết quả vào MySQL (cộng coins & total_score cho 2 người chơi)
        try {
            UserRepository.addCoins(mp1.getPlayer().getId(), mp1.getCoinsEarned());
            UserRepository.addCoins(mp2.getPlayer().getId(), mp2.getCoinsEarned());
            // Gọi thêm GameMatchRepository.saveMatchResult(match, List.of(mp1, mp2)) tại đây
        } catch (SQLException e) {
            System.err.println("[DB] Failed to persist finished match: " + e.getMessage());
        }

        var endMsg = JsonTemplates.matchEndedPush(match, List.of(mp1, mp2));
        registry.sendToUser(runtime.player1.getId(), endMsg);
        registry.sendToUser(runtime.player2.getId(), endMsg);
    }

    private static BoardCard[][] generateShuffledBoard(int rows, int cols) {
        int total = rows * cols;
        List<Long> pairIds = new ArrayList<>(total);
        for (long pairId = 1; pairId <= total / 2; pairId++) {
            pairIds.add(pairId);
            pairIds.add(pairId);
        }
        Collections.shuffle(pairIds);

        BoardCard[][] board = new BoardCard[rows][cols];
        int idx = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                long pid = pairIds.get(idx++);
                board[r][c] = new BoardCard(pid, "CARD_" + pid, false, "/cards/card_" + pid + ".png", r, c);
            }
        }
        return board;
    }

    private static void shuffleUnmatchedCards(BoardCard[][] cards) {
        List<BoardCard> unmatched = new ArrayList<>();
        for (BoardCard[] row : cards) {
            for (BoardCard card : row) {
                if (!card.isMatched()) {
                    unmatched.add(card);
                }
            }
        }
        List<Long> pairIds = new ArrayList<>();
        for (BoardCard c : unmatched) {
            pairIds.add(c.getPairId());
        }
        Collections.shuffle(pairIds);

        for (int i = 0; i < unmatched.size(); i++) {
            long newPid = pairIds.get(i);
            BoardCard c = unmatched.get(i);
            c.setPairId(newPid);
            c.setCardCode("CARD_" + newPid);
            c.setImgPath("/cards/card_" + newPid + ".png");
        }
    }

    private static boolean isBoardCleared(BoardCard[][] cards) {
        for (BoardCard[] row : cards) {
            for (BoardCard card : row) {
                if (!card.isMatched()) {
                    return false;
                }
            }
        }
        return true;
    }

    public static int countMatches() {
        return ACTIVE_GAMES.size();
    }

    public static List<GameMatch> listMatches() {
        return ACTIVE_GAMES.values().stream().map(GameState::getMatch).toList();
    }

    public static GameMatch createMatch(String joinCode) {
        GameMatch m = new GameMatch();
        m.setJoinCode(joinCode);
        return m;
    }

    public static Map<String, Object> finishMatchSummary(long matchId) {
        return Map.of("matchId", matchId, "status", "FINISHED");
    }
}
