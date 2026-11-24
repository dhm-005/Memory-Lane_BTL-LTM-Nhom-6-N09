# Architecture notes

## Build boundaries

`client/` and `server/` are independent Java 17 Maven projects. The only integration contract is the newline-delimited JSON protocol documented in `PROTOCOL.md`; no third shared Maven module is introduced.

## Server

- `MainServer` is the startup entry point and exposes only small compatibility helpers/constants.
- `DatabaseConfig` owns JDBC connection creation. Repositories no longer obtain connections through `MainServer`.
- `UserRepository` owns account, balance and ranking-point SQL.
- `CardService` owns the ordered card catalogue, the existing 12-card database override/fallback rule, card serialization and collection response.
- `OnlineUsers` and `RoomRegistry` own the in-memory registries.
- `RoomService` owns room discovery/creation/joining and exposes room lifecycle entry points.
- `GameService` routes PvP commands and owns the original four-thread scheduler.
- `SoloService` owns Solo progression validation, records, rewards and Solo leaderboard responses.
- `HistoryService` owns PvP history and leaderboard responses.
- `GameRepository` owns game-result persistence as well as history/ranking queries.
- `Room` remains the synchronization monitor and owns waiting-room, pre-match purchase and room lifecycle state. Its active board/turn state is held by `Match`; `Match` also contains pure turn-choice and random-pair helpers. All changes to a match must continue to synchronize on its owning `Room`.

## Client

- `Session` groups account and room state previously held as unrelated `MainFrame` fields.
- `UiTheme` owns shared colors and the symbol catalogue, removing component-to-frame palette dependencies.
- `DataPagePanel` directly builds history, leaderboard and collection pages; the three wrapper classes were removed.
- `PvPGamePanel` registers the log area directly with `MainFrame`; log writes no longer traverse the whole component tree.
- `ServerMessageHandler` remains a dedicated message dispatcher. Swing updates and modal dialogs remain on the EDT.

## Compatibility rules

The project keeps the existing board size (36), pair count (18), symbol count (32), purchase timeout (30 seconds), missed-pair timeout (1.5 seconds), and private reveal timeout (2 seconds). The Server enforces a 10-coin reserve for 1v1 participation and purchases, settles match rewards/penalties transactionally, and validates Solo progression and timing. No reconnect behavior is introduced.

## Verification limitation

The source was syntax/type-smoke-checked with `javac` against minimal local API stubs because Maven and the real dependency artifacts were unavailable in the editing environment. This is not a substitute for `mvn clean package` with the real dependencies or an end-to-end two-client/MySQL game test. Run those checks on a machine with Maven, JDK 17 and MySQL before deployment.
