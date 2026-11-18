# TỔNG HỢP CÔNG VIỆC NGƯỜI 2 – PHÒNG CHƠI (NGÀY 1–6)

**Dự án:** Memory Lane – Lật Thẻ Nhớ Đối Kháng 1v1  
**Phạm vi:** Quản lý phòng chơi, sảnh chờ, kết nối TCP và cập nhật trạng thái phòng realtime.

## Mục lục

- [TỔNG HỢP CÔNG VIỆC NGƯỜI 2 – PHÒNG CHƠI (NGÀY 1–6)](#tổng-hợp-công-việc-người-2--phòng-chơi-ngày-16)
  - [Mục lục](#mục-lục)
  - [1. Tổng quan chức năng](#1-tổng-quan-chức-năng)
  - [2. Danh sách file theo từng ngày](#2-danh-sách-file-theo-từng-ngày)
    - [Ngày 1 – Xây dựng lõi quản lý phòng trên Server](#ngày-1--xây-dựng-lõi-quản-lý-phòng-trên-server)
      - [File tạo mới](#file-tạo-mới)
      - [Các hàm nghiệp vụ quan trọng](#các-hàm-nghiệp-vụ-quan-trọng)
    - [Ngày 2 – Tầng mạng Server và cơ chế broadcast](#ngày-2--tầng-mạng-server-và-cơ-chế-broadcast)
      - [File tạo mới](#file-tạo-mới-1)
      - [File chỉnh sửa](#file-chỉnh-sửa)
    - [Ngày 3 – Tầng mạng Client](#ngày-3--tầng-mạng-client)
      - [File tạo mới](#file-tạo-mới-2)
      - [File chỉnh sửa](#file-chỉnh-sửa-1)
      - [Các thành phần quan trọng trong NetworkClient](#các-thành-phần-quan-trọng-trong-networkclient)
    - [Ngày 4 – Giao diện sảnh chờ](#ngày-4--giao-diện-sảnh-chờ)
      - [File tạo mới](#file-tạo-mới-3)
      - [Các thành phần chính](#các-thành-phần-chính)
    - [Ngày 5 – LobbyController và realtime](#ngày-5--lobbycontroller-và-realtime)
      - [File tạo mới](#file-tạo-mới-4)
      - [Các hàm quan trọng trong LobbyController](#các-hàm-quan-trọng-trong-lobbycontroller)
    - [Ngày 6 – Kiểm thử và xử lý trường hợp biên](#ngày-6--kiểm-thử-và-xử-lý-trường-hợp-biên)
      - [File kiểm thử có sẵn / bổ sung](#file-kiểm-thử-có-sẵn--bổ-sung)
      - [Kết quả kiểm thử](#kết-quả-kiểm-thử)
  - [3. Giao thức TCP/JSON cần quan tâm khi tích hợp](#3-giao-thức-tcpjson-cần-quan-tâm-khi-tích-hợp)
    - [3.1. Quy tắc truyền dữ liệu](#31-quy-tắc-truyền-dữ-liệu)
    - [3.2. Cấu trúc message](#32-cấu-trúc-message)
    - [3.3. Các message type](#33-các-message-type)
    - [3.4. Các trường dữ liệu quan trọng](#34-các-trường-dữ-liệu-quan-trọng)
    - [3.5. Các event phòng](#35-các-event-phòng)
    - [3.6. Mã lỗi cần xử lý](#36-mã-lỗi-cần-xử-lý)
  - [4. Luồng hoạt động tổng thể](#4-luồng-hoạt-động-tổng-thể)
  - [5. Những điểm cần thống nhất khi tích hợp với thành viên khác](#5-những-điểm-cần-thống-nhất-khi-tích-hợp-với-thành-viên-khác)
    - [Với Người 1 – Tài khoản / đăng nhập](#với-người-1--tài-khoản--đăng-nhập)
    - [Với Người 3 – Gameplay](#với-người-3--gameplay)
    - [Với cả nhóm](#với-cả-nhóm)
  - [6. Trạng thái và việc còn tồn đọng](#6-trạng-thái-và-việc-còn-tồn-đọng)
---

## 1. Tổng quan chức năng

Phần Người 2 phụ trách xây dựng luồng quản lý phòng chơi, bao gồm:

- Tạo phòng mới.
- Hiển thị danh sách phòng đang hoạt động.
- Tham gia và rời phòng.
- Giới hạn tối đa 2 người/phòng.
- Chuyển quyền chủ phòng khi host rời đi.
- Tự động xóa phòng khi người cuối cùng rời đi.
- Đồng bộ trạng thái phòng giữa các client thông qua TCP và sự kiện `ROOM_UPDATE`.
- Xử lý các trường hợp biên: phòng đầy, người chơi đã ở phòng khác, mất kết nối và nhiều client cùng tham gia.

**Nguyên tắc thiết kế:**
- Trạng thái phòng được lưu trong RAM server, chưa sử dụng bảng `rooms` trong MySQL.
- Client chỉ hiển thị giao diện và gửi yêu cầu; server chịu trách nhiệm xác thực và cập nhật trạng thái phòng.
- Giao diện (`LobbyFrame`) tách biệt với xử lý nghiệp vụ (`LobbyController`).
- Các thành viên khác có thể mở rộng giao thức thông qua `TypeHandler`, hạn chế sửa các file mạng dùng chung.

---

## 2. Danh sách file theo từng ngày

### Ngày 1 – Xây dựng lõi quản lý phòng trên Server

**Mục tiêu:** Xây dựng nghiệp vụ quản lý phòng độc lập với TCP Socket.

#### File tạo mới

| Đường dẫn | Tác dụng / mục đích |
|---|---|
| `server/src/main/java/com/mcserver/model/room/RoomStatus.java` | Enum trạng thái phòng: `WAITING`, `FULL`, `PLAYING`. |
| `server/src/main/java/com/mcserver/model/room/RoomPlayer.java` | Record lưu thông tin người chơi trong phòng: `userId`, `username`. |
| `server/src/main/java/com/mcserver/model/room/Room.java` | Đối tượng phòng, quản lý danh sách người chơi, host, trạng thái và giới hạn 2 người. |
| `server/src/main/java/com/mcserver/dto/response/RoomPlayerDTO.java` | DTO thông tin người chơi gửi về client. |
| `server/src/main/java/com/mcserver/dto/response/RoomDTO.java` | DTO biểu diễn thông tin phòng gửi qua mạng. |
| `server/src/main/java/com/mcserver/dto/response/RoomUpdateEvent.java` | Dữ liệu sự kiện cập nhật phòng, gồm event, roomId, version và danh sách rooms. |
| `server/src/main/java/com/mcserver/service/room/RoomException.java` | Ngoại lệ nghiệp vụ, chứa mã lỗi và thông báo. |
| `server/src/main/java/com/mcserver/service/room/RoomBroadcaster.java` | Interface phát sự kiện cập nhật phòng; giúp tách nghiệp vụ khỏi tầng mạng. |
| `server/src/main/java/com/mcserver/service/room/RoomService.java` | Xử lý tạo, tham gia, rời phòng, chuyển host, xóa phòng và quản lý version. |
| `server/src/test/java/com/mcserver/service/room/RoomServiceTest.java` | Kiểm thử nghiệp vụ phòng, giới hạn người chơi và cạnh tranh đa luồng. |

#### Các hàm nghiệp vụ quan trọng

| Hàm | Chức năng |
|---|---|
| `createRoom(userId, username)` | Tạo phòng và đưa người tạo thành host. |
| `joinRoom(roomId, userId, username)` | Cho người chơi tham gia phòng nếu phòng còn chỗ. |
| `leaveRoom(userId)` | Rời phòng; chuyển host hoặc đóng phòng nếu cần. |
| `listRooms()` | Trả về snapshot danh sách phòng hiện tại. |
| `handleDisconnect(userId)` | Xử lý người chơi mất kết nối đột ngột. |
| `markPlaying(roomId)` | Chuyển phòng sang trạng thái đang chơi, phục vụ tích hợp module game. |

**Điểm kỹ thuật cần lưu ý:**
- Dùng `synchronized` để bảo vệ trạng thái phòng và tránh race condition.
- Thực hiện broadcast sau khi nhả khóa, tránh I/O làm chặn các thao tác phòng khác.
- `version` tăng dần sau mỗi lần thay đổi trạng thái.
- Phòng tối đa 2 người; người thứ ba nhận `ROOM_FULL`.

### Ngày 2 – Tầng mạng Server và cơ chế broadcast

**Mục tiêu:** Tiếp nhận message TCP, định tuyến tới đúng handler và gửi sự kiện cập nhật phòng cho các client.

#### File tạo mới

| Đường dẫn | Tác dụng / mục đích |
|---|---|
| `server/src/main/java/com/mcserver/network/Message.java` | Cấu trúc message chung cho phản hồi và sự kiện server chủ động đẩy. |
| `server/src/main/java/com/mcserver/network/TypeHandler.java` | Interface để từng nhóm chức năng đăng ký các message type mình xử lý. |
| `server/src/main/java/com/mcserver/network/MessageDispatcher.java` | Đọc JSON, xác định `type` và chuyển yêu cầu tới handler tương ứng. |
| `server/src/main/java/com/mcserver/network/ClientSession.java` | Quản lý một kết nối client, thông tin xác thực và gửi message an toàn giữa nhiều luồng. |
| `server/src/main/java/com/mcserver/network/SessionRegistry.java` | Quản lý các session đang kết nối và broadcast tới client đăng ký nhận cập nhật sảnh. |
| `server/src/main/java/com/mcserver/network/LobbyRoomBroadcaster.java` | Triển khai `RoomBroadcaster`, chuyển sự kiện phòng thành message `ROOM_UPDATE`. |
| `server/src/main/java/com/mcserver/network/RoomMessageHandler.java` | Xử lý `LIST_ROOMS`, `CREATE_ROOM`, `JOIN_ROOM`, `LEAVE_ROOM`. |

#### File chỉnh sửa

| Đường dẫn | Nội dung thay đổi |
|---|---|
| `server/src/main/java/com/mcserver/network/TcpServer.java` | Tích hợp `MessageDispatcher`, `SessionRegistry`, `RoomService`; quản lý session khi client kết nối/ngắt kết nối và gọi xử lý disconnect. |

**Không chủ động sửa:** `TcpRequestHandler.java` và các test cũ của nó. `MessageDispatcher` vẫn hỗ trợ giao thức legacy không có trường `type`, nhằm giữ tương thích với chức năng cũ như `health`, `matches.*`.

**Điểm kỹ thuật cần lưu ý:**
- `ClientSession` sử dụng `ReentrantLock` để tránh các luồng ghi đồng thời làm hỏng dữ liệu.
- `SessionRegistry` dùng tập hợp đồng thời để quản lý session.
- `RoomMessageHandler` dùng `identify()` ở chế độ DEV vì hệ thống đăng nhập của Người 1 chưa tích hợp.
- Khi client ngắt kết nối, `TcpServer` gọi `roomService.handleDisconnect()` để giải phóng trạng thái phòng.

### Ngày 3 – Tầng mạng Client

**Mục tiêu:** Xây dựng kết nối TCP lâu dài, phân biệt phản hồi và sự kiện server chủ động đẩy.

#### File tạo mới

| Đường dẫn | Tác dụng / mục đích |
|---|---|
| `client/src/main/java/com/mcclient/util/Json.java` | Cấu hình Jackson `ObjectMapper`, hỗ trợ JSON và `LocalDateTime`. |
| `client/src/main/java/com/mcclient/network/ServerException.java` | Biểu diễn lỗi trả về từ server, giữ mã lỗi để client xử lý. |
| `client/src/main/java/com/mcclient/network/NetworkClient.java` | Quản lý socket TCP dùng chung, gửi request, đọc phản hồi, phân phối push event. |
| `client/src/main/java/com/mcclient/model/RoomPlayer.java` | Model thông tin người chơi nhận từ server. |

#### File chỉnh sửa

| Đường dẫn | Nội dung thay đổi |
|---|---|
| `client/pom.xml` | Bổ sung Jackson Databind và Jackson JSR310 để xử lý JSON và thời gian. |
| `client/src/main/java/com/mcclient/model/RoomInfo.java` | Chuyển `id` sang `String`; bổ sung `hostName`, `maxPlayers`, `players`, `hasPlayer()`. |

#### Các thành phần quan trọng trong NetworkClient

| Thành phần | Chức năng |
|---|---|
| `connect(host, port)` | Thiết lập kết nối TCP tới server. |
| `request(type, data)` | Gửi yêu cầu và trả về `CompletableFuture` tương ứng. |
| `addPushListener(listener)` | Đăng ký nhận sự kiện server chủ động đẩy. |
| `readLoop()` | Đọc liên tục các dòng JSON từ server. |
| `dispatch(node)` | Phân biệt phản hồi có `requestId` và sự kiện push không có `requestId`. |
| `onDisconnected()` | Xử lý khi kết nối bị đóng, thông báo các request đang chờ và phát sự kiện mất kết nối. |
| `close()` | Đóng socket. |

**Lưu ý:** `NetworkClient` là kết nối dùng chung cho các chức năng client, không nên tạo một socket riêng cho từng controller.

### Ngày 4 – Giao diện sảnh chờ

**Mục tiêu:** Xây dựng giao diện hiển thị danh sách phòng và phòng chờ.

#### File tạo mới

| Đường dẫn | Tác dụng / mục đích |
|---|---|
| `client/src/main/java/com/mcclient/view/LobbyFrame.java` | Giao diện sảnh và phòng chờ, sử dụng `CardLayout` để chuyển giữa hai màn hình. |

#### Các thành phần chính

| Hàm / thành phần | Chức năng |
|---|---|
| `buildLobbyCard()` | Tạo giao diện danh sách phòng và các nút thao tác. |
| `buildRoomCard()` | Tạo giao diện phòng chờ gồm hai slot người chơi. |
| `showRooms(rooms)` | Hiển thị danh sách phòng. |
| `showRoom(room, myId)` | Hiển thị thành viên, host và trạng thái phòng. |
| `showLobby()` | Chuyển về sảnh. |
| `setBusy(busy)` | Khóa/mở các nút khi đang xử lý request. |
| `onCreate()`, `onJoin()`, `onLeave()`, `onRefresh()` | Đăng ký callback để Controller xử lý hành vi. |

**Nguyên tắc:** `LobbyFrame` chỉ phụ trách hiển thị, không trực tiếp xử lý socket hay nghiệp vụ phòng.

### Ngày 5 – LobbyController và realtime

**Mục tiêu:** Kết nối giao diện với tầng mạng và xử lý các thao tác người dùng.

#### File tạo mới

| Đường dẫn | Tác dụng / mục đích |
|---|---|
| `client/src/main/java/com/mcclient/controller/LobbyController.java` | Điều phối thao tác người dùng, gửi request, nhận push event và cập nhật giao diện. |
| `client/src/main/java/com/mcclient/LobbyDev.java` | Launcher tạm để chạy thử sảnh chờ bằng tài khoản DEV, chưa cần luồng đăng nhập hoàn chỉnh. |

#### Các hàm quan trọng trong LobbyController

| Hàm | Chức năng |
|---|---|
| `start()` | Hiển thị giao diện và tải danh sách phòng ban đầu. |
| `loadRooms()` | Gửi `LIST_ROOMS`. |
| `createRoom()` | Gửi `CREATE_ROOM`. |
| `joinRoom(roomId)` | Gửi `JOIN_ROOM`. |
| `leaveRoom()` | Gửi `LEAVE_ROOM`, chuyển giao diện về sảnh. |
| `identity()` | Tạo dữ liệu nhận diện DEV gồm `userId`, `username`. |
| `call()` | Hàm dùng chung để gửi request và xử lý phản hồi bất đồng bộ. |
| `onPush()` | Tiếp nhận `ROOM_UPDATE` và `DISCONNECTED`. |
| `applySnapshot()` | Cập nhật danh sách phòng khi nhận version mới hơn. |
| `syncRoomView()` | Xác định client còn ở phòng nào và cập nhật giao diện tương ứng. |

**Điểm kỹ thuật cần lưu ý:**
- Cập nhật Swing UI bằng `SwingUtilities.invokeLater()` để đảm bảo chạy trên EDT.
- Phản hồi và push event cùng sử dụng `applySnapshot()`.
- Dùng `version` để bỏ qua snapshot trùng hoặc cũ.
- Xác định trạng thái đang ở phòng bằng `RoomInfo.hasPlayer()` thay vì phụ thuộc thứ tự nhận response/push.

### Ngày 6 – Kiểm thử và xử lý trường hợp biên

**Mục tiêu:** Xác minh nghiệp vụ phòng, xử lý mất kết nối và cạnh tranh đa luồng.

#### File kiểm thử có sẵn / bổ sung

| Đường dẫn | Tác dụng |
|---|---|
| `server/src/test/java/com/mcserver/service/room/RoomServiceTest.java` | Kiểm thử nghiệp vụ phòng, gồm trường hợp 200 luồng cùng tranh một slot cuối. |
| `client/src/main/java/com/mcclient/tools/RoomEdgeCaseTcpTest.java` | Chương trình kiểm thử TCP bổ sung để tạo hai client tranh slot cuối và kiểm tra một user tạo phòng khi đang ở trong phòng. |

`RoomEdgeCaseTcpTest.java` là công cụ kiểm thử bổ sung, không phải thành phần nghiệp vụ chạy trong ứng dụng chính.

#### Kết quả kiểm thử

| Trường hợp | Kết quả |
|---|---|
| Người thứ ba vào phòng đã đầy | Đạt – server từ chối `ROOM_FULL`. |
| Hai client cùng tranh slot cuối | Đạt – kiểm thử bằng chương trình TCP riêng; chỉ một client vào thành công. |
| Client bị tắt đột ngột | Đạt – server xử lý mất kết nối và cập nhật phòng. |
| Host rời phòng | Đạt – người còn lại trở thành host. |
| User tạo phòng khi đang ở phòng khác | Đạt – server từ chối `ALREADY_IN_ROOM`. |
| Server tắt khi client đang mở | Đạt – client phát hiện `DISCONNECTED`. |
| `mvn test` phía server | Đã chạy thành công. |

**Giới hạn hiện tại:** Client chưa tự động reconnect sau khi server khởi động lại. Đây là phần mở rộng, không phải yêu cầu bắt buộc của Ngày 6 theo hướng dẫn.

---

## 3. Giao thức TCP/JSON cần quan tâm khi tích hợp

### 3.1. Quy tắc truyền dữ liệu

- Giao thức: TCP Socket.
- Encoding: UTF-8.
- Mỗi message là một JSON object trên một dòng.
- Kết thúc message bằng ký tự xuống dòng `\n`.
- Client gửi request có `requestId`.
- Server phản hồi tương ứng bằng `requestId`.
- Sự kiện server chủ động đẩy không có `requestId`.
- Một kết nối TCP được duy trì để vừa gửi request vừa nhận push event.

### 3.2. Cấu trúc message

**Client → Server:**

```json
{
  "type": "CREATE_ROOM",
  "requestId": "req-1",
  "data": {
    "userId": 1,
    "username": "alice"
  }
}
```

**Server → Client – phản hồi thành công:**

```json
{
  "type": "ROOM_UPDATE",
  "requestId": "req-1",
  "success": true,
  "data": {
    "event": "ROOM_CREATED",
    "roomId": "R001",
    "version": 1,
    "rooms": []
  }
}
```

**Server → Client – sự kiện push:**

```json
{
  "type": "ROOM_UPDATE",
  "data": {
    "event": "PLAYER_JOINED",
    "roomId": "R001",
    "version": 2,
    "rooms": []
  }
}
```

Sự kiện push không có `requestId` và `success`. Client dựa vào đặc điểm này để phân biệt với phản hồi request.

### 3.3. Các message type

| Hướng | Type | Mục đích |
|---|---|---|
| Client → Server | `LIST_ROOMS` | Yêu cầu danh sách phòng hiện tại. |
| Client → Server | `CREATE_ROOM` | Tạo phòng mới. |
| Client → Server | `JOIN_ROOM` | Tham gia phòng, cần `data.roomId`. |
| Client → Server | `LEAVE_ROOM` | Rời phòng hiện tại. |
| Server → Client | `ROOM_UPDATE` | Phản hồi thành công hoặc push cập nhật trạng thái phòng. |
| Server → Client | `ROOM_ERROR` | Lỗi nghiệp vụ phòng. |
| Server → Client | `ERROR` | Lỗi giao thức hoặc lỗi xử lý chung. |

`LIST_ROOMS` là message type bổ sung để phục vụ giao diện sảnh chờ, cần được thống nhất trong bảng giao thức chung của nhóm.

### 3.4. Các trường dữ liệu quan trọng

| Field | Ý nghĩa |
|---|---|
| `type` | Loại yêu cầu hoặc phản hồi. |
| `requestId` | Định danh request để ghép với response. |
| `success` | Trạng thái thành công/thất bại của response. |
| `data` | Payload của message. |
| `event` | Loại sự kiện phòng. |
| `roomId` | Mã phòng dạng String. |
| `version` | Phiên bản snapshot, dùng loại bỏ dữ liệu cũ/trùng. |
| `rooms` | Danh sách đầy đủ phòng hiện tại. |
| `hostId`, `hostName` | ID và tên chủ phòng. |
| `playerCount`, `maxPlayers` | Số người hiện tại và giới hạn phòng. |
| `players` | Danh sách người chơi trong phòng. |
| `status` | Trạng thái phòng: `WAITING`, `FULL`, `PLAYING`. |

### 3.5. Các event phòng

| Event | Ý nghĩa |
|---|---|
| `SNAPSHOT` | Snapshot danh sách phòng khi tải dữ liệu. |
| `ROOM_CREATED` | Phòng mới được tạo. |
| `PLAYER_JOINED` | Có người tham gia phòng. |
| `PLAYER_LEFT` | Có người rời phòng. |
| `HOST_CHANGED` | Quyền chủ phòng được chuyển. |
| `ROOM_CLOSED` | Phòng bị xóa khi không còn người. |
| `ROOM_STARTED` | Phòng chuyển sang trạng thái đang chơi. |

### 3.6. Mã lỗi cần xử lý

| Mã lỗi | Ý nghĩa |
|---|---|
| `ALREADY_IN_ROOM` | Người chơi đã ở trong một phòng. |
| `ROOM_NOT_FOUND` | Phòng không tồn tại. |
| `ROOM_FULL` | Phòng đã đủ người. |
| `ROOM_IN_PROGRESS` | Phòng đang trong trận. |
| `NOT_IN_ROOM` | Người chơi không ở trong phòng. |
| `ROOM_NOT_READY` | Phòng chưa đủ điều kiện bắt đầu. |
| `UNAUTHENTICATED` | Chưa xác thực người chơi. |
| `INVALID_JSON` | JSON không hợp lệ. |
| `INVALID_REQUEST` | Cấu trúc request không hợp lệ. |
| `UNKNOWN_TYPE` | Message type không được hỗ trợ. |
| `INTERNAL` | Lỗi xử lý nội bộ server. |

---

## 4. Luồng hoạt động tổng thể

```text
                 CLIENT
    ┌─────────────────────────────┐
    │         LobbyFrame          │
    │       (Giao diện Swing)     │
    └──────────────┬──────────────┘
                   │ Callback
    ┌──────────────▼──────────────┐
    │       LobbyController       │
    │  Xử lý thao tác người dùng  │
    └──────────────┬──────────────┘
                   │ request(type, data)
    ┌──────────────▼──────────────┐
    │        NetworkClient        │
    │   TCP + JSON + requestId    │
    └──────────────┬──────────────┘
                   │ TCP : 5000
═══════════════════╪════════════════════════
                   │
                 SERVER
    ┌──────────────▼──────────────┐
    │          TcpServer          │
    │    Quản lý kết nối client   │
    └──────────────┬──────────────┘
                   │
    ┌──────────────▼──────────────┐
    │      MessageDispatcher      │
    │   Định tuyến theo type      │
    └──────────────┬──────────────┘
                   │
    ┌──────────────▼──────────────┐
    │      RoomMessageHandler     │
    │  Xử lý request phòng chơi   │
    └──────────────┬──────────────┘
                   │
    ┌──────────────▼──────────────┐
    │         RoomService         │
    │ Quản lý trạng thái phòng RAM│
    └──────────────┬──────────────┘
                   │ RoomUpdateEvent
    ┌──────────────▼──────────────┐
    │     LobbyRoomBroadcaster    │
    │       SessionRegistry       │
    └──────────────┬──────────────┘
                   │ ROOM_UPDATE
                   └──────────────► Các client
```

---

## 5. Những điểm cần thống nhất khi tích hợp với thành viên khác

### Với Người 1 – Tài khoản / đăng nhập

- Khi đăng nhập thành công, handler đăng nhập gọi `session.authenticate(userId, username)`.
- Sau khi tích hợp, xóa cơ chế `identify()` DEV trong `RoomMessageHandler`.
- Không tạo thêm socket riêng cho chức năng đăng nhập; dùng chung `NetworkClient`.
- Các thông tin `userId`, `username` phải lấy từ session đã xác thực, không tin dữ liệu client tự gửi.

### Với Người 3 – Gameplay

- Khi bắt đầu trận, handler `START_GAME` gọi `roomService.markPlaying(roomId)`.
- Server phát `ROOM_UPDATE` với event `ROOM_STARTED`.
- Cần thống nhất host có phải người duy nhất được phép bắt đầu trận hay không.
- Khi người chơi thoát giữa trận, cần thống nhất cách xử lý trạng thái phòng và kết quả trận.

### Với cả nhóm

- Thống nhất `LIST_ROOMS` và cấu trúc `ROOM_UPDATE` trong tài liệu giao thức chung.
- Chỉ sử dụng một `NetworkClient` cho các chức năng client.
- Các module mở rộng nên triển khai `TypeHandler`, tránh sửa trực tiếp `MessageDispatcher`.
- Khi thay đổi cấu trúc JSON, cần thông báo cho các thành viên đang sử dụng message tương ứng.

---

## 6. Trạng thái và việc còn tồn đọng

| Hạng mục | Trạng thái |
|---|---|
| RoomService và quản lý phòng | Hoàn thành, đã kiểm thử |
| Tầng TCP Server và broadcast | Đã tích hợp, đã chạy |
| NetworkClient và JSON | Đã tích hợp, đã chạy |
| LobbyFrame | Hoàn thành giao diện |
| LobbyController | Hoàn thành luồng phòng realtime |
| Kiểm thử trường hợp biên Ngày 6 | Đã thực hiện các trường hợp chính |
| Tự động reconnect khi server khởi động lại | Chưa triển khai |
| Tích hợp đăng nhập thật | Chờ Người 1 |
| Tích hợp bắt đầu trận/gameplay | Chờ Người 3 |

**Kết luận:** Phần Người 2 đã hoàn thành luồng quản lý phòng và sảnh chờ từ server đến client, bao gồm giao diện, giao thức TCP, đồng bộ realtime và kiểm thử các trường hợp biên chính trong phạm vi Ngày 1–6.
