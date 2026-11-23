USE memory_lane;

-- =============================================
-- 1. USERS - TÀI KHOẢN NGƯỜI CHƠI
-- =============================================
INSERT INTO users
(display_name, username, password_hash, email, phone, dob,
 total_score, coins, player_rank, role)
VALUES
('Nguyen Van An', 'nguyenvanan', '1234', 'an@gmail.com', '0901234567', '2003-05-12', 2500, 500, 1, 'PLAYER'),
('Tran Thi Binh', 'tranthibinh', '1234', 'binh@gmail.com', '0901234568', '2004-08-20', 1800, 350, 2, 'PLAYER'),
('Le Hoang Nam', 'lehoangnam', '1234', 'nam@gmail.com', '0901234569', '2003-11-15', 3200, 800, 3, 'PLAYER'),
('Pham Minh Quan', 'phamminhquan', '1234', 'quan@gmail.com', '0901234570', '2002-02-10', 1200, 200, 4, 'PLAYER'),
('Admin Memory Lane', 'admin', '1234', 'admin@memorylane.com', NULL, '2000-01-01', 0, 9999, 0, 'ADMIN'),
('Do Thu Ha', 'dothuha', '1234', 'ha@gmail.com', '0901234571', '2004-06-25', 900, 150, 5, 'PLAYER');

-- =============================================
-- 2. GAME_TYPES - CHẾ ĐỘ CHƠI
-- =============================================
INSERT INTO game_types
(name, description, time_limit, board_size_m, board_size_n, game_mode, level)
VALUES
('Solo 4x4 - Easy', 'Chế độ chơi đơn với bàn cờ 4x4, độ khó dễ.', 120, 4, 4, 'SOLO_MATCH', 1),
('Solo 6x6 - Medium', 'Chế độ chơi đơn với bàn cờ 6x6, độ khó trung bình.', 180, 6, 6, 'SOLO_MATCH', 2),
('Solo 8x8 - Hard', 'Chế độ chơi đơn với bàn cờ 8x8, độ khó cao.', 240, 8, 8, 'SOLO_MATCH', 3),
('One vs One 4x4', 'Hai người chơi thi đấu trên bàn cờ 4x4.', 180, 4, 4, 'ONE_ONE', 1),
('One vs One 6x6', 'Hai người chơi thi đấu trên bàn cờ 6x6.', 240, 6, 6, 'ONE_ONE', 2),
('Power Up 4x4', 'Hai người chơi sử dụng thẻ chức năng.', 180, 4, 4, 'POWER_UP_MATCH', 1),
('Power Up 6x6', 'Hai người chơi sử dụng thẻ chức năng trên bàn cờ 6x6.', 240, 6, 6, 'POWER_UP_MATCH', 2);

-- =============================================
-- 3. ACTION_CARDS - THẺ CHỨC NĂNG
-- =============================================

INSERT INTO action_cards (code, category, name, description, price)
VALUES

-- ATTACK - THẺ TẤN CÔNG
('A01', 'ATTACK', 'Đóng băng',
 'Đối thủ mất lượt kế tiếp; bạn được tiếp tục.', 5),
('A02', 'ATTACK', 'Khóa bài',
 'Khóa một lá úp trong lượt kế tiếp của đối thủ.', 3),
('A03', 'ATTACK', 'Lời nguyền',
 'Nếu đối thủ lật sai ở lượt bị nguyền, họ mất thêm một lượt sau đó.', 5),
('A04', 'ATTACK', 'Ảo ảnh',
 'Đổi vị trí ngẫu nhiên của hai lá chưa ghép.', 3),


-- SUPPORT - THẺ HỖ TRỢ
('S01', 'SUPPORT', 'Con mắt tinh anh',
 'Xem riêng một lá úp trong 2 giây.', 3),
('S02', 'SUPPORT', 'Khiên bảo vệ',
 'Chặn một thẻ Attack nhắm vào bạn.', 4),
('S03', 'SUPPORT', 'Tăng tốc',
 'Sau khi lật sai, được thêm một lần thử.', 4),
('S04', 'SUPPORT', 'Ngôi sao may mắn',
 'Có 25% cơ hội tự ghép một cặp.', 5),

-- LUCK - THẺ MAY MẮN
('L01', 'LUCK', 'Rương kho báu',
 'Thắng: nhận thêm 10 xu.', 2),
('L02', 'LUCK', 'Xúc xắc may mắn',
 'Thắng: 50% cơ hội nhận thêm 20 xu.', 1),
('L03', 'LUCK', 'Móng ngựa vàng',
 'Thắng: nhân đôi thưởng thắng cơ bản.', 2),
('L04', 'LUCK', 'Vương miện vàng',
 'Thua: 50% cơ hội tránh bị trừ 10 xu.', 1);


-- =============================================
-- 4. GAME_MATCHES - LỊCH SỬ TRẬN ĐẤU
-- =============================================
INSERT INTO game_matches
(join_code, game_type_id, started_at, ended_at, winner_id)
VALUES
('SOLO001', 1, '2026-11-20 10:00:00', '2026-11-20 10:01:30', 1),
('SOLO002', 2, '2026-11-20 11:00:00', '2026-11-20 11:02:45', 3),
('ROOM001', 4, '2026-11-21 14:00:00', '2026-11-21 14:03:00', 1),
('ROOM002', 6, '2026-11-21 15:00:00', '2026-11-21 15:04:00', 2),
('ROOM003', 5, '2026-11-22 09:00:00', '2026-11-22 09:05:00', 3),
('SOLO003', 3, '2026-11-22 16:00:00', '2026-11-22 16:04:00', 6);

-- =============================================
-- 5. MATCH_PLAYERS - KẾT QUẢ NGƯỜI CHƠI
-- =============================================
INSERT INTO match_players
(match_id, player_id, score, coins_earned,
 flip_count, correct_pairs_count, max_streak)
VALUES
-- Trận 1: Solo - An
(1, 1, 500, 50, 16, 8, 4),

-- Trận 2: Solo - Nam
(2, 3, 800, 80, 25, 18, 6),

-- Trận 3: An thắng Binh
(3, 1, 650, 65, 20, 8, 5),
(3, 2, 400, 40, 24, 6, 3),

-- Trận 4: Binh thắng Quan
(4, 2, 700, 70, 18, 8, 5),
(4, 4, 450, 45, 22, 6, 2),

-- Trận 5: Nam thắng An
(5, 3, 900, 90, 30, 18, 7),
(5, 1, 600, 60, 35, 15, 4),

-- Trận 6: Solo - Ha
(6, 6, 550, 55, 40, 30, 5);

-- =============================================
-- 6. MATCH_MOVES - CHI TIẾT NƯỚC ĐI
-- =============================================
INSERT INTO match_moves
(match_id, player_id,
 card1_x, card1_y, card1_pair_id,
 card2_x, card2_y, card2_pair_id,
 is_match, moved_at, streak_count)
VALUES
-- Trận 1
(1, 1, 0, 0, 1, 1, 1, 1, 1, '2026-11-20 10:00:10', 1),
(1, 1, 0, 1, 2, 2, 1, 3, 0, '2026-11-20 10:00:20', 0),
(1, 1, 1, 0, 2, 3, 0, 2, 1, '2026-11-20 10:00:30', 2),

-- Trận 3
(3, 1, 0, 0, 1, 1, 1, 1, 1, '2026-11-21 14:00:10', 1),
(3, 2, 0, 1, 2, 2, 2, 3, 0, '2026-11-21 14:00:20', 0),
(3, 1, 1, 0, 2, 3, 0, 2, 1, '2026-11-21 14:00:30', 2),
(3, 2, 1, 1, 4, 2, 0, 4, 1, '2026-11-21 14:00:40', 1),

-- Trận 4
(4, 2, 0, 0, 1, 1, 1, 1, 1, '2026-11-21 15:00:10', 1),
(4, 4, 0, 1, 2, 2, 2, 3, 0, '2026-11-21 15:00:20', 0),
(4, 2, 1, 0, 2, 3, 0, 2, 1, '2026-11-21 15:00:30', 2),

-- Trận 5
(5, 3, 0, 0, 1, 1, 1, 1, 1, '2026-11-22 09:00:10', 1),
(5, 1, 0, 1, 2, 2, 2, 3, 0, '2026-11-22 09:00:20', 0),
(5, 3, 1, 0, 2, 3, 0, 2, 1, '2026-11-22 09:00:30', 2);
