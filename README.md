# MEMORY LANE

Memory Duel là game lật thẻ ghi nhớ được phát triển bằng Java. Dự án được xây dựng theo mô hình Client–Server, sử dụng giao diện Java Swing và cơ sở dữ liệu MySQL để lưu trữ thông tin người chơi, thẻ và kết quả chơi.

Dự án gồm hai Maven project độc lập:

- **Client:** Hiển thị giao diện và gửi yêu cầu đến Server.
- **Server:** Xử lý tài khoản, phòng chơi, logic trận đấu và dữ liệu.

## 1. Công nghệ sử dụng

- Java 17
- Apache Maven
- Java Swing
- FlatLaf
- TCP Socket
- JSON – Jackson
- MySQL
- JDBC

## 2. Chức năng chính

### Tài khoản
- Đăng ký tài khoản.
- Đăng nhập và đăng xuất.
- Đổi mật khẩu.
- Lưu thông tin tài khoản trong MySQL.

### Phòng chơi và PvP
- Xem danh sách phòng.
- Tạo phòng và tham gia phòng.
- Sẵn sàng hoặc hủy trạng thái sẵn sàng.
- Chọn thứ tự chơi bằng kéo, búa, bao.
- Mua thẻ trước khi bắt đầu trận.
- Lật thẻ và tính điểm.
- Sử dụng thẻ kỹ năng trong trận.
- Xử lý kết quả trận đấu.

### Chơi đơn
- Tham gia các màn chơi đơn.
- Theo dõi tiến trình chơi.
- Lưu thành tích tốt nhất.
- Ghi nhận kết quả hoàn thành.

### Thống kê
- Xem lịch sử trận đấu.
- Xem bảng xếp hạng.
- Xem bộ sưu tập thẻ.
- Theo dõi xu và điểm xếp hạng.

## 3. Cấu trúc dự án

```text
Memory-Lane_BTL-LTM-Nhom-6-N09/
├── .gitignore
├── README.md
├── run-client.bat
├── run-server.bat
│
├── client/
│   ├── pom.xml
│   └── src/main/java/com/cardgame/client/
│       ├── MainClient.java
│       ├── controller/
│       │   └── LoginController.java
│       ├── model/
│       │   ├── RoomInfo.java
│       │   └── Session.java
│       ├── network/
│       │   └── NetworkClient.java
│       ├── util/
│       │   └── Json.java
│       └── view/
│           ├── DataPagePanel.java
│           ├── LobbyPanel.java
│           ├── LoginPanel.java
│           ├── MainFrame.java
│           ├── PvPGamePanel.java
│           ├── RegisterPanel.java
│           ├── ServerMessageHandler.java
│           ├── SoloGamePanel.java
│           ├── UiTheme.java
│           ├── WaitingPanel.java
│           ├── components/
│           │   ├── CardTile.java
│           │   ├── IconPainter.java
│           │   ├── RoundedPanel.java
│           │   └── VectorIcon.java
│           └── dialogs/
│               ├── CardPurchaseDialog.java
│               ├── CardUseDialog.java
│               ├── ChangePasswordDialog.java
│               ├── MatchCancelledDialog.java
│               ├── MatchResultDialog.java
│               ├── OrderChoiceDialog.java
│               ├── RpsDialog.java
│               └── SoloResultDialog.java
│
├── database/
│   └── schema.sql
│
├── docs/
│   ├── ARCHITECTURE.md
│   └── PROTOCOL.md
│
└── server/
    ├── pom.xml
    └── src/main/
        ├── java/com/cardgame/server/
        │   ├── MainServer.java
        │   ├── config/
        │   │   └── DatabaseConfig.java
        │   ├── model/
        │   │   ├── Card.java
        │   │   ├── GameResult.java
        │   │   ├── Match.java
        │   │   ├── Room.java
        │   │   └── User.java
        │   ├── network/
        │   │   ├── ClientHandler.java
        │   │   ├── MessageDispatcher.java
        │   │   ├── OnlineUsers.java
        │   │   └── TcpServer.java
        │   ├── repository/
        │   │   ├── CardRepository.java
        │   │   ├── GameRepository.java
        │   │   ├── SoloRecordRepository.java
        │   │   └── UserRepository.java
        │   ├── service/
        │   │   ├── AuthService.java
        │   │   ├── CardService.java
        │   │   ├── GameService.java
        │   │   ├── HistoryService.java
        │   │   ├── RoomRegistry.java
        │   │   ├── RoomService.java
        │   │   └── SoloService.java
        │   └── util/
        │       ├── Json.java
        │       └── PasswordHasher.java
        └── resources/
            └── db.properties.example
```

### Vai trò các thành phần

| Thành phần | Mô tả |
|---|---|
| `client` | Giao diện người chơi và xử lý thao tác trên Client |
| `server` | Xử lý nghiệp vụ, kết nối và dữ liệu game |
| `database` | Script tạo cơ sở dữ liệu MySQL |
| `docs` | Tài liệu kiến trúc và giao thức giao tiếp |
| `run-client.bat` | Khởi chạy Client trên Windows |
| `run-server.bat` | Khởi chạy Server trên Windows |

## 4. Yêu cầu môi trường

Cần cài đặt các phần mềm sau:

- JDK 17 trở lên.
- Apache Maven.
- MySQL Server.
- IDE Java như IntelliJ IDEA hoặc NetBeans (không bắt buộc).

Kiểm tra Java:

```bash
java -version
```

Kiểm tra Maven:

```bash
mvn -version
```

## 5. Cài đặt cơ sở dữ liệu

### Bước 1: Tạo Database

Mở MySQL Workbench hoặc công cụ quản lý MySQL.

Chạy toàn bộ file:

```text
database/schema.sql
```

File SQL sẽ tạo Database `card_memory_game` và các bảng phục vụ lưu trữ tài khoản, danh mục thẻ, kết quả trận đấu và thành tích chơi đơn.

### Bước 2: Cấu hình kết nối MySQL

File cấu hình mẫu có sẵn tại:

```text
server/src/main/resources/db.properties.example
```

Tạo file cấu hình thực tế:

```text
server/src/main/resources/db.properties
```

Nếu chưa có file `db.properties`, có thể sao chép file mẫu và đổi tên thành `db.properties`.

Mở file `db.properties`, cấu hình thông tin kết nối:

```properties
db.url=jdbc:mysql://localhost:3306/card_memory_game?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true
db.username=root
db.password=YOUR_MYSQL_PASSWORD
server.port=5000
```

Thay `YOUR_MYSQL_PASSWORD` bằng mật khẩu MySQL trên máy chạy Server.

Lưu ý:

- Database phải có tên `card_memory_game`.
- Username và password phải đúng với tài khoản MySQL.
- `server.port` là cổng TCP của Server, mặc định là `5000`.
- Không đưa file `db.properties` chứa mật khẩu thật lên Git.

### Cấu hình bằng biến môi trường

Server hỗ trợ các biến môi trường sau:

```text
CARDGAME_DB_URL
CARDGAME_DB_USER
CARDGAME_DB_PASSWORD
```

Các biến môi trường này được ưu tiên hơn thông tin tương ứng trong file `db.properties`.

## 6. Chạy chương trình trên Windows

### Cách 1: Sử dụng file BAT

Đây là cách chạy nhanh được cung cấp trong project.

#### Khởi động Server

Tại thư mục gốc project, chạy:

```bat
run-server.bat
```

Script sẽ kiểm tra file cấu hình Database. Nếu chưa có `db.properties` và file mẫu tồn tại, script sẽ sao chép file mẫu thành `db.properties`.

Sau đó Server được chạy bằng JAR đã build nếu có; nếu chưa có JAR, script sẽ chạy thông qua Maven.

Server cần kết nối MySQL thành công trước khi bắt đầu nhận kết nối từ Client.

#### Khởi động Client

Mở một terminal khác tại thư mục gốc project:

```bat
run-client.bat
```

Client mặc định kết nối đến:

```text
127.0.0.1:5000
```

Nếu Client và Server chạy trên cùng máy, có thể sử dụng cấu hình mặc định.

### Cách 2: Chạy bằng Maven

Mở terminal tại thư mục gốc project.

#### Chạy Server

```bat
cd server
mvn compile exec:java -Dexec.mainClass=com.cardgame.server.MainServer
```

#### Chạy Client

Mở terminal khác:

```bat
cd client
mvn compile exec:java -Dexec.mainClass=com.cardgame.client.MainClient
```

Khi chạy Server trực tiếp bằng Maven, cần đảm bảo file `db.properties` đã được đặt trong `server/src/main/resources/`.

## 7. Build JAR

Có thể đóng gói Client và Server thành JAR để chạy độc lập với IDE.

### Build Client

```bat
cd client
mvn clean package
```

JAR được tạo tại:

```text
client/target/cardgame-client-1.0.0.jar
```

Chạy Client:

```bat
java -jar target/cardgame-client-1.0.0.jar
```

### Build Server

```bat
cd server
mvn clean package
```

JAR được tạo tại:

```text
server/target/cardgame-server-1.0.0.jar
```

Chạy Server:

```bat
java -jar target/cardgame-server-1.0.0.jar
```

Server vẫn cần MySQL đang hoạt động và file `db.properties` được cấu hình đúng.

## 8. Kết nối Client từ máy khác

Để chơi qua mạng LAN hoặc ZeroTier, cần chạy Server trên một máy và cho các Client kết nối đến địa chỉ IP của máy đó.

### Trên máy chạy Server

1. Khởi động MySQL.
2. Kiểm tra cấu hình `db.properties`.
3. Chạy Server.
4. Cho phép kết nối TCP đến cổng Server trong Windows Firewall.

Để xem địa chỉ IP của máy Server, mở CMD và chạy:

```bat
ipconfig
```

Tìm địa chỉ IPv4 của card mạng đang sử dụng.

Ví dụ:

```text
192.168.1.10
```

### Trên máy Client

Client phải kết nối đến IP của máy Server, không sử dụng `127.0.0.1`.

Chạy Client JAR bằng lệnh:

```bat
java -Dcardgame.host=192.168.1.10 -Dcardgame.port=5000 -jar target/cardgame-client-1.0.0.jar
```

Thay `192.168.1.10` bằng IP thực tế của máy Server.

Nếu chạy bằng `run-client.bat`, có thể đặt biến môi trường trước khi chạy:

```bat
set CARDGAME_HOST=192.168.1.10
set CARDGAME_PORT=5000
run-client.bat
```

Hai máy cần kết nối được với nhau qua mạng. Nếu không kết nối được, hãy kiểm tra địa chỉ IP, cổng Server và Windows Firewall.

## 9. Giao thức Client–Server

Client và Server giao tiếp thông qua TCP Socket.

Mỗi message là một đối tượng JSON được gửi trên một dòng riêng biệt, kết thúc bằng ký tự xuống dòng.

Ví dụ:

```json
{
  "type": "LOGIN",
  "username": "player1",
  "password": "password"
}
```

Trường `type` xác định loại yêu cầu. Các trường còn lại chứa dữ liệu tương ứng.

Chi tiết về message, trường dữ liệu và phản hồi được mô tả trong:

```text
docs/PROTOCOL.md
```

Khi thay đổi giao thức, cần cập nhật đồng thời Client và Server để tránh sai lệch tên message hoặc cấu trúc dữ liệu.

## 10. Tài liệu dự án

### `docs/ARCHITECTURE.md`

Mô tả kiến trúc tổng thể của hệ thống, vai trò của Client, Server và Database, cũng như cách các thành phần phối hợp với nhau.

### `docs/PROTOCOL.md`

Mô tả giao thức JSON dùng để trao đổi dữ liệu giữa Client và Server, bao gồm các loại message và cấu trúc dữ liệu tương ứng.

### `database/schema.sql`

Chứa script tạo Database, bảng dữ liệu và dữ liệu khởi tạo cần thiết cho game.

## 11. Xử lý lỗi thường gặp

### Server không kết nối được MySQL

Kiểm tra:

- MySQL đã được khởi động chưa.
- Database `card_memory_game` đã được tạo chưa.
- Username và password trong `db.properties` có đúng không.
- MySQL có đang sử dụng cổng mặc định `3306` không.

### Client không kết nối được Server

Kiểm tra:

- Server đã chạy chưa.
- Client có sử dụng đúng IP và port không.
- Nếu chạy trên máy khác, không dùng `127.0.0.1`.
- Windows Firewall đã cho phép kết nối TCP đến cổng Server chưa.

### Maven không build được

Kiểm tra:

```bat
java -version
mvn -version
```

Đảm bảo đang sử dụng JDK 17 trở lên và Maven có thể tải các dependency từ Internet.

### Cổng Server đã được sử dụng

Nếu cổng `5000` đang được chương trình khác sử dụng, hãy dừng chương trình đó hoặc thay đổi `server.port` trong `db.properties`.

Client cũng cần kết nối đến đúng cổng mới.

## 12. Lưu ý

- Server xử lý logic trận đấu và dữ liệu game.
- Client gửi yêu cầu và hiển thị dữ liệu nhận được từ Server.
- Dữ liệu tài khoản, kết quả trận đấu và thành tích được lưu trong MySQL.
- Không chia sẻ hoặc commit file `db.properties` chứa mật khẩu thật.
- Khi triển khai qua mạng, chỉ mở cổng TCP cần thiết cho Server.

---

**Memory Lane – Java TCP Client–Server Card Matching Game**
