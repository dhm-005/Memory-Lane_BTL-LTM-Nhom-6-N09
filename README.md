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

### Vai trò các thư mục

| Thành phần | Vai trò |
|---|---|
| `client` | Giao diện người chơi, xử lý thao tác và giao tiếp với Server |
| `server` | Xử lý tài khoản, phòng chơi, trận đấu, thẻ và dữ liệu |
| `database` | Chứa script khởi tạo cơ sở dữ liệu MySQL |
| `docs` | Tài liệu kiến trúc và đặc tả giao thức |
| `run-client.bat` | Script khởi chạy Client trên Windows |
| `run-server.bat` | Script khởi chạy Server trên Windows |

### Tài liệu trong thư mục docs

- `ARCHITECTURE.md`: mô tả kiến trúc tổng thể và cách các thành phần Client, Server, Database phối hợp với nhau.
- `PROTOCOL.md`: mô tả giao thức JSON dùng để trao đổi message giữa Client và Server.
