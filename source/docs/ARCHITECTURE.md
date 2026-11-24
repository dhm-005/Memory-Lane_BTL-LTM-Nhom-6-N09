# Kiến trúc hệ thống Memory Duel

## 1. Tổng quan

Memory Duel là game tìm các cặp thẻ giống nhau, được xây dựng theo mô hình Client–Server. Người chơi sử dụng ứng dụng Client để đăng nhập, tham gia phòng, chơi game và xem các thông tin cá nhân. Server tiếp nhận yêu cầu từ Client, xử lý logic game và lưu trữ dữ liệu cần thiết vào database.

Hệ thống được chia thành hai chương trình chính là Client và Server. Hai chương trình hoạt động độc lập và trao đổi dữ liệu thông qua kết nối TCP với giao thức JSON.

## 2. Cấu trúc hệ thống

Project được tổ chức thành các thành phần chính:

- `client/`: Chương trình giao diện dành cho người chơi.
- `server/`: Chương trình xử lý nghiệp vụ và điều phối game.
- `database/`: Các file SQL phục vụ khởi tạo cấu trúc và dữ liệu database.
- `docs/`: Tài liệu mô tả kiến trúc và giao thức trao đổi dữ liệu.
- `run-client.bat`: Hỗ trợ khởi chạy Client.
- `run-server.bat`: Hỗ trợ khởi chạy Server.

Client và Server là hai project Maven riêng biệt, cùng sử dụng Java 17. Mỗi project có cấu hình build và các thư viện phụ thuộc riêng.

## 3. Kiến trúc Client

Client được xây dựng bằng Java Swing, đảm nhiệm việc hiển thị giao diện và tiếp nhận thao tác của người chơi.

Các thành phần chính gồm:

- **View:** Xây dựng các màn hình và component giao diện như đăng nhập, đăng ký, lobby, phòng chơi, Solo, PvP, bộ sưu tập, lịch sử và bảng xếp hạng.
- **Controller:** Tiếp nhận thao tác từ giao diện và điều phối các yêu cầu tương ứng.
- **Network:** Quản lý kết nối TCP đến Server, gửi yêu cầu và nhận thông điệp phản hồi.
- **Session:** Lưu trạng thái phiên người chơi, bao gồm thông tin tài khoản và phòng chơi.
- **ServerMessageHandler:** Phân loại và xử lý các thông điệp nhận được từ Server, sau đó cập nhật giao diện tương ứng.
- **UiTheme:** Quản lý màu sắc và các biểu tượng được sử dụng chung trong giao diện.

Các cập nhật giao diện Swing và việc hiển thị hộp thoại được thực hiện trên Event Dispatch Thread (EDT).

## 4. Kiến trúc Server

Server chịu trách nhiệm xử lý các yêu cầu từ Client, quản lý trạng thái người chơi và thực hiện logic của game.

Các thành phần chính gồm:

- **MainServer:** Điểm khởi chạy Server và thiết lập các thành phần cần thiết để hệ thống hoạt động.
- **Network:** Tiếp nhận kết nối TCP, quản lý phiên Client và chuyển thông điệp đến bộ xử lý tương ứng.
- **Service:** Xử lý nghiệp vụ của game, bao gồm tài khoản, phòng chơi, PvP, Solo, thẻ và lịch sử.
- **Repository:** Thực hiện các truy vấn database để lấy hoặc cập nhật dữ liệu.
- **Model:** Biểu diễn các đối tượng trong hệ thống như người chơi, phòng, trận đấu, thẻ và kết quả game.
- **Registry:** Lưu thông tin người chơi đang trực tuyến và các phòng hiện có trong bộ nhớ.
- **DatabaseConfig:** Cung cấp kết nối JDBC để các Repository truy cập MySQL.

Server là nơi kiểm tra và xử lý logic game. Client gửi yêu cầu và hiển thị kết quả nhận được, không tự quyết định các kết quả nghiệp vụ quan trọng của trận đấu.

## 5. Quản lý trận đấu

Hệ thống hỗ trợ hai chế độ chơi chính là Solo và PvP.

### 5.1. Solo

Ở chế độ Solo, người chơi thực hiện các màn chơi theo tiến trình. Server kiểm tra dữ liệu và điều kiện hoàn thành, sau đó xử lý kết quả, phần thưởng và lưu thông tin cần thiết.

`SoloService` đảm nhiệm phần nghiệp vụ Solo, bao gồm kiểm tra tiến trình, ghi nhận kết quả và cung cấp dữ liệu bảng xếp hạng.

### 5.2. PvP

Ở chế độ PvP, người chơi tham gia phòng và thi đấu với người chơi khác.

`RoomService` xử lý việc tạo phòng, tìm phòng và tham gia phòng. `Room` quản lý trạng thái phòng và đồng bộ các thao tác trong trận. Trạng thái bàn chơi và lượt hiện tại được lưu trong `Match`.

`GameService` tiếp nhận các lệnh PvP và điều phối quá trình xử lý trận đấu. Các thao tác thay đổi trạng thái trận phải được đồng bộ trên `Room` tương ứng để tránh xảy ra xung đột giữa các yêu cầu.

Sau khi trận đấu kết thúc, kết quả được xử lý và lưu vào database.

## 6. Database

Hệ thống sử dụng MySQL để lưu trữ dữ liệu cần duy trì giữa các lần chạy.

Các dữ liệu chính bao gồm:

- Thông tin tài khoản người chơi.
- Số dư coin và điểm xếp hạng.
- Dữ liệu thẻ.
- Kết quả trận đấu.
- Lịch sử chơi và dữ liệu phục vụ bảng xếp hạng.

Các thao tác truy cập database được thực hiện thông qua Repository. `DatabaseConfig` quản lý việc tạo kết nối JDBC, giúp các lớp nghiệp vụ không phải tự thiết lập kết nối database.

Thông tin kết nối database được cấu hình riêng trong file `db.properties` trên máy chạy Server.

## 7. Giao tiếp Client–Server

Client và Server giao tiếp thông qua TCP. Dữ liệu được truyền dưới dạng JSON, mỗi thông điệp kết thúc bằng ký tự xuống dòng.

Client gửi các yêu cầu như đăng nhập, tạo phòng, tham gia phòng hoặc thực hiện hành động trong game. Server tiếp nhận yêu cầu, kiểm tra và xử lý nghiệp vụ, sau đó gửi thông điệp phản hồi về Client.

Chi tiết cấu trúc thông điệp, các loại yêu cầu và phản hồi được mô tả trong `docs/PROTOCOL.md`.

## 8. Khởi chạy hệ thống

Để chạy game, cần chuẩn bị môi trường Java 17, Maven và MySQL.

Trước tiên, database cần được khởi tạo theo các file SQL trong thư mục `database/`. Server cần được cấu hình thông tin kết nối MySQL trong file `db.properties`.

Sau khi cấu hình hoàn tất, khởi chạy Server trước. Khi Server hoạt động, người chơi có thể mở Client để kết nối và sử dụng các chức năng của game.

Client và Server có thể được build và chạy độc lập thông qua cấu hình Maven tương ứng hoặc các file hỗ trợ khởi chạy trong project.
