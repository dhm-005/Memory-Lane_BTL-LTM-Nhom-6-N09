CREATE DATABASE IF NOT EXISTS memory_lane
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE memory_lane;

-- 1. Bảng người chơi
CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    display_name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(150) NULL,
    phone VARCHAR(20) NULL,
    dob DATE NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_login_at DATETIME(6) NULL,
    total_score INT NOT NULL DEFAULT 0,
    coins INT NOT NULL DEFAULT 0,
    player_rank INT NOT NULL DEFAULT 0,
    role ENUM('PLAYER', 'ADMIN') NOT NULL DEFAULT 'PLAYER',

    PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT chk_users_coins CHECK (coins >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Bảng chế độ chơi / kích thước bàn cờ
CREATE TABLE IF NOT EXISTS game_types (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    time_limit INT NOT NULL,
    board_size_m INT NOT NULL,
    board_size_n INT NOT NULL,
    game_mode ENUM('ONE_ONE', 'SOLO_MATCH', 'POWER_UP_MATCH') NOT NULL,
    level INT NOT NULL DEFAULT 1,

    PRIMARY KEY (id),
    CONSTRAINT chk_game_types_board CHECK (
        time_limit > 0 AND board_size_m > 0 AND board_size_n > 0 AND (board_size_m * board_size_n) % 2 = 0
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Bảng danh mục thẻ chức năng (mua trước mỗi ván bằng coins)
CREATE TABLE IF NOT EXISTS action_cards (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(100) NOT NULL,
    category VARCHAR(100) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT NULL,
    price INT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    CONSTRAINT uk_action_cards_name UNIQUE (name),
    CONSTRAINT chk_action_cards_price CHECK (price >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Bảng lịch sử trận đấu
CREATE TABLE IF NOT EXISTS game_matches (
    id BIGINT NOT NULL AUTO_INCREMENT,
    join_code VARCHAR(20) NOT NULL,
    game_type_id BIGINT NOT NULL,
    started_at DATETIME(6) NOT NULL,
    ended_at DATETIME(6) NULL,
    winner_id BIGINT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_game_matches_game_type
        FOREIGN KEY (game_type_id) REFERENCES game_types (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_game_matches_winner
        FOREIGN KEY (winner_id) REFERENCES users (id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Bảng kết quả từng người chơi trong trận
CREATE TABLE IF NOT EXISTS match_players (
    id BIGINT NOT NULL AUTO_INCREMENT,
    match_id BIGINT NOT NULL,
    player_id BIGINT NOT NULL,
    score INT NOT NULL DEFAULT 0,
    coins_earned INT NOT NULL DEFAULT 0,
    flip_count INT NOT NULL DEFAULT 0,
    correct_pairs_count INT NOT NULL DEFAULT 0,
    max_streak INT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    CONSTRAINT uk_match_players_match_player UNIQUE (match_id, player_id),
    CONSTRAINT chk_match_players_stats CHECK (
        flip_count >= 0 AND correct_pairs_count >= 0 AND max_streak >= 0 AND coins_earned >= 0
    ),
    CONSTRAINT fk_match_players_match
        FOREIGN KEY (match_id) REFERENCES game_matches (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_match_players_player
        FOREIGN KEY (player_id) REFERENCES users (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Bảng chi tiết từng nước đi trong trận
CREATE TABLE IF NOT EXISTS match_moves (
    id BIGINT NOT NULL AUTO_INCREMENT,
    match_id BIGINT NOT NULL,
    player_id BIGINT NOT NULL,
    card1_x INT NOT NULL,
    card1_y INT NOT NULL,
    card1_pair_id BIGINT NOT NULL,
    card2_x INT NOT NULL,
    card2_y INT NOT NULL,
    card2_pair_id BIGINT NOT NULL,
    is_match BOOLEAN NOT NULL,
    moved_at DATETIME(6) NOT NULL,
    streak_count INT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    CONSTRAINT fk_match_moves_match
        FOREIGN KEY (match_id) REFERENCES game_matches (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_match_moves_player
        FOREIGN KEY (player_id) REFERENCES users (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;