# Smart Campus Backend

REST API (Node.js + Express + MySQL) cho ứng dụng Android đặt phòng Smart Campus.

- **Hợp đồng API**: [docs/REST_API.md](docs/REST_API.md), sao chép từ repo `smart-campus-mobile` (nhánh `feature/api-design`).
- **Ghi chú backend** (thời gian, JWT hết hạn, chạy qua Wi-Fi, các điểm đã chốt): [docs/BACKEND_NOTES.md](docs/BACKEND_NOTES.md)
- **Schema**: `database/schema.sql` và `database/seed.sql` lấy từ repo Mobile.

## Chạy nhanh

1. Cài Node.js LTS và MySQL 8 (MySQL Workbench).
2. Trong MySQL Workbench, mở và chạy `database/schema.sql`, sau đó `database/seed.sql` (biểu tượng tia sét).
   Hai file này **giống hệt** bản trong repo Mobile (nhánh `feature/database`), không sửa gì.
   Chúng chỉ chạy được trên máy **chưa có** database `smart_campus`. Muốn chạy lại từ đầu,
   chạy lệnh `DROP DATABASE smart_campus;` trước (mất hết dữ liệu trong database đó).
3. Trong thư mục này:

```bash
npm install
copy .env.example .env    # macOS/Linux: cp .env.example .env
# sửa DB_PASSWORD và JWT_SECRET trong .env
npm run dev               # tự khởi động lại khi sửa code
```

4. Terminal in ra địa chỉ cho laptop, emulator và điện thoại. Mở http://localhost:3000/api/health,
   thấy `"success": true` là chạy được.
5. Test API bằng `docs/REST_API.http` (VS Code REST Client / IntelliJ) hoặc import
   `SmartCampus.postman_collection.json` vào Postman. Các request Login tự lưu token.

**Không có API đăng ký** (nhóm đã bỏ `POST /api/auth/register`): tài khoản do nhà trường cấp,
khi chạy local thì lấy từ `database/seed.sql`. Tài khoản mẫu, mật khẩu đều là `CampusDemo123!`:

| Email | Role |
| --- | --- |
| student1@example.test | STUDENT |
| student2@example.test | STUDENT |
| teacher@example.test | TEACHER |
| admin@example.test | ADMIN |

## Endpoint

| Mã | Method | Path | Quyền |
| --- | --- | --- | --- |
| | GET | /api/health | Public |
| A02 | POST | /api/auth/login | Public |
| | GET | /api/auth/me | Đã đăng nhập |
| F01 | GET | /api/facilities?type=&operationalStatus=&sort=&page=&limit= | Đã đăng nhập |
| F02 | GET | /api/facilities/{facilityId} | Đã đăng nhập |
| B01 | GET | /api/bookings/availability?facilityId=&date= | Đã đăng nhập |
| B02 | POST | /api/bookings | STUDENT, TEACHER |
| B03 | GET | /api/bookings/my?status=&date=&page=&limit= | STUDENT, TEACHER |
| B04 | DELETE | /api/bookings/{bookingId} | Chủ booking |
| | GET | /api/notifications?since=&page=&limit= (thông báo duyệt/từ chối) | STUDENT, TEACHER |
| AD01 | GET | /api/admin/bookings?status=&date=&facilityId=&userId=&page=&limit= | ADMIN |
| AD02 | PATCH | /api/admin/bookings/{bookingId}/status | ADMIN |
| AD03 | POST | /api/admin/facilities | ADMIN |
| AD04 | PATCH | /api/admin/facilities/{facilityId} | ADMIN |
| | DELETE | /api/admin/facilities/{facilityId} (ngừng sử dụng, không xóa dòng) | ADMIN |

Bảng đối chiếu từng mục của file draw.io (Workflow, System Architecture, ERD, API Catalog) với code: [docs/BACKEND_NOTES.md mục 7](docs/BACKEND_NOTES.md).

Chỉ cho tài khoản email của trường: đặt `ALLOWED_EMAIL_DOMAINS=<domain trường>` trong `.env`.

## Cấu trúc

```
src/
  server.js                 # đọc .env, kiểm tra MySQL, mở cổng, in địa chỉ LAN
  app.js                    # cấu hình Express, gắn routes
  config/db.js              # connection pool MySQL (UTC) + helper transaction
  routes/index.js           # TẤT CẢ endpoint ở một chỗ
  middleware/auth.js        # kiểm tra JWT, đọc user từ DB, phân quyền theo role
  middleware/errorHandler.js
  controllers/              # xử lý nghiệp vụ + SQL
  utils/
    http.js                 # khuôn phản hồi {success, message, data, error}
    time.js                 # RFC 3339 <-> UTC, ngày campus (Asia/Ho_Chi_Minh)
    validate.js             # kiểm tra body/query/ID/phân trang
    mappers.js              # dòng MySQL (snake_case) -> JSON (camelCase)
database/
  schema.sql                # tạo bảng (từ repo Mobile)
  seed.sql                  # dữ liệu mẫu (từ repo Mobile)
docs/
  REST_API.md               # hợp đồng API (từ repo Mobile)
  REST_API.http             # request mẫu (từ repo Mobile)
  BACKEND_NOTES.md          # các quyết định phía backend
```
