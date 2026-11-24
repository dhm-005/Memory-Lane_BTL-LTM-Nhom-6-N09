
USE card_memory_game;

-- =====================================================
-- 1. INSERT USERS (Pass đều là 1234)
-- =====================================================
SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO users
(id, username, password_hash, coins, ranking_points, created_at)
VALUES
(1, 'player01', 'pbkdf2$210000$hsozwNdDP6cbdgB802Os4w==$P34jgoqKLvnISWMmDQB9Ju4mmSX+1uk6/Sk5cEidgnU='
, 100, 120, '2026-01-01 08:00:00'),
(2, 'player02', 'pbkdf2$210000$hsozwNdDP6cbdgB802Os4w==$P34jgoqKLvnISWMmDQB9Ju4mmSX+1uk6/Sk5cEidgnU='
, 80, 100, '2026-01-02 08:00:00'),
(3, 'player03', 'pbkdf2$210000$hsozwNdDP6cbdgB802Os4w==$P34jgoqKLvnISWMmDQB9Ju4mmSX+1uk6/Sk5cEidgnU='
, 150, 200, '2026-01-03 08:00:00'),
(4, 'player04', 'pbkdf2$210000$hsozwNdDP6cbdgB802Os4w==$P34jgoqKLvnISWMmDQB9Ju4mmSX+1uk6/Sk5cEidgnU='
, 60, 50, '2026-01-04 08:00:00'),
(5, 'player05', 'pbkdf2$210000$hsozwNdDP6cbdgB802Os4w==$P34jgoqKLvnISWMmDQB9Ju4mmSX+1uk6/Sk5cEidgnU='
, 200, 300, '2026-01-05 08:00:00'),
(6, 'player06', 'pbkdf2$210000$hsozwNdDP6cbdgB802Os4w==$P34jgoqKLvnISWMmDQB9Ju4mmSX+1uk6/Sk5cEidgnU='
, 90, 80, '2026-01-06 08:00:00'),
(7, 'player07', 'pbkdf2$210000$hsozwNdDP6cbdgB802Os4w==$P34jgoqKLvnISWMmDQB9Ju4mmSX+1uk6/Sk5cEidgnU='
, 120, 150, '2026-01-07 08:00:00'),
(8, 'player08', 'pbkdf2$210000$hsozwNdDP6cbdgB802Os4w==$P34jgoqKLvnISWMmDQB9Ju4mmSX+1uk6/Sk5cEidgnU='
, 70, 60, '2026-01-08 08:00:00'),
(9, 'player09', 'pbkdf2$210000$hsozwNdDP6cbdgB802Os4w==$P34jgoqKLvnISWMmDQB9Ju4mmSX+1uk6/Sk5cEidgnU='
, 180, 250, '2026-01-09 08:00:00'),
(10, 'player10', 'pbkdf2$210000$hsozwNdDP6cbdgB802Os4w==$P34jgoqKLvnISWMmDQB9Ju4mmSX+1uk6/Sk5cEidgnU='
, 50, 30, '2026-01-10 08:00:00'),
(11, 'player11', 'pbkdf2$210000$hsozwNdDP6cbdgB802Os4w==$P34jgoqKLvnISWMmDQB9Ju4mmSX+1uk6/Sk5cEidgnU='
, 130, 170, '2026-01-11 08:00:00'),
(12, 'player12', 'pbkdf2$210000$hsozwNdDP6cbdgB802Os4w==$P34jgoqKLvnISWMmDQB9Ju4mmSX+1uk6/Sk5cEidgnU='
, 110, 110, '2026-01-12 08:00:00');

-- =====================================================
-- 2. INSERT CARDS
-- =====================================================
INSERT INTO cards
(card_code, category, card_name, description, price)
VALUES
('A01','ATTACK','Đóng băng','Đối thủ mất lượt kế tiếp; bạn được tiếp tục.',5),
('A02','ATTACK','Khóa bài','Khóa một lá úp trong lượt kế tiếp của đối thủ.',3),
('A03','ATTACK','Lời nguyền','Nếu đối thủ lật sai ở lượt bị nguyền, họ mất thêm một lượt sau đó.',5),
('A04','ATTACK','Ảo ảnh','Đổi vị trí ngẫu nhiên của hai lá chưa ghép.',3),
('S01','SUPPORT','Con mắt tinh anh','Xem riêng một lá úp trong 2 giây.',3),
('S02','SUPPORT','Khiên bảo vệ','Chặn một thẻ Attack nhắm vào bạn.',4),
('S03','SUPPORT','Tăng tốc','Sau khi lật sai, được thêm một lần thử.',4),
('S04','SUPPORT','Ngôi sao may mắn','Có 75% cơ hội tự ghép một cặp.',5),
('L01','LUCK','Rương kho báu','Thắng: nhận thêm 10 xu.',2),
('L02','LUCK','Xúc xắc may mắn','Thắng: 50% cơ hội nhận thêm 20 xu.',1),
('L03','LUCK','Móng ngựa vàng','Thắng: nhân đôi thưởng thắng cơ bản.',2),
('L04','LUCK','Vương miện vàng','Thua: 50% cơ hội tránh bị trừ 10 xu.',1);

-- =====================================================
-- 3. INSERT GAME_RESULTS
-- =====================================================
INSERT INTO game_results
(id, room_id, player1_id, player2_id,
 player1_score, player2_score, winner_id,
 status, cancel_reason,
 player1_coin_change, player2_coin_change,
 player1_rank_change, player2_rank_change,
 started_at, ended_at)
VALUES
(1,'ROOM001',1,2,8,5,1,'COMPLETED',NULL,20,-10,15,-10,'2026-02-01 08:00:00','2026-02-01 08:10:00'),
(2,'ROOM002',3,4,4,7,4,'COMPLETED',NULL,-10,20,-10,15,'2026-02-01 09:00:00','2026-02-01 09:12:00'),
(3,'ROOM003',5,6,9,3,5,'COMPLETED',NULL,20,-10,15,-10,'2026-02-02 08:00:00','2026-02-02 08:15:00'),
(4,'ROOM004',7,8,6,6,NULL,'COMPLETED',NULL,0,0,0,0,'2026-02-02 09:00:00','2026-02-02 09:10:00'),
(5,'ROOM005',9,10,2,8,10,'COMPLETED',NULL,-10,20,-10,15,'2026-02-03 08:00:00','2026-02-03 08:11:00'),
(6,'ROOM006',11,12,10,4,11,'COMPLETED',NULL,20,-10,15,-10,'2026-02-03 09:00:00','2026-02-03 09:14:00'),
(7,'ROOM007',1,3,5,7,3,'COMPLETED',NULL,-10,20,-10,15,'2026-02-04 08:00:00','2026-02-04 08:09:00'),
(8,'ROOM008',2,4,8,6,2,'COMPLETED',NULL,20,-10,15,-10,'2026-02-04 09:00:00','2026-02-04 09:13:00'),
(9,'ROOM009',5,7,3,9,7,'COMPLETED',NULL,-10,20,-10,15,'2026-02-05 08:00:00','2026-02-05 08:12:00'),
(10,'ROOM010',6,8,7,5,6,'COMPLETED',NULL,20,-10,15,-10,'2026-02-05 09:00:00','2026-02-05 09:11:00'),
(11,'ROOM011',9,11,6,8,11,'COMPLETED',NULL,-10,20,-10,15,'2026-02-06 08:00:00','2026-02-06 08:12:00'),
(12,'ROOM012',10,12,5,3,10,'CANCELLED','Đối thủ mất kết nối',0,0,0,0,'2026-02-06 09:00:00','2026-02-06 09:05:00');

-- =====================================================
-- 4. INSERT SOLO_RECORDS
-- =====================================================
INSERT INTO solo_records
(user_id, best_level, best_time_ms, completed_runs)
VALUES
(1,10,120000,15),
(2,8,145000,10),
(3,15,180000,25),
(4,5,200000,5),
(5,20,240000,30),
(6,9,160000,12),
(7,12,135000,18),
(8,6,190000,7),
(9,18,210000,22),
(10,4,250000,3),
(11,14,155000,20),
(12,11,170000,14);

SET FOREIGN_KEY_CHECKS = 1;
