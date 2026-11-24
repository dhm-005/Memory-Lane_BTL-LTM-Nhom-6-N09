# Giao thức giao tiếp Client – Server

## 1. Tổng quan

Client và Server được xây dựng thành hai Maven project độc lập. Hai thành phần giao tiếp với nhau thông qua TCP Socket.

Dữ liệu trao đổi giữa Client và Server sử dụng định dạng JSON. Mỗi message được gửi trên một dòng riêng biệt; phía nhận đọc từng dòng và phân tích thành đối tượng JSON để xử lý.

Trường `type` xác định loại message. Các trường còn lại chứa dữ liệu tương ứng với message đó.

Tên message và tên các trường dữ liệu cần được thống nhất giữa Client và Server để đảm bảo quá trình giao tiếp diễn ra chính xác.

## 2. Quy ước message

Mỗi message có cấu trúc JSON với trường `type`:

```json
{
  "type": "MESSAGE_TYPE",
  "field": "value"
}
```

- `type`: loại message cần xử lý.
- Các trường khác: dữ liệu được gửi kèm theo message.
- Message không có dữ liệu bổ sung chỉ cần trường `type`.

Các giá trị được gửi trong message phải đúng định dạng mà Server hoặc Client quy định.

## 3. Message từ Client đến Server

### 3.1. Tài khoản

| Message | Các trường dữ liệu | Mô tả |
|---|---|---|
| `REGISTER` | `username`, `password` | Đăng ký tài khoản |
| `LOGIN` | `username`, `password` | Đăng nhập |
| `CHANGE_PASSWORD` | `oldPassword`, `newPassword` | Đổi mật khẩu |
| `LOGOUT` | Không có | Đăng xuất |

### 3.2. Phòng chơi

| Message | Các trường dữ liệu | Mô tả |
|---|---|---|
| `LIST_ROOMS` | Không có | Lấy danh sách phòng |
| `CREATE_ROOM` | Không có | Tạo phòng chơi |
| `JOIN_ROOM` | `roomId` | Tham gia phòng |
| `READY` | Không có | Xác nhận sẵn sàng |
| `UNREADY` | Không có | Hủy trạng thái sẵn sàng |
| `LEAVE_ROOM` | Không có | Rời phòng |

### 3.3. Bắt đầu trận đấu

| Message | Các trường dữ liệu | Mô tả |
|---|---|---|
| `RPS_CHOICE` | `choice` | Gửi lựa chọn kéo, búa hoặc bao |
| `CHOOSE_ORDER` | `order` | Chọn thứ tự chơi |
| `BUY_SELECTION` | `codes[]` | Gửi danh sách mã thẻ đã chọn mua |
| `BUY_CONFIRM` | Không có | Xác nhận lựa chọn mua thẻ |

Các giá trị hợp lệ:

- `choice`: `ROCK`, `PAPER`, `SCISSORS`
- `order`: `FIRST`, `SECOND`

### 3.4. Trong trận đấu

| Message | Các trường dữ liệu | Mô tả |
|---|---|---|
| `FLIP` | `index` | Lật lá bài tại vị trí được chọn |
| `USE_CARD` | `code`, `index` | Sử dụng thẻ kỹ năng |
| `SKIP_CARD` | Không có | Bỏ qua việc sử dụng thẻ |
| `SHIELD_RESPONSE` | `use` | Phản hồi yêu cầu sử dụng khiên |
| `LEAVE_MATCH` | Không có | Rời khỏi trận đấu |

Trường `index` xác định vị trí lá bài trên bàn chơi. Với `USE_CARD`, giá trị `index` bằng `-1` được sử dụng khi kỹ năng không yêu cầu chọn mục tiêu là một lá bài.

### 3.5. Chế độ chơi đơn

| Message | Các trường dữ liệu | Mô tả |
|---|---|---|
| `SOLO_START` | Không có | Bắt đầu chế độ chơi đơn |
| `SOLO_BEST` | Không có | Yêu cầu thông tin thành tích chơi đơn tốt nhất |
| `SOLO_LEVEL_COMPLETE` | `level`, `timeMs` | Gửi thông tin hoàn thành màn chơi |

### 3.6. Dữ liệu người chơi

| Message | Các trường dữ liệu | Mô tả |
|---|---|---|
| `HISTORY` | Không có | Yêu cầu lịch sử trận đấu |
| `LEADERBOARD` | Không có | Yêu cầu bảng xếp hạng PvP |
| `SOLO_LEADERBOARD` | Không có | Yêu cầu bảng xếp hạng chơi đơn |
| `CARD_COLLECTION` | Không có | Yêu cầu bộ sưu tập thẻ |

## 4. Message từ Server đến Client

### 4.1. Tài khoản

| Message | Dữ liệu trả về | Mô tả |
|---|---|---|
| `ERROR` | `message` | Thông báo lỗi |
| `LOGIN_OK` | `userId`, `username`, `coins`, `points` | Đăng nhập thành công |
| `PASSWORD_CHANGED` | Không có | Đổi mật khẩu thành công |
| `LOGOUT_OK` | Không có | Đăng xuất thành công |

### 4.2. Phòng chơi

| Message | Dữ liệu trả về | Mô tả |
|---|---|---|
| `ROOMS` | `rooms[]` | Danh sách phòng |
| `LOBBY_UPDATE` | Dữ liệu cập nhật phòng | Thông báo thay đổi tại sảnh |
| `ROOM_JOINED` | `roomId`, `host` | Tham gia phòng thành công |
| `ROOM_STATE` | `roomId`, `player1`, `player2`, `ready1`, `ready2` | Trạng thái phòng |
| `ROOM_CANCELLED` | `reason`, `coins` | Thông báo phòng bị hủy |
| `ROOM_LEFT` | `reason`, `coins` | Thông báo rời phòng |

### 4.3. Bắt đầu trận đấu

| Message | Dữ liệu trả về | Mô tả |
|---|---|---|
| `RPS_START` | Dữ liệu bắt đầu lượt chọn | Bắt đầu chọn kéo, búa, bao |
| `RPS_WAIT` | Không có | Chờ người chơi còn lại |
| `RPS_TIE` | Không có | Hai người chơi hòa |
| `RPS_RESULT` | `winner`, `youWin` | Kết quả kéo, búa, bao |
| `ORDER_SELECTED` | `firstPlayer`, `secondPlayer` | Thông báo thứ tự chơi |
| `CARD_OFFERS` | `cards[]` | Danh sách thẻ có thể mua |
| `BUY_CONFIRMED` | `coins`, `owned[]` | Xác nhận mua thẻ |
| `COINS_UPDATED` | `coins` | Cập nhật số xu |
| `GAME_START` | `player1`, `player2`, `currentPlayer`, `boardSize`, `firstPlayer`, `secondPlayer`, `coins1`, `coins2` | Bắt đầu trận đấu |
| `PRIVATE_CARDS` | `cards[]` | Danh sách thẻ riêng của người chơi |

### 4.4. Diễn biến trận đấu

| Message | Dữ liệu trả về | Mô tả |
|---|---|---|
| `CARD_FLIP` | `index`, `value`, `by` | Thông báo lật lá bài |
| `ROUND_RESULT` | `matched`, `score1`, `score2`, `indexA`, `indexB`, `player`, `currentPlayer`, `player1`, `player2` | Kết quả lượt lật bài |
| `BOARD_RESET` | `currentPlayer` | Cập nhật trạng thái bàn chơi |
| `TURN_CHANGED` | `currentPlayer`, `score1`, `score2`, `lockedIndex`, `turnSeconds`, `player1`, `player2` | Thông báo lượt chơi hiện tại |
| `CARD_PROMPT` | `phase`, `eligible[]` | Yêu cầu lựa chọn sử dụng thẻ |
| `CARD_USED` | `player`, `code`, `name` | Thông báo thẻ được sử dụng |
| `CARD_EFFECT` | `code`, `target`, `message`, có thể có `index` | Thông báo hiệu ứng thẻ |
| `SHIELD_PROMPT` | `attacker`, `code` | Yêu cầu phản hồi bằng khiên |
| `SHIELD_BLOCKED` | `player` | Thông báo hiệu ứng bị chặn |
| `PRIVATE_REVEAL` | `index`, `value` | Hiển thị thông tin lá bài riêng |
| `CARD_FAILED` | Dữ liệu lỗi sử dụng thẻ | Thông báo sử dụng thẻ thất bại |
| `BONUS_ATTEMPT` | Dữ liệu lượt thử thêm | Thông báo lượt thử thêm |
| `LUCKY_PAIR` | `player`, `indexA`, `indexB` | Thông báo ghép cặp may mắn |
| `BOARD_SHUFFLED` | Dữ liệu bàn chơi | Thông báo bàn chơi được xáo trộn |
| `TURN_SKIPPED` | `player` | Thông báo lượt chơi bị bỏ qua |
| `TURN_TIMEOUT` | `player` | Thông báo hết thời gian lượt chơi |

### 4.5. Kết thúc trận đấu

| Message | Dữ liệu trả về | Mô tả |
|---|---|---|
| `GAME_OVER` | `score1`, `score2`, `winner`, `player1`, `player2`, `coin1`, `coin2`, `purchaseCoin1`, `purchaseCoin2`, `finishCoin1`, `finishCoin2`, `netCoin1`, `netCoin2`, `rank1`, `rank2`, `coins1`, `coins2` | Thông báo kết quả trận đấu |
| `MATCH_CANCELLED` | `reason`, `quitter`, `player1`, `player2`, `coin1`, `coin2`, `purchaseCoin1`, `purchaseCoin2`, `finishCoin1`, `finishCoin2`, `coins1`, `coins2` | Thông báo trận đấu bị hủy |

### 4.6. Chế độ chơi đơn

| Message | Dữ liệu trả về | Mô tả |
|---|---|---|
| `SOLO_LEVEL_OK` | `level`, `timeMs`, `coins` | Xác nhận hoàn thành màn chơi |
| `SOLO_BEST` | `level`, `timeMs` | Thông tin thành tích tốt nhất |
| `SOLO_REWARD_OK` | `coins`, `amount`, `level` | Xác nhận phần thưởng |

### 4.7. Lịch sử và bảng xếp hạng

| Message | Dữ liệu trả về | Mô tả |
|---|---|---|
| `HISTORY` | `items[]` | Danh sách lịch sử trận đấu |
| `LEADERBOARD` | `items[]` | Bảng xếp hạng PvP |
| `SOLO_LEADERBOARD` | `items[]` | Bảng xếp hạng chơi đơn |
| `CARD_COLLECTION` | `cards[]` | Bộ sưu tập thẻ của người chơi |

## 5. Quy tắc xử lý trận đấu

### 5.1. Bàn chơi

- Bàn chơi PvP gồm 36 ô, tương ứng 18 cặp bài.
- Danh mục biểu tượng bài gồm 32 giá trị.
- Trường `value` trong message `CARD_FLIP` là chỉ số tham chiếu đến danh mục biểu tượng bài phía Client.

### 5.2. Mua thẻ

- Người chơi có thể chọn tối đa 2 thẻ từ 3 nhóm thẻ được cung cấp.
- Người chơi cần có tối thiểu 10 xu để tạo hoặc tham gia trận đấu 1v1.
- Khi mua thẻ, người chơi phải giữ lại ít nhất 10 xu để đảm bảo khoản phạt khi rời trận.

### 5.3. Thời gian

- Thời gian chờ lựa chọn mua thẻ: 32 giây.
- Client đóng giao diện lựa chọn mua thẻ sau 30 giây.
- Thời gian mỗi lượt PvP: 60 giây.
- Khi hai lá bài không khớp, Server chờ 1,5 giây trước khi ẩn các lá bài.
- Thời gian hiển thị riêng lá bài của kỹ năng S01 ở Client: 2 giây.

Server là thành phần quản lý thời gian chính thức của trận đấu. Bộ đếm thời gian trên Client chỉ phục vụ hiển thị.

### 5.4. Xử lý hết thời gian lượt chơi

Khi hết thời gian lượt chơi, Server xử lý các trạng thái lật bài hoặc lựa chọn thẻ chưa hoàn thành, gửi message `TURN_TIMEOUT`, sau đó chuyển lượt chơi theo trạng thái trận đấu.

Message `TURN_CHANGED` cung cấp trường `turnSeconds` để Client hiển thị thời gian còn lại của lượt.

### 5.5. Kỹ năng và hiệu ứng

- Server kiểm tra tính hợp lệ của lượt chơi và việc sử dụng thẻ.
- Server xác định hiệu ứng thẻ và kết quả trận đấu.
- Kỹ năng S04 – Lucky Star có tỷ lệ 75% tự động ghép đúng một cặp bài.

## 6. Quy tắc chế độ chơi đơn

- Client gửi yêu cầu bắt đầu chế độ chơi đơn bằng `SOLO_START`.
- Server kiểm tra trạng thái đăng nhập và tiến trình hoàn thành màn chơi.
- Server xác nhận hoàn thành màn chơi bằng `SOLO_LEVEL_OK`.
- Client chỉ chuyển màn hoặc hiển thị kết quả cuối sau khi nhận được xác nhận từ Server.
- Thời gian hoàn thành do Server đo và kiểm tra. Giá trị `timeMs` do Client gửi không được sử dụng làm thời gian thành tích chính thức.

## 7. Dữ liệu và tính toàn vẹn

- Server chịu trách nhiệm xác thực người chơi, kiểm tra lượt chơi, tính điểm và xác định kết quả trận đấu PvP.
- Việc cập nhật xu, điểm xếp hạng và lịch sử trận đấu được thực hiện trong cùng một giao dịch cơ sở dữ liệu.
- Bảng xếp hạng PvP chỉ bao gồm người chơi đã hoàn thành ít nhất một trận đấu.
- Tỷ lệ thắng được hiển thị với hai chữ số thập phân.

## 8. Kết nối

Client và Server giao tiếp qua TCP Socket bằng message JSON theo từng dòng.

Khi kết nối bị gián đoạn, Client thông báo tình trạng kết nối cho người chơi. Việc tự động kết nối lại không được thực hiện trong trạng thái hiện tại của hệ thống.

## 9. Lưu ý khi thay đổi giao thức

Khi thêm hoặc thay đổi message:

1. Cập nhật xử lý message ở cả Client và Server.
2. Giữ thống nhất tên `type` và các trường dữ liệu.
3. Kiểm tra các luồng liên quan đến đăng nhập, phòng chơi, trận đấu và kết thúc trận.
4. Cập nhật tài liệu này nếu cấu trúc message hoặc quy tắc xử lý thay đổi.
