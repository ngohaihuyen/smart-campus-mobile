# Ghi chú Backend: trả lời các điểm cần chốt

Tài liệu API chung là [REST_API.md](REST_API.md), bản sao từ repo `smart-campus-mobile`
(nhánh `feature/api-design`). Backend trong repo này **đã triển khai đúng hợp đồng đó**.
File này trả lời các câu hỏi trong buổi họp và mục 9 của REST_API.md,
đồng thời ghi lại những chỗ backend bổ sung thêm.

## 1. Câu hỏi trong buổi họp

| Câu hỏi | Chốt |
| --- | --- |
| Endpoint chung để làm MVVM | 12 API trong REST_API.md mục 4. Repository Android gọi đúng các path đó. |
| Tài liệu API chung | `docs/REST_API.md`, giữ ở repo Mobile; backend chỉ sao chép lại. Sửa thì sửa ở repo Mobile trước. |
| Repo riêng | Backend và Mobile là hai repo riêng; schema SQL lấy từ repo Mobile. |
| Định dạng thời gian | Xem mục 2 bên dưới. |
| JWT hết hạn trả lỗi gì | Xem mục 3 bên dưới. |
| Chạy local, Wi-Fi nội bộ, mở cổng 3000 | Xem mục 4 bên dưới. |

## 2. Định dạng thời gian

- **Gửi lên** (`startTime`, `endTime`): RFC 3339, bắt buộc có giây và múi giờ, không có phần lẻ giây.
  - Ví dụ: `"2030-01-15T09:00:00+07:00"` hoặc `"2030-01-15T02:00:00Z"`.
  - Thiếu múi giờ (`"2030-01-15T09:00:00"`) thì backend trả **400 VALIDATION_ERROR**.
- **Nhận về**: luôn là UTC, đuôi `Z`, có giây. Ví dụ `"2030-01-15T02:00:00Z"` (tức 09:00 giờ Việt Nam).
- **Ngày** (`?date=` của availability): `YYYY-MM-DD`, tính theo **giờ Việt Nam** (Asia/Ho_Chi_Minh).
- **Giờ mở/đóng cửa** (`openingTime`, `closingTime`): `HH:mm:ss`, theo giờ Việt Nam.
- Database lưu UTC. Backend tự đặt `time_zone = '+00:00'` cho mỗi kết nối MySQL,
  nên không phụ thuộc múi giờ của laptop.

Phía Android (java.time, minSdk 26 trở lên hoặc bật desugaring):

```kotlin
val zone = ZoneId.of("Asia/Ho_Chi_Minh")
val apiFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")

// Nhận về -> hiển thị
val local = Instant.parse(booking.startTime).atZone(zone)
val text = local.format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"))   // "09:00 15/01/2030"

// Người dùng chọn ngày/giờ -> gửi lên
val start = LocalDateTime.of(date, time).atZone(zone).format(apiFormat)    // "2030-01-15T09:00:00+07:00"
```

> Lưu ý: **không** dùng `OffsetDateTime.toString()` để gửi lên. Hàm này bỏ phần giây khi giây = 0
> (`"2030-01-15T09:00+07:00"`), và backend sẽ từ chối vì thiếu giây. Hãy luôn format bằng `apiFormat` như trên.

## 3. JWT và xử lý token hết hạn

- Đăng nhập trả `{ accessToken, tokenType: "Bearer", expiresIn: 3600, user }`. `expiresIn` tính bằng giây,
  đổi được bằng `JWT_EXPIRES_IN_SECONDS` trong `.env`. Không có refresh token.
- Mọi API trừ login (và health) cần header `Authorization: Bearer <accessToken>`.
- Mọi lỗi token đều trả **HTTP 401**, `error.code = "UNAUTHORIZED"`, kèm header `WWW-Authenticate: Bearer`.
  Chỉ phần `message` khác nhau:

| Trường hợp | message |
| --- | --- |
| Không gửi token | `Bạn chưa đăng nhập` |
| Token hết hạn | `Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại` |
| Token sai, bị sửa, sai secret | `Phiên đăng nhập không hợp lệ, vui lòng đăng nhập lại` |
| User trong token không còn | `Tài khoản không còn tồn tại` |

Ví dụ phản hồi khi token hết hạn:

```json
{
  "success": false,
  "message": "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại",
  "data": null,
  "error": { "code": "UNAUTHORIZED" }
}
```

Android nên xử lý tập trung ở **một chỗ** (OkHttp Interceptor):

1. Gắn `Authorization: Bearer <token>` vào mọi request (trừ login).
2. Nếu response là 401 và **không phải** từ `/auth/login`, thì xóa token đã lưu, hiện `message`,
   rồi chuyển về màn hình Login. (401 ở `/auth/login` là `INVALID_CREDENTIALS`, tức sai mật khẩu.)
3. Có thể lưu thời điểm `now + expiresIn` để chủ động đăng xuất trước khi gọi API.
4. Khi mở app có sẵn token: gọi `GET /api/auth/me`. Nhận 200 thì vào thẳng app, nhận 401 thì về Login.
5. Đăng xuất = xóa token phía app (server không có API thu hồi token).

**403 khác 401**: token vẫn hợp lệ nhưng không đủ quyền (ví dụ STUDENT gọi API Admin). Không đăng xuất khi gặp 403.

## 4. Chạy local và kết nối qua Wi-Fi nội bộ

1. Backend và MySQL chạy trên laptop của người làm backend. Server lắng nghe `0.0.0.0:3000`,
   nên máy khác trong cùng mạng truy cập được.
2. Khi chạy `npm run dev`, terminal in sẵn các địa chỉ, ví dụ:
   ```
   This laptop:         http://localhost:3000/api
   Android emulator:    http://10.0.2.2:3000/api
   Phone on same Wi-Fi: http://192.168.1.242:3000/api
   ```
3. **Base URL trong Retrofit** (phải kết thúc bằng `/`):
   - Emulator chạy trên **cùng laptop** với backend: `http://10.0.2.2:3000/api/`
   - Điện thoại thật hoặc emulator ở laptop khác: `http://<IP-laptop-backend>:3000/api/`.
     IP này có thể đổi khi đổi Wi-Fi, nên để trong `BuildConfig`/`local.properties`, đừng hard-code.
4. **Mở cổng 3000 trên Windows Firewall** (chỉ cần một lần, PowerShell chạy bằng quyền Administrator):
   ```powershell
   New-NetFirewallRule -DisplayName "Smart Campus API 3000" -Direction Inbound -Protocol TCP -LocalPort 3000 -Action Allow -Profile Private
   ```
   Mạng Wi-Fi phải được đặt là **Private** (Settings → Network & internet → Wi-Fi → Network profile type → Private).
   Không cần mở cổng MySQL 3306 vì app không bao giờ kết nối thẳng tới MySQL.
5. **Android cho phép HTTP** (không có HTTPS khi chạy local). Trong `AndroidManifest.xml`:
   ```xml
   <uses-permission android:name="android.permission.INTERNET" />
   <application android:usesCleartextTraffic="true" ...>
   ```
6. Kiểm tra nhanh: mở trình duyệt **trên điện thoại** vào `http://<IP>:3000/api/health`.
   Thấy `"success": true` là kết nối được. Không thấy thì kiểm tra lại: cùng Wi-Fi chưa, firewall đã mở chưa,
   mạng trường/quán cà phê có chặn các thiết bị nhìn thấy nhau không (client isolation; dùng hotspot điện thoại để thay).

## 5. Các điểm ở mục 9 của REST_API.md: backend xác nhận

Backend nhận toàn bộ đề xuất, để Android không phải đổi gì:

| Điểm | Backend triển khai |
| --- | --- |
| Tên JSON và kiểu ID | camelCase; mọi ID là chuỗi (`"12"`). Gửi `facilityId` dạng số sẽ bị 400. |
| Thời gian HTTP | RFC 3339 có múi giờ; phản hồi UTC `Z`, chính xác tới giây |
| Phân trang | `page`/`limit` mặc định 1/20, tối đa 100; sai thì 400; trả `{items, page, limit, total}` |
| JWT | 3600 giây (cấu hình được), không refresh/revoke |
| Email | trim + lowercase, kiểm tra định dạng, tối đa 254; chưa giới hạn domain trường |
| APPROVE khi phòng UNAVAILABLE | 409 `FACILITY_UNAVAILABLE`, booking giữ PENDING để Admin từ chối |
| PATCH phòng | Kiểm tra trạng thái cuối sau khi gộp; mở lại phòng phải gửi `unavailableReason: null` (và note nếu đang có) |
| Cảnh báo khi ngừng phòng | `data = {facility, affectedBookings}`; booking cũ không bị đổi |
| Hủy lặp | CANCELLED của chính mình: 200, `cancelledAt` giữ nguyên |
| Method hủy | `DELETE /bookings/{id}`, chỉ đổi status, không xóa dòng |
| Ngừng phòng | Dùng PATCH `operationalStatus`. Thêm `DELETE /admin/facilities/{id}` theo API Catalog, nhưng chỉ là lối tắt ngừng phòng, không xóa dòng (mục 6) |
| Booking của Admin | Thêm `userFullName`, `facilityName` |

## 6. Backend khác REST_API.md

| Thay đổi | Lý do |
| --- | --- |
| **Bỏ A01 `POST /api/auth/register`** | Nhóm quyết định (ghi chú họp: "tất cả của tùng **trừ register**"). Tài khoản do nhà trường cấp; local dùng `seed.sql`. Gọi vào trả **404 NOT_FOUND** "Không có chức năng đăng ký. Tài khoản do nhà trường cấp". Android bỏ màn Đăng ký |

### Phần bổ sung thêm

Đều là phần **thêm vào**, không đổi trường nào đã có. Gson/Moshi bỏ qua trường lạ nên Android không bị ảnh hưởng.

| Bổ sung | Lý do |
| --- | --- |
| `GET /api/health` (public) | Kiểm tra kết nối từ điện thoại; trả `serverTime` (UTC) để so giờ máy |
| `GET /api/auth/me` (đã đăng nhập) | Kiểm tra token đã lưu khi mở app (mục 3.4) |
| `DELETE /api/admin/facilities/{id}` (ADMIN) | Có trong API Catalog ("ẩn hoặc ngừng sử dụng"). **Không xóa dòng**: đặt UNAVAILABLE + OTHER + note "Ngừng sử dụng"; trả `{facility, affectedBookings}` như AD04. Gọi lại khi đã UNAVAILABLE → 200, không đổi. Mở lại bằng AD04 |
| B03 `?date=YYYY-MM-DD` | API Catalog: "lọc status/date". Lấy booking **bắt đầu** trong ngày campus đó |
| AD01 `?date=` và `?facilityId=` | Admin lọc theo ngày/phòng khi duyệt |
| F01 `?sort=name\|type\|capacity_asc\|capacity_desc` | API Catalog: "filter/sort theo type". Mặc định `name` như REST_API.md; giá trị khác → 400 |
| B01 `occupiedIntervals[].status` | Workflow: màn "phòng và trạng thái (Đã đặt, Đợi duyệt, Chưa đặt)". APPROVED = Đã đặt, PENDING = Đợi duyệt, `freeIntervals` = Chưa đặt. Không lộ người đặt |
| 409 BOOKING_CONFLICT có `error.conflicts` | Workflow: "Hiển thị thời gian bị trùng". `message` ghi giờ trùng theo giờ VN, `error.conflicts = [{startTime, endTime, status}]` |
| `GET /api/notifications?since=` (STUDENT, TEACHER) | Workflow: "thông báo cho người dùng yêu cầu đã được phê duyệt / bị từ chối". Xem mục 7 |
| `ALLOWED_EMAIL_DOMAINS` trong `.env` | Workflow: "Có phải tài khoản của trường?". Xem mục 7 |
| AD01 `?userId=` | Workflow: "Xem lịch sử booking của tất cả người dùng", lọc theo từng người |
| B03, B02, B04 trả thêm `facilityName` | Màn "Lịch của tôi" hiển thị được tên phòng mà không phải gọi F02 cho từng dòng |
| AD02 trả thêm `userFullName`, `facilityName` | Giống AD01, để màn Admin cập nhật dòng vừa duyệt |
| `freeIntervals` bỏ phần thời gian đã qua | Với ngày hôm nay, chỉ trả khoảng còn đặt được (bắt đầu ở tương lai) |
| URL không tồn tại | 404 `NOT_FOUND` (nếu chưa gửi token thì nhận 401 trước) |

## 7. Đối chiếu với tài liệu nhóm (file draw.io: Workflow, System Architecture, ERD, API Catalog; ghi chú họp)

### Trang 01 - Workflow

| Bước trong Workflow | Backend |
| --- | --- |
| Đăng ký / Đăng nhập | Chỉ còn **đăng nhập** (A02). Nhóm đã **bỏ API đăng ký**: tài khoản do nhà trường cấp |
| Xác thực tài khoản: "Có phải tài khoản của trường?" → Không: từ chối đăng nhập, hiển thị lỗi | Đặt `ALLOWED_EMAIL_DOMAINS=<domain trường>` trong `.env`. Đăng nhập bằng email ngoài trường → **403 FORBIDDEN** "Chỉ chấp nhận tài khoản email của trường (@...)". Để trống = không giới hạn (dữ liệu mẫu dùng `example.test`). **Cần nhóm chốt domain trường** |
| Phân quyền → trang chủ người dùng / trang chủ admin | Login trả `user.role`; app rẽ theo `role === "ADMIN"` |
| Xem danh sách CSVC, tìm kiếm / lọc theo loại phòng | F01 `?type=`, `?sort=` |
| Xem chi tiết: Tên, Loại, Vị trí, Sức chứa, Mô tả, Trạng thái | F02 |
| Xem chi tiết: **Thiết bị** | Schema không có cột thiết bị (REST_API.md: "không thêm equipment"). Tạm ghi thiết bị trong `description`. Xem cuối mục này |
| Chọn phòng → màn "phòng và trạng thái (Đã đặt, Đợi duyệt, Chưa đặt)" | B01: `occupiedIntervals[].status` APPROVED/PENDING, `freeIntervals` |
| Chọn ngày + giờ bắt đầu + giờ kết thúc, kiểm tra thời gian trống | B01 để hiển thị; B02 kiểm tra lại trong transaction |
| "Có trùng thời gian?" → Có: không cho đặt, **hiển thị thời gian bị trùng**, chọn thời gian khác | B02 trả 409 `BOOKING_CONFLICT`, `message` ghi giờ trùng, `error.conflicts` liệt kê các khoảng trùng |
| Nhập lý do / mục đích: BẮT BUỘC → "Lý do là bắt buộc" | B02 trả 400 `VALIDATION_ERROR`, message "Lý do / mục đích là bắt buộc" |
| Gửi yêu cầu → "Thông báo gửi thành công, booking đang chờ Admin xử lý" | B02 trả 201, status PENDING, message "Gửi yêu cầu đặt phòng thành công, đang chờ Admin xử lý" |
| Admin: Xem yêu cầu → kiểm tra Người dùng, Phòng, Ngày, Giờ, Lý do, Trạng thái | AD01 (có `userFullName`, `facilityName`, giờ, `reason`, `status`) |
| Duyệt booking? Có → ĐÃ DUYỆT / Không → BỊ TỪ CHỐI / Không thao tác → giữ Chờ duyệt | AD02 APPROVED / REJECTED; không gọi gì thì booking giữ PENDING, không tự hết hạn |
| "Thông báo cho người dùng: yêu cầu đã được phê duyệt / đã bị từ chối" | `GET /api/notifications` (bên dưới) |
| Quản lý CSVC: Thêm, Sửa, Xóa/Ẩn, Thay đổi trạng thái | AD03, AD04, `DELETE /admin/facilities/{id}` (ẩn, không xóa dòng) |
| Quản lý booking: Xem, Duyệt/Từ chối, Theo dõi trạng thái | AD01, AD02 |
| Xem lịch sử booking của tất cả người dùng | AD01 mặc định mọi trạng thái; lọc `?userId=`, `?facilityId=`, `?date=`, `?status=` |
| Quy tắc: không trùng giờ; liên tiếp 11–12 và 12–14 được phép | Có |
| Quy tắc: vai trò quyết định được đặt loại phòng nào | `accessPolicy` từng phòng: ALL hoặc TEACHER_ONLY |
| Quy tắc: lưu thời gian tạo gồm giờ, phút, giây để xử lý gửi cùng lúc | `created_at` DATETIME tới giây; thêm khóa transaction nên 10 request cùng lúc chỉ 1 thành công (đã test) |
| Quy tắc: không làm Đề xuất phòng, Multi-space / Multiple capacity | Không có |

**Thông báo phê duyệt / từ chối.** `GET /api/notifications?since=<RFC3339>&page=&limit=` (STUDENT, TEACHER):

```json
{
  "items": [{
    "type": "BOOKING_REJECTED",
    "message": "Yêu cầu đặt Phòng tự học A101 lúc 13:00–14:00 15/01/2030 đã bị từ chối. Lý do: Phòng cần dùng cho kỳ thi",
    "createdAt": "2030-01-14T03:10:00Z",
    "booking": { "bookingId": "1", "status": "REJECTED", "facilityName": "Phòng tự học A101", "rejectionReason": "Phòng cần dùng cho kỳ thi" }
  }],
  "page": 1, "limit": 20, "total": 1
}
```

Không có Firebase/push (REST_API.md loại khỏi phạm vi), nên app **tự hỏi** server: khi mở app, khi kéo làm mới,
hoặc định kỳ bằng WorkManager. Mỗi item hiện thành một local notification. Lần sau gửi `since` = `createdAt` mới nhất đã nhận.
Dữ liệu lấy từ `bookings.reviewed_at`, không thêm bảng.

**Những chỗ Workflow khác REST_API.md, cần nhóm chốt:**
- **Nhân viên (Staff)**: Workflow ghi "Sinh viên / Nhân viên / Giảng viên"; REST_API.md và schema chỉ có STUDENT, TEACHER, ADMIN.
- **Thiết bị**: Workflow có trong màn chi tiết phòng; REST_API.md ghi "không thêm equipment". Nếu cần, thêm một cột `equipment` (TEXT) vào schema repo Mobile, backend thêm field trong 5 phút.

### Trang 02–04: System Architecture, ERD, API Catalog

### API Catalog: 13 dòng, đủ cả 13

| Catalog | Backend | Ghi chú |
| --- | --- | --- |
| POST /api/auth/register | **Đã bỏ** | Nhóm quyết định bỏ: tài khoản do nhà trường cấp. Gọi vào trả 404 "Không có chức năng đăng ký" |
| POST /api/auth/login | A02 | Trả JWT + user (có role) |
| GET /api/facilities | F01 | Lọc `type`, `operationalStatus`; sort `name/type/capacity_*`; phân trang |
| GET /api/facilities/:facilityId | F02 | Tên, loại, vị trí, sức chứa, mô tả, trạng thái |
| GET /api/bookings/availability | B01 | Khoảng đã giữ chỗ / còn trống theo phòng + ngày |
| POST /api/bookings | B02 | Đúng 4 trường `facilityId, startTime, endTime, reason`; kiểm tra role với phòng |
| GET /api/bookings/my | B03 | Lọc `status` và `date` |
| DELETE /api/bookings/:bookingId | B04 | Chỉ chủ booking, theo chính sách hủy (mục 5 REST_API.md) |
| GET /api/admin/bookings | AD01 | Toàn hệ thống; lọc `status`, `date`, `facilityId` |
| PATCH /api/admin/bookings/:bookingId/status | AD02 | `status` + `rejectionReason` khi từ chối |
| POST /api/admin/facilities | AD03 | |
| PATCH /api/admin/facilities/:facilityId | AD04 | Sửa thông tin / trạng thái |
| DELETE /api/admin/facilities/:facilityId | Có | Ẩn/ngừng sử dụng, giữ lịch sử (mục 6) |
| Status code 200/201/400/401/403/404/409/500 | Có | Thêm 503 `DATABASE_BUSY` theo REST_API.md |
| Header `Authorization: Bearer <JWT>` | Có | |

### System Architecture

| Thành phần | Backend |
| --- | --- |
| REST API Routes `/api/auth*`, `/api/facilities*`, `/api/bookings*` | `src/routes/index.js` (thêm `/api/admin/*`) |
| Middleware: JWT authentication | `src/middleware/auth.js` |
| Middleware: role-based authorization | `requireRole(...)` trong `auth.js` |
| Middleware: request validation | `src/utils/validate.js` (trường lạ, sai kiểu, sai enum → 400) |
| Middleware: error handling | `src/middleware/errorHandler.js` |
| Đăng nhập (bcrypt); đăng ký đã bỏ theo quyết định của nhóm | `auth.controller.js` |
| Phân quyền theo role & facility type | `accessPolicy` ALL / TEACHER_ONLY; ADMIN không đặt phòng |
| Kiểm tra thời gian hợp lệ | Tương lai, cùng ngày, 07:00–21:00, tối đa 120 phút |
| Chống overlap booking | Transaction + `SELECT ... FOR UPDATE` |
| Tạo / hủy / duyệt / từ chối booking | B02, B04, AD02 |
| Ghi nhận trạng thái & lịch sử | `reviewed_by`, `reviewed_at`, `cancelled_at`; không bao giờ xóa booking |
| Booking rules: liên tiếp 11:00–12:00 và 12:00–14:00 được phép | Có (biên chạm nhau không tính là trùng) |
| Booking rules: lý do bắt buộc | Có (`reason` không rỗng, tối đa 500 ký tự) |
| Booking rules: PENDING → APPROVED/REJECTED; hủy theo quy định | Có |
| Local development: backend + MySQL trên máy cá nhân, kết nối qua mạng local | Mục 4 |

### ERD

| ERD | Schema đã chốt (repo Mobile) |
| --- | --- |
| USERS: user_id, full_name, email UNIQUE, password_hash, role, created_at | Giống |
| FACILITIES: facility_id, name, type, location, capacity, description | Giống |
| FACILITIES đề xuất `allowed_role` | `access_policy` (ALL / TEACHER_ONLY) |
| FACILITIES đề xuất `is_active / status` | `operational_status` (ACTIVE / UNAVAILABLE) + lý do, ghi chú |
| BOOKINGS: booking_id, user_id FK, facility_id FK, start_time, end_time, status | Giống |
| BOOKINGS đề xuất reason NOT NULL, rejection_reason, created_at, updated_at | Có cả 4 cột; thêm reviewed_by, reviewed_at, cancelled_at |
| Status PENDING → APPROVED / REJECTED; CANCELLED | Giống |
| Overlap: existing.start < new.end AND existing.end > new.start; end = start không trùng | Đúng công thức này |

### Những chỗ tài liệu cũ và REST_API.md khác nhau: backend theo REST_API.md

| Tài liệu cũ | REST_API.md / schema đã chốt | Lý do theo REST_API.md |
| --- | --- | --- |
| Actors: Student, Teacher, **Staff** | Chỉ STUDENT, TEACHER, ADMIN | REST_API.md mục 1 ghi rõ "không thêm STAFF" |
| `allowed_role`, `is_active` | `access_policy`, `operational_status` | Tên cột trong schema đã chốt |
| ID kiểu BIGINT | BIGINT UNSIGNED, JSON trả chuỗi | Schema đã chốt + mục 2 |

Nếu nhóm muốn quay lại STAFF hoặc tên cột cũ thì phải sửa schema ở repo Mobile trước, rồi backend sửa theo.

## 8. Tài khoản mẫu (seed.sql)

Mật khẩu chung: `CampusDemo123!`

| Email | Role |
| --- | --- |
| student1@example.test | STUDENT |
| student2@example.test | STUDENT |
| teacher@example.test | TEACHER |
| admin@example.test | ADMIN |

Dữ liệu booking mẫu nằm ở ngày 15/01/2030, nên luôn ở tương lai khi test.
