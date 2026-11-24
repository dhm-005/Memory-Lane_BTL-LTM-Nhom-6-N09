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
**Chủ đề đã đăng ký:** (XÂY DỰNG TRÒ CHƠI LẬT THẺ GHI NHỚ ĐỐI KHÁNG TRỰC TUYẾN 1V1 SỬ DỤNG JAVA SWING, TCP SOCKET VÀ LẬP TRÌNH ĐA LUỒNG THEO MÔ HÌNH CLIENT–SERVER)

---

## 🧠 MÔ TẢ HỆ THỐNG

Memory Lane là game lật thẻ ghi nhớ được phát triển bằng Java. Dự án được xây dựng theo mô hình Client–Server, sử dụng giao diện Java Swing và cơ sở dữ liệu MySQL để lưu trữ thông tin người chơi, thẻ và kết quả chơi.

>  Hệ thống bao gồm **server** xử lý yêu cầu và **client** gửi yêu cầu đến server qua giao thức TCP/IP bằng TCPSocket.  

> **Client:** là ứng dụng desktop, sử dụng Java Swing.
> **Server:** gửi response dạng JSON Object cho những yêu cầu của client.

Ví dụ:
> Hệ thống bao gồm **server** xử lý yêu cầu và **client** gửi yêu cầu đến server qua giao thức HTTP.  
> Server cung cấp API cho phép tính toán đơn giản hoặc truyền dữ liệu.  
> Client có thể là chương trình CLI, web hoặc ứng dụng desktop.

**Cấu trúc logic tổng quát:**
```
client  <-->  server  <-->  database
```

**Sơ đồ hệ thống:**

![System Diagram](./statics/diagram.png)

---

## ⚙️ CÔNG NGHỆ SỬ DỤNG

> Liệt kê công nghệ, framework, thư viện chính mà nhóm sử dụng.

| Thành phần | Công nghệ | Ghi chú |
|------------|-----------|---------|
| Server | Python 3.11 + Flask | REST API |
| Client | Node.js 20 + Axios | Giao tiếp HTTP |
| Database | SQLite | Lưu trữ dữ liệu tạm thời |
| Triển khai | Docker | (nếu có) |

---

## 🚀 HƯỚNG DẪN CHẠY DỰ ÁN

### 1. Clone repository
```bash
git clone <repository-url>
cd assignment-network-project
```

### 2. Chạy server
```bash
cd source/server
# Các lệnh để khởi động server
```

### 3. Chạy client
```bash
cd source/client
# Các lệnh để khởi động client
```

### 4. Kiểm thử nhanh
```bash
# Các lệnh test
```

---

## 🔗 GIAO TIẾP (GIAO THỨC SỬ DỤNG)

| Endpoint | Protocol | Method | Input | Output |
|----------|----------|--------|-------|--------|
| `/health` | HTTP/1.1 | GET | — | `{"status": "ok"}` |
| `/compute` | HTTP/1.1 | POST | `{"task":"sum","payload":[1,2,3]}` | `{"result":6}` |

---

## 📊 KẾT QUẢ THỰC NGHIỆM

> Đưa ảnh chụp kết quả hoặc mô tả log chạy thử.

![Demo Result](./statics/result.png)

---

## 🧩 CẤU TRÚC DỰ ÁN
```
assignment-network-project/
├── README.md
├── INSTRUCTION.md
├── statics/
│   ├── diagram.png
│   └── dataset_sample.csv
└── source/
    ├── .gitignore
    ├── client/
    │   ├── README.md
    │   └── (client source files...)
    ├── server/
    │   ├── README.md
    │   └── (server source files...)
    │   └── (client source files...)
    ├── database/
    │   ├── schema.sql
    │   └── insert.sql
    └── docs/

```

---

## 🧩 HƯỚNG PHÁT TRIỂN THÊM

> Nêu ý tưởng mở rộng hoặc cải tiến hệ thống.

- [ ] Cải thiện giao diện người dùng
- [ ] Thêm tính năng xác thực và phân quyền
- [ ] Tối ưu hóa hiệu suất
- [ ] Triển khai trên cloud

---

## 📝 GHI CHÚ

- Repo tuân thủ đúng cấu trúc đã hướng dẫn trong `INSTRUCTION.md`.
- Đảm bảo test kỹ trước khi submit.

---

## 📚 TÀI LIỆU THAM KHẢO

> (Nếu có) Liệt kê các tài liệu, API docs, hoặc nguồn tham khảo đã sử dụng.