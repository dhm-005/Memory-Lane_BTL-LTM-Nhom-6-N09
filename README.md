# BÀI TẬP LỚN: LẬP TRÌNH MẠNG  

## MEMORY LANE - Trò chơi lật thẻ đối kháng online

---

## 🧑‍💻 THÔNG TIN NHÓM

| STT | Họ và Tên | MSSV | Email | Đóng góp |
|-----|-----------|------|-------|----------|
| 1 | Đào Hà My | B23DCCN571 | daohamyvn2005@gmail.com | ... |
| 2 | Cao Thị Thanh Nhàn | B23DCCN618 | b@example.com | ... |
| 3 | Đặng Thanh Thúy | B23DCCN807 | c@example.com | ... |
| 4 | Nguyễn Đức Minh | B23DCCN555 | d@example.com | ... |


**Tên nhóm:** Nhóm 06 – Lập trình mạng  
**Chủ đề đã đăng ký:** Xây dựng trò chơi lật thẻ ghi nhớ đối kháng trực tuyến 1v1 sử dụng Java Swing, TCP Socket và lập trình đa luồng theo mô hình Client–Server.

---

## 🧠 MÔ TẢ HỆ THỐNG

Memory Lane là game lật thẻ ghi nhớ đối kháng được phát triển bằng Java. Dự án được xây dựng theo mô hình Client–Server, sử dụng giao diện desktop Java Swing và cơ sở dữ liệu MySQL để lưu trữ thông tin người chơi, danh mục thẻ kỹ năng, lịch sử đấu và thành tích chơi.

> Hệ thống bao gồm **Server** xử lý đa luồng các kết nối và nghiệp vụ trận đấu, và **Client** gửi yêu cầu đến Server thông qua giao thức TCP/IP bằng `Socket`.  

> **Client:** Ứng dụng desktop xây dựng bằng Java Swing kết hợp giao diện hiện đại FlatLaf, gửi các yêu cầu thao tác của người chơi và cập nhật bàn chơi theo thời gian thực.  
> **Server:** Tiếp nhận kết nối đồng thời trên cổng TCP (mặc định `5000`), quản lý phòng chơi, trạng thái ván đấu, xác thực tài khoản và phản hồi dữ liệu định dạng JSON.


**Cấu trúc logic tổng quát:**
```text
Client (Java Swing)  <--- TCP Socket (JSON) --->  Server (Java Multithreading)  <--- JDBC --->  Database (MySQL)
```

**Sơ đồ hệ thống:**

![System Diagram](./statics/diagram.png)

---

## ⚙️ CÔNG NGHỆ SỬ DỤNG

| Thành phần | Công nghệ | Ghi chú |
|------------|-----------|---------|
| Ngôn ngữ & Build | Java 17 + Apache Maven | Chia thành 2 Maven project độc lập (`client` và `server`) |
| Server | Java Core + TCP Socket + Multithreading | Xử lý kết nối đồng thời (`ClientHandler`), quản lý phòng & logic trận đấu |
| Client | Java Swing + FlatLaf | Giao diện desktop hiện đại, vẽ icon vector tùy chỉnh |
| Giao tiếp & Dữ liệu | TCP/IP + JSON (Jackson) | Trao đổi gói tin JSON theo từng dòng qua luồng TCP |
| Database | MySQL + JDBC | Lưu trữ tài khoản, thẻ kỹ năng, lịch sử trận và kỷ lục Solo (`card_memory_game`) |

---

## 🚀 HƯỚNG DẪN CHẠY DỰ ÁN

### 1. Yêu cầu môi trường & Clone repository
- Đảm bảo máy đã cài đặt **JDK 17+**, **Apache Maven** và **MySQL Server** (`java -version`, `mvn -version`).

```bash
git clone https://github.com/dhm-005/Memory-Lane_BTL-LTM-Nhom-6-N09
cd Memory-Lane_BTL-LTM-Nhom-6-N09
```

### 2. Cài đặt cơ sở dữ liệu MySQL
1. Mở MySQL Workbench (hoặc công cụ quản lý MySQL) và chạy toàn bộ script tạo bảng và dữ liệu mẫu tại:
   ```text
   database/schema.sql
   database/insert.sql
   ```
2. Sao chép file cấu hình mẫu `server/src/main/resources/db.properties.example` thành `server/src/main/resources/db.properties` và cập nhật thông tin kết nối

### 3. Chạy Server
**Cách 1 – Chạy nhanh bằng script (Windows):**
```bat
run-server.bat
```

**Cách 2 – Chạy bằng Maven hoặc JAR:**
```bash
cd source/server
mvn compile exec:java -Dexec.mainClass=com.cardgame.server.MainServer
```

### 4. Chạy Client
**Cách 1 – Chạy nhanh bằng script (Windows, kết nối mặc định `127.0.0.1:5000`):**
```bat
run-client.bat
```

**Cách 2 – Chạy bằng Maven hoặc kết nối qua mạng LAN / ZeroTier:**
```bash
cd source/client
mvn compile exec:java -Dexec.mainClass=com.cardgame.client.MainClient
```
Nếu chạy Client từ máy khác trong mạng LAN, truyền địa chỉ IPv4 của máy Server:
```bash
java -Dcardgame.host=192.168.1.10 -Dcardgame.port=5000 -jar target/cardgame-client-1.0.0.jar
```

### 5. Kiểm thử nhanh
Chạy bộ kiểm thử tự động tại thư mục `server`:
```bash
cd source/server
mvn test
```

---

## 🔗 GIAO TIẾP (GIAO THỨC SỬ DỤNG)

Client và Server giao tiếp qua **TCP Socket**, mỗi thông điệp là một chuỗi **JSON Object** trên một dòng độc lập (kết thúc bằng `\n`). Trường `type` xác định loại hành động/sự kiện. Chi tiết đầy đủ được mô tả tại `docs/PROTOCOL.md`.

| Nhóm chức năng | Message `type` (Client $\rightarrow$ Server) | Payload gửi lên (Ví dụ) | Phản hồi từ Server (`type`) |
|----------------|---------------------------------------------|-------------------------|-----------------------------|
| Xác thực | `REGISTER` / `LOGIN` | `{"type":"LOGIN","username":"player1","password":"123"}` | `LOGIN_OK` (kèm `username`, `coins`, `points`) hoặc `ERROR` |
| Phòng chờ | `LIST_ROOMS` / `CREATE_ROOM` / `JOIN_ROOM` | `{"type":"JOIN_ROOM","roomId":"1024"}` | `ROOMS`, `ROOM_JOINED`, `ROOM_STATE` |
| Chuẩn bị trận | `READY` / `RPS_CHOICE` / `CHOOSE_ORDER` / `BUY_CONFIRM` | `{"type":"RPS_CHOICE","choice":"ROCK"}` | `RPS_START`, `RPS_RESULT`, `CARD_OFFERS`, `GAME_START` |
| Trong trận PvP | `FLIP` / `USE_CARD` / `SKIP_CARD` | `{"type":"FLIP","index":5}` | `CARD_FLIP`, `ROUND_RESULT`, `TURN_CHANGED`, `GAME_OVER` |
| Chơi đơn (Solo) | `SOLO_START` / `SOLO_LEVEL_COMPLETE` | `{"type":"SOLO_LEVEL_COMPLETE","level":5,"timeMs":45200}` | `SOLO_BEST`, `SOLO_REWARD_OK` |
| Thống kê | `HISTORY` / `LEADERBOARD` / `CARD_COLLECTION` | `{"type":"LEADERBOARD"}` | `HISTORY`, `LEADERBOARD`, `SOLO_LEADERBOARD`, `CARD_COLLECTION` |

---

## 📊 KẾT QUẢ THỰC NGHIỆM

- **Hoạt động ổn định đa kết nối:** Server xử lý đồng thời nhiều Client đăng nhập, tạo nhiều phòng chờ riêng biệt (`RoomIsolationTest`, `MultipleRoomTest`) và vận hành nhiều trận đấu 1v1 song song (`MultipleMatchTest`) mà không xảy ra xung đột dữ liệu hay rò rỉ gói tin giữa các phòng.
- **Đồng bộ thời gian thực:** Trạng thái lật bài, tính điểm, sử dụng thẻ kỹ năng (tấn công, hỗ trợ, khiên chặn, may mắn) và kết quả thắng/thua được đồng bộ tức thì giữa hai người chơi và lưu trữ chính xác vào MySQL.

![Demo Result](./statics/result.png)

---

## 🧩 CẤU TRÚC DỰ ÁN
```
Memory-Lane_BTL-LTM-Nhom-6-N09/
├── README.md
├── INSTRUCTION.md
├── run-client.bat       # File chạy client nhanh
├── run-server.bat       # File chạy server nhanh
├── statics/
│   ├── diagram.png
│   └── dataset_sample.csv
└── source/
    ├── .gitignore
    ├── client/
    │   ├── pom.xml
    │   └── src/main/java/com/cardgame/client/
    │       ├── MainClient.java
    │       ├── controller/        # Xử lý điều hướng
    │       ├── model/             # Đối tượng dữ liệu phòng & phiên
    │       ├── network/           # Kết nối TCP Socket
    │       ├── util/              # Tiện ích
    │       └── view/              # Giao diện Swing
    ├── server/
    │   ├── pom.xml
    │   └── src/
    │       ├── main/
    │       │   ├── java/com/cardgame/server/
    │       │   │   ├── MainServer.java
    │       │   │   ├── config/        # Cấu hình ứng dụng
    │       │   │   ├── model/         # Thực thể nghiệp vụ
    │       │   │   ├── network/       # Đa luồng TCP
    │       │   │   ├── repository/    # Truy vấn JDBC MySQL
    │       │   │   ├── service/       # Xử lý logic nghiệp vụ
    │       │   │   └── util/          # Tiện ích JSON & mã hóa mật khẩu
    │       │   └── resources/
    │       │       └── db.properties.example
    │       └── test/java/com/cardgame/server/ # Kiểm thử
    ├── database/
    │   ├── schema.sql          #Tạo CSDL và bảng
    │   └── insert.sql          #Chèn dữ liệu mẫu
    └── docs/
```

---

## 🧩 HƯỚNG PHÁT TRIỂN THÊM

- [ ] Bổ sung tính năng kết bạn, mời bạn bè trực tiếp vào phòng và khung chat nhanh trong trận đấu.
- [ ] Thêm chế độ đấu xếp hạng tự động ghép cặp (Matchmaking) theo mức điểm ELO/Ranking.
- [ ] Bổ sung hiệu ứng âm thanh, hoạt ảnh lật thẻ mượt mà hơn và đa dạng hóa bộ thẻ kỹ năng.
- [ ] Đóng gói bằng Docker / Docker Compose để triển khai Server và MySQL lên máy chủ cloud dễ dàng.

---

## 📝 GHI CHÚ

- Không đưa file `db.properties` chứa mật khẩu MySQL thật lên kho lưu trữ Git công khai.
- Khi triển khai qua mạng LAN hoặc Internet, cần mở cổng TCP của Server (mặc định `5000`) trên tường lửa (Windows Firewall).

---

## 📚 TÀI LIỆU THAM KHẢO

- Tài liệu chính thức Java SE 17 (`java.net.Socket`, `java.net.ServerSocket`, `javax.swing`, `java.sql`).
- Tài liệu thư viện giao diện [FlatLaf – Flat Look and Feel](https://www.formdev.com/flatlaf/).
- Tài liệu thư viện xử lý JSON [FasterXML Jackson](https://github.com/FasterXML/jackson).