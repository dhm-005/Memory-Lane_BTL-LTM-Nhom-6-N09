-- (có thể BỎ QUA) CHARACTER SET utf8mb4: Hỗ trợ lưu trữ đầy đủ tiếng Việt có dấu và emoji (4 byte) khi mở rộng thêm tính năng chat trong game
-- COLLATE utf8mb4_unicode_ci: Quy tắc so sánh/sắp xếp không phân biệt chữ hoa/thường (case-insensitive).
CREATE DATABASE IF NOT EXISTS memory_lane
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE memory_lane;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS game_results (
    id INT AUTO_INCREMENT PRIMARY KEY,
    room_id VARCHAR(50) NOT NULL,
    player1_id INT NOT NULL,
    player2_id INT NOT NULL,
    player1_score INT NOT NULL DEFAULT 0,
    player2_score INT NOT NULL DEFAULT 0,
    winner_id INT NULL,
    started_at DATETIME NOT NULL,
    ended_at DATETIME NOT NULL,
    
    -- Đặt tên tường minh cho FK, dễ báo lỗi & dễ xóa/sửa; ON DELETE CASCADE: tự xóa lịch sử nếu user bị xóa
    CONSTRAINT fk_player1 FOREIGN KEY (player1_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_player2 FOREIGN KEY (player2_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- ON DELETE SET NULL: Nếu user thắng bị xóa, cột winner_id tự chuyển về NULL (trường hợp hòa hoặc xóa tài khoản)
    CONSTRAINT fk_winner FOREIGN KEY (winner_id) REFERENCES users(id) ON DELETE SET NULL
);