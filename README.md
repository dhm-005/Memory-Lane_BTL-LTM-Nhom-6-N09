# CardGame

Java 17 card-matching game with separate Maven projects for Client and Server. The Client is Swing/FlatLaf; the Server is authoritative for PvP and uses TCP sockets with newline-delimited JSON. Both Client and Server use Jackson 3.3.0 for JSON; only the Server accesses MySQL.

## Requirements

- JDK 17+
- Maven 3.8+
- MySQL 8+

## Database setup

1. Run `database/schema.sql` in MySQL to create/update `card_memory_game` and its tables. The script widens `users.password_hash` for salted PBKDF2 hashes; existing SHA-256 passwords are upgraded automatically at the next successful login.
2. Copy `server/src/main/resources/db.properties.example` to `server/src/main/resources/db.properties`.
3. Set your local MySQL username/password in `db.properties`. This local file is ignored by Git and is not included in the source ZIP.

Alternatively configure `CARDGAME_DB_URL`, `CARDGAME_DB_USER`, and `CARDGAME_DB_PASSWORD` environment variables. Server port defaults to `5000` and can be set with `server.port` in the properties file.

## Run from source

Start Server:

```bash
cd server
mvn compile exec:java -Dexec.mainClass=com.cardgame.server.MainServer
```

Start Client in another terminal:

```bash
cd client
mvn compile exec:java -Dexec.mainClass=com.cardgame.client.MainClient
```

On Windows, `run-server.bat` and `run-client.bat` at the project root perform these steps. Client mặc định kết nối `127.0.0.1:5000`; khi chạy qua LAN/ZeroTier, đặt biến môi trường `CARDGAME_HOST` và tùy chọn `CARDGAME_PORT` trước khi chạy `run-client.bat`.

## Build a standalone Client JAR

```bash
cd client
mvn clean package
java -jar target/cardgame-client-1.0.0.jar
```

The Shade plugin packages runtime dependencies into the Client JAR. Copy the resulting JAR to another computer with Java 17+.

## Connect a Client on another computer

On the Server computer, allow inbound TCP port `5000` through the firewall and start the Server. On the other computer, use the Server computer's LAN IP (the Client defaults to `127.0.0.1` for local play):

```bash
java -Dcardgame.host=192.168.1.10 -Dcardgame.port=5000 -jar target/cardgame-client-1.0.0.jar
```

Replace `192.168.1.10` with the actual LAN or ZeroTier IP of the Server computer. Both computers must be able to reach each other over the network. The connection attempt runs in the background and times out after 5 seconds.

## Project structure

```text
CardGame/
├── client/src/main/java/com/cardgame/client/
│   ├── controller/       # Authentication form controller
│   ├── model/            # Session and room data
│   ├── network/          # TCP Client
│   ├── util/             # JSON helpers
│   └── view/             # Swing screens, dialogs, UI theme and widgets
├── server/src/main/java/com/cardgame/server/
│   ├── config/           # JDBC configuration
│   ├── model/            # Card, Room and Match board state
│   ├── network/          # TCP sessions, dispatcher and online registry
│   ├── repository/       # MySQL access
│   ├── service/          # Auth, room, game, Solo, history and card services
│   └── util/             # JSON helpers
├── database/schema.sql
├── docs/PROTOCOL.md
├── run-client.bat
└── run-server.bat
```

Client and Server remain independent Maven projects. Their JSON contract is documented in `docs/PROTOCOL.md`; no shared Maven module is required.
