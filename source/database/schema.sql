CREATE DATABASE IF NOT EXISTS card_memory_game CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE card_memory_game;

CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(24) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  coins INT NOT NULL DEFAULT 50,
  ranking_points INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

ALTER TABLE users MODIFY password_hash VARCHAR(255) NOT NULL;

CREATE TABLE IF NOT EXISTS cards (
  card_code VARCHAR(3) PRIMARY KEY,
  category ENUM('ATTACK','SUPPORT','LUCK') NOT NULL,
  card_name VARCHAR(80) NOT NULL,
  description VARCHAR(500) NOT NULL,
  price INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS game_results (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  room_id VARCHAR(12) NOT NULL,
  player1_id INT NOT NULL,
  player2_id INT NOT NULL,
  player1_score INT NOT NULL DEFAULT 0,
  player2_score INT NOT NULL DEFAULT 0,
  winner_id INT NULL,
  status ENUM('COMPLETED','CANCELLED') NOT NULL DEFAULT 'COMPLETED',
  cancel_reason VARCHAR(120) NULL,
  player1_coin_change INT NOT NULL DEFAULT 0,
  player2_coin_change INT NOT NULL DEFAULT 0,
  player1_rank_change INT NOT NULL DEFAULT 0,
  player2_rank_change INT NOT NULL DEFAULT 0,
  started_at DATETIME NOT NULL,
  ended_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_result_p1 FOREIGN KEY (player1_id) REFERENCES users(id),
  CONSTRAINT fk_result_p2 FOREIGN KEY (player2_id) REFERENCES users(id),
  CONSTRAINT fk_result_winner FOREIGN KEY (winner_id) REFERENCES users(id),
  INDEX idx_result_p1_time(player1_id, ended_at),
  INDEX idx_result_p2_time(player2_id, ended_at),
  INDEX idx_result_status(status)
) ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS solo_records (
  user_id INT PRIMARY KEY,
  best_level TINYINT NOT NULL DEFAULT 0,
  best_time_ms BIGINT NULL,
  completed_runs INT NOT NULL DEFAULT 0,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_solo_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;
