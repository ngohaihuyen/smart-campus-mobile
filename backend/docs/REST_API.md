# Thiết kế REST API Smart Campus Mobile

Bản bàn giao thiết kế ngày 22/11/2026 cho nhóm Backend và Android.
Phạm vi nghiệp vụ đã chốt; các lựa chọn giao tiếp tại mục 2 và 9 là đề xuất
chờ nhóm backend xác nhận. Đây là hợp đồng API dự kiến, chưa phải server đã triển khai.

## 1. Phạm vi và nguồn đối chiếu

Thiết kế gồm 12 API: 8 API người dùng trong proposal và 4 API Admin phục vụ
xét duyệt, thêm/sửa/ngừng hoạt động phòng như nhóm đã thống nhất.

| Tài liệu | Nội dung áp dụng |
| --- | --- |
| PROPOSAL_GP2526-Smart-Campus (1).pdf, mục 6 | 8 API Auth, Facilities, Bookings; chạy local |
| Smart_Campus_Ke_hoach_Backend_Android.docx | JWT, bcrypt, JSON thống nhất; phân quyền, transaction; tích hợp Retrofit |
| My_sql_schema_Smart_Campus_don_gian.docx | Bản chốt cột, trạng thái, lịch, giờ hoạt động và chính sách hủy |
| database/schema.sql trong repo Mobile | Tên cột, kiểu dữ liệu, PK/FK/CHECK và indexes thực tế |
| grouppjfacilitybooking.drawio, trang API Catalog | Tham khảo đường dẫn và quyền của các API Admin |

Quyết định mới của người dùng và schema đã chốt ưu tiên hơn nội dung cũ nhắc STAFF.
Backend mới chỉ dùng 3 bảng `users`, `facilities`, `bookings`.
Web cũ chỉ tham khảo cách chia nhóm, điều kiện overlap và tình huống kiểm thử;
không sao chép code backend web.

Không thêm STAFF, equipment, categories, HIDDEN, ưu tiên giảng viên giành lịch,
khung giờ cố định, đặt trước tối đa 30 ngày hoặc mục đích tối thiểu 10 ký tự.
Không bổ sung thanh toán, cloud, dashboard, quản lý khóa tài khoản,
refresh token hoặc Firebase trong bản thiết kế này.

## 2. Quy ước giao tiếp đề xuất

Các quy ước dưới đây giúp Android và backend có cùng cách hiểu; cần xác nhận
trước khi hai phía triển khai, không phải yêu cầu nghiệp vụ bổ sung.

- Base URL ví dụ trên máy backend: `http://localhost:3000/api`.
  Cổng 3000 là ví dụ, không phải cổng MySQL. Emulator dùng host `10.0.2.2`;
  điện thoại thật dùng IP mạng local của máy backend. API chưa chạy tại các URL này.
- JSON dùng camelCase; cột SQL giữ snake_case. `Content-Type: application/json`
  khi gửi body và `Accept: application/json` khi nhận dữ liệu.
- ID dùng chuỗi số nguyên dương trong JSON/path/query để bảo toàn BIGINT UNSIGNED.
  Ví dụ `"bookingId": "12"`; backend kiểm tra giá trị trong phạm vi kiểu SQL.
- Thời điểm gửi/nhận theo RFC 3339, có giây, nhận `Z` hoặc offset rõ ràng.
  Ví dụ `2030-01-15T09:00:00+07:00`; phản hồi chuẩn hóa `2030-01-15T02:00:00Z`.
  Không nhận chuỗi giờ thiếu múi giờ. Bản cơ bản không nhận phần lẻ giây.
- `date=2030-01-15` trong availability là ngày tại campus **Asia/Ho_Chi_Minh**.
  Backend đổi sang khoảng UTC tương ứng để truy vấn. Không lấy ngày UTC thay ngày campus.
- Database DATETIME lưu UTC; mỗi kết nối MySQL đặt timezone UTC. Không dùng timezone
  mặc định của máy chạy server để suy ra giờ campus.
- Các route ngoài register/login cần `Authorization: Bearer <accessToken>`.
  Token lỗi/hết hạn trả 401; token hợp lệ nhưng thiếu quyền trả 403.
  Backend xác minh chữ ký/hạn token và đọc user hiện tại từ Database.
- Danh sách dùng `page` mặc định 1, `limit` mặc định 20, tối đa 100.
  Giá trị không phải số nguyên dương hoặc vượt giới hạn trả 400, không tự cắt.
  Trả `{items, page, limit, total}`; không có dữ liệu trả `items: []`, HTTP 200.
- Body được kiểm tra theo danh sách trường cho phép. Trường lạ/sai kiểu trả 400;
  không cho client ghi userId, role, status hoặc metadata duyệt/hủy qua API tạo booking.
- Chuỗi tên, mục đích, lý do được trim; không rỗng nếu trường bắt buộc.
  Password giữ nguyên, không trim hay ghi vào log. Không trả passwordHash qua API.

### Khuôn phản hồi

Thành công:

```json
{
  "success": true,
  "message": "Gửi yêu cầu đặt phòng thành công",
  "data": {"bookingId": "12", "status": "PENDING"},
  "error": null
}
```

Đoạn trên minh họa khuôn JSON. Dữ liệu đầy đủ của mỗi API được quy định ở mục 3–4.

Thất bại:

```json
{
  "success": false,
  "message": "Khung giờ đã có yêu cầu giữ chỗ",
  "data": null,
  "error": {"code": "BOOKING_CONFLICT"}
}
```

`error.code` là mã ổn định để Android xử lý; `message` là lời giải thích.
Không trả câu SQL, stack trace, mật khẩu, JWT secret hoặc thông tin booking của người khác.
Phản hồi 401 có header `WWW-Authenticate: Bearer`.

## 3. Dữ liệu trả về và ánh xạ Database

Đây là các mẫu **data đầy đủ** được các API tại mục 4 sử dụng.
Mẫu có ngày cố định để đọc dễ; khi kiểm thử backend cần chọn thời gian ở tương lai.

### User

```json
{
  "userId": "1",
  "fullName": "Nguyễn An",
  "email": "student1@example.test",
  "role": "STUDENT",
  "createdAt": "2030-01-14T01:00:00Z"
}
```

Ánh xạ lần lượt `user_id`, `full_name`, `email`, `role`, `created_at`.
`password_hash` chỉ dùng trong backend. Public register luôn tạo STUDENT.
Teacher/Admin được cấp tài khoản bằng quy trình quản trị riêng; chưa thêm API cấp quyền.

### Facility

```json
{
  "facilityId": "1",
  "name": "Phòng tự học A101",
  "type": "STUDY_ROOM",
  "location": "Tầng 1, nhà A",
  "capacity": 30,
  "description": "Phòng học nhóm",
  "accessPolicy": "ALL",
  "operationalStatus": "ACTIVE",
  "unavailableReason": null,
  "unavailableNote": null,
  "createdAt": "2030-01-14T01:00:00Z",
  "updatedAt": "2030-01-14T01:00:00Z"
}
```

Ánh xạ camelCase tương ứng cột snake_case. `type` là chuỗi tối đa 50 ký tự,
không phải FK sang categories, không tự áp đặt enum mới.
ACTIVE là trạng thái vận hành, không có nghĩa là mọi khung giờ đều còn trống.

### Booking

```json
{
  "bookingId": "12",
  "userId": "1",
  "facilityId": "1",
  "startTime": "2030-01-15T02:00:00Z",
  "endTime": "2030-01-15T03:00:00Z",
  "reason": "Học nhóm",
  "status": "PENDING",
  "rejectionReason": null,
  "reviewedBy": null,
  "reviewedAt": null,
  "cancelledAt": null,
  "createdAt": "2030-01-14T01:00:00Z",
  "updatedAt": "2030-01-14T01:00:00Z"
}
```

Ánh xạ camelCase tương ứng cột snake_case. Không dùng `purpose`, `bookingDate`,
`adminNote` từ web cũ. `reason` và `rejectionReason` tối đa 500 ký tự.
GET lịch của tôi trả đầy đủ Booking, đủ dữ liệu cho màn hình chi tiết và lý do từ chối;
chưa cần thêm endpoint chi tiết booking ngoài catalogue.

## 4. Danh sách và chi tiết API

Path dưới đây đã có tiền tố `/api`. Mọi phản hồi thành công dùng khuôn mục 2.
Khi ghi `data = Booking/Facility/User`, toàn bộ trường của mẫu mục 3 được trả về.

| Mã | Method | Path | Người dùng |
| --- | --- | --- | --- |
| A01 | POST | /api/auth/register | Public |
| A02 | POST | /api/auth/login | Public |
| F01 | GET | /api/facilities | Đã đăng nhập |
| F02 | GET | /api/facilities/{facilityId} | Đã đăng nhập |
| B01 | GET | /api/bookings/availability | Đã đăng nhập |
| B02 | POST | /api/bookings | STUDENT, TEACHER |
| B03 | GET | /api/bookings/my | STUDENT, TEACHER |
| B04 | DELETE | /api/bookings/{bookingId} | STUDENT, TEACHER, chủ booking |
| AD01 | GET | /api/admin/bookings | ADMIN |
| AD02 | PATCH | /api/admin/bookings/{bookingId}/status | ADMIN |
| AD03 | POST | /api/admin/facilities | ADMIN |
| AD04 | PATCH | /api/admin/facilities/{facilityId} | ADMIN |

### A01 Đăng ký

`POST /api/auth/register`. Không cần JWT.

```json
{"fullName":"Nguyễn An","email":"student1@example.test","password":"CampusDemo123!"}
```

Trường bắt buộc: fullName (1–150 ký tự sau trim), email (đúng định dạng, tối đa 254),
password. Không nhận `role`. Backend hash bcrypt và INSERT với role STUDENT.
Email trùng kiểm tra bằng UNIQUE của Database kể cả hai request đăng ký đồng thời.
Giới hạn password và domain email còn cần xác nhận tại mục 9.

Phản hồi: **201**, `data = User`; không tự đăng nhập, không trả passwordHash.
Lỗi: **400 VALIDATION_ERROR** (bao gồm gửi role), **409 EMAIL_EXISTS**.

### A02 Đăng nhập

`POST /api/auth/login`. Không cần JWT.

```json
{"email":"student1@example.test","password":"CampusDemo123!"}
```

Backend tìm email, so sánh bcrypt; dùng cùng thông báo khi sai email hoặc mật khẩu.
Đăng nhập không xác nhận tài khoản email trường đã được trường cấp; quy trình
xác minh email và cấp tài khoản Teacher/Admin chưa được đặc tả.

Phản hồi: **200**, data có mẫu sau; `user` chứa toàn bộ User tại mục 3:

```json
{
  "accessToken":"JWT_ACCESS_TOKEN",
  "tokenType":"Bearer",
  "expiresIn":3600,
  "user":{
    "userId":"1","fullName":"Nguyễn An","email":"student1@example.test",
    "role":"STUDENT","createdAt":"2030-01-14T01:00:00Z"
  }
}
```

`expiresIn` tính bằng giây; 3600 chỉ là đề xuất tại mục 9.
Lỗi: **400 VALIDATION_ERROR**, **401 INVALID_CREDENTIALS**.
Đăng xuất trong bản cơ bản: Android xóa token đang lưu; không có endpoint revoke.
Token đã cấp vẫn có thể dùng đến khi hết hạn, không tuyên bố logout thu hồi token phía server.

### F01 Danh sách và lọc phòng

`GET /api/facilities?type=STUDY_ROOM&page=1&limit=20`.

Query tùy chọn: `type` khớp loại phòng; `operationalStatus` là ACTIVE hoặc UNAVAILABLE;
`page`, `limit` theo mục 2. Không thêm categoryId/equipment.
Mặc định trả cả ACTIVE và UNAVAILABLE để app hiển thị đúng trạng thái.
Sắp xếp tên tăng dần, facilityId làm tiêu chí phụ ổn định.

Phản hồi: **200**, `data = {items: Facility[], page, limit, total}`.
Ví dụ khi bộ lọc không có kết quả:

```json
{"items":[],"page":1,"limit":20,"total":0}
```

Lỗi: **400 VALIDATION_ERROR**, **401 UNAUTHORIZED**.
Backend vẫn kiểm tra accessPolicy khi đặt, không chỉ dựa vào bộ lọc của app.

### F02 Chi tiết phòng

`GET /api/facilities/1`. Không có body/query.

Phản hồi: **200**, `data = Facility`, ví dụ đầy đủ tại mục 3.
UNAVAILABLE vẫn trả chi tiết cùng lý do, không tự chuyển thành 404.
Lỗi: **400 VALIDATION_ERROR** (ID sai định dạng), **401 UNAUTHORIZED**,
**404 FACILITY_NOT_FOUND**.

### B01 Xem giờ trống

`GET /api/bookings/availability?facilityId=1&date=2030-01-15`.

Bắt buộc facilityId và ngày campus có thật dạng YYYY-MM-DD.
Phản hồi **200**, data ví dụ:

```json
{
  "facilityId":"1",
  "date":"2030-01-15",
  "timeZone":"Asia/Ho_Chi_Minh",
  "operationalStatus":"ACTIVE",
  "unavailableReason":null,
  "unavailableNote":null,
  "canBook":true,
  "openingTime":"07:00:00",
  "closingTime":"21:00:00",
  "maxDurationMinutes":120,
  "occupiedIntervals":[
    {"startTime":"2030-01-15T02:00:00Z","endTime":"2030-01-15T03:00:00Z"},
    {"startTime":"2030-01-15T03:00:00Z","endTime":"2030-01-15T05:00:00Z"}
  ],
  "freeIntervals":[
    {"startTime":"2030-01-15T00:00:00Z","endTime":"2030-01-15T02:00:00Z"},
    {"startTime":"2030-01-15T05:00:00Z","endTime":"2030-01-15T14:00:00Z"}
  ]
}
```

occupiedIntervals chỉ lấy PENDING/APPROVED; không lộ ID, user, mục đích hay
thông tin cá nhân của người giữ chỗ. Gộp các khoảng giao nhau/sát nhau khi tính phần bù.
freeIntervals là khoảng trống lịch trong giờ hoạt động, có thể dài hơn 120 phút;
mỗi lần đặt vẫn tối đa 120 phút, bắt đầu ở tương lai. Không chia slot cố định.

`canBook` chỉ biểu thị phòng ACTIVE và role đúng accessPolicy, không bảo đảm một
khoảng cụ thể còn trống hay hợp lệ về thời gian. ADMIN luôn canBook=false.
UNAVAILABLE: canBook=false, freeIntervals=[]; occupiedIntervals cũ vẫn giữ nguyên.
Người sai accessPolicy có thể xem lịch trống nhưng không được đặt.
Availability chỉ là ảnh chụp lúc đọc; POST booking phải kiểm tra lại trong transaction.

Lỗi: **400 VALIDATION_ERROR**, **401 UNAUTHORIZED**, **404 FACILITY_NOT_FOUND**.

### B02 Tạo booking

`POST /api/bookings`. Chỉ STUDENT/TEACHER.

```json
{
  "facilityId":"1",
  "startTime":"2030-01-15T09:00:00+07:00",
  "endTime":"2030-01-15T10:00:00+07:00",
  "reason":"Học nhóm"
}
```

Backend lấy userId từ user đã xác thực, không từ body. Bốn trường trên bắt buộc.
Áp dụng toàn bộ điều kiện mục 5–6. Luôn tạo PENDING; metadata duyệt/hủy là NULL.

Phản hồi: **201**, `data = Booking`; giờ được chuẩn hóa UTC như mẫu mục 3.
Lỗi: **400 VALIDATION_ERROR / INVALID_BOOKING_TIME**,
**401 UNAUTHORIZED**, **403 FORBIDDEN / FACILITY_ACCESS_DENIED**,
**404 FACILITY_NOT_FOUND**, **409 FACILITY_UNAVAILABLE / BOOKING_CONFLICT**.

### B03 Lịch của tôi

`GET /api/bookings/my?status=PENDING&page=1&limit=20`.

Backend lấy userId từ JWT/user hiện tại. Không nhận userId của người khác.
`status` tùy chọn, chỉ nhận 4 trạng thái đã chốt; không có thì trả toàn bộ lịch sử.
Sắp xếp createdAt giảm dần, bookingId giảm dần làm tiêu chí phụ.

Phản hồi: **200**, `data = {items: Booking[], page, limit, total}`.
Giữ đủ rejectionReason và metadata duyệt/hủy để Android xem chi tiết.
Danh sách rỗng có cùng mẫu F01.
Lỗi: **400 VALIDATION_ERROR**, **401 UNAUTHORIZED**, **403 FORBIDDEN**.

### B04 Hủy booking

`DELETE /api/bookings/12`. Không có body/query. Giữ method DELETE của proposal;
nghiệp vụ chỉ cập nhật CANCELLED, tuyệt đối không DELETE dòng trong MySQL.

Chỉ chủ booking được hủy. PENDING được hủy, kể cả đã qua giờ bắt đầu;
APPROVED chỉ được hủy trước giờ bắt đầu. Kiểm tra bằng giờ server hiện tại.
REJECTED không được hủy. Với CANCELLED của chính mình, trả lại kết quả hiện tại
HTTP 200, không thay cancelledAt lần nữa (đề xuất xử lý gọi lặp).

Cập nhật cancelledAt và updatedAt. Nếu từ APPROVED thì giữ reviewedBy/reviewedAt;
từ PENDING thì cả hai vẫn NULL. Không thêm cancellationReason ngoài schema.

Phản hồi: **200**, `data = Booking` có status CANCELLED và cancelledAt khác NULL.
Lỗi: **400 VALIDATION_ERROR**, **401 UNAUTHORIZED**, **403 NOT_BOOKING_OWNER**,
**404 BOOKING_NOT_FOUND**, **409 BOOKING_NOT_CANCELLABLE**.
Quyền sở hữu được kiểm tra trước khi trả dữ liệu booking, kể cả gọi lặp.

### AD01 Danh sách booking của Admin

`GET /api/admin/bookings?status=PENDING&page=1&limit=20`.

Chỉ ADMIN. `status`, `page`, `limit` tùy chọn, quy tắc như B03.
Mặc định tất cả trạng thái. Sắp xếp createdAt tăng dần, bookingId tăng dần để
xem yêu cầu cũ trước; không tự loại các PENDING đã quá giờ bắt đầu.

Phản hồi: **200**, data có items là toàn bộ Booking tại mục 3, cộng hai trường:
`userFullName` (từ users.full_name), `facilityName` (từ facilities.name).
Đây là dữ liệu JOIN, không thêm cột Database. Cấu trúc phân trang giống F01.
Lỗi: **400 VALIDATION_ERROR**, **401 UNAUTHORIZED**, **403 FORBIDDEN**.

### AD02 Duyệt hoặc từ chối

`PATCH /api/admin/bookings/12/status`. Chỉ ADMIN.

Duyệt:

```json
{"status":"APPROVED"}
```

Từ chối:

```json
{"status":"REJECTED","rejectionReason":"Phòng cần dùng cho kỳ thi"}
```

Chỉ nhận APPROVED/REJECTED, chỉ xử lý booking hiện đang PENDING.
REJECTED bắt buộc rejectionReason không rỗng, tối đa 500 ký tự;
APPROVED không nhận rejectionReason. Không nhận reviewer từ body.
Backend tự ghi reviewedBy là ADMIN hiện tại, reviewedAt/updatedAt là giờ server;
cancelledAt vẫn NULL. APPROVE khi đã đến/qua startTime trả 409.
REJECT PENDING quá giờ vẫn được phép để Admin xử lý thủ công.

Phản hồi: **200**, `data = Booking` với metadata tương ứng.
Lỗi: **400 VALIDATION_ERROR**, **401 UNAUTHORIZED**, **403 FORBIDDEN**,
**404 BOOKING_NOT_FOUND**, **409 BOOKING_ALREADY_PROCESSED / BOOKING_STARTED**.
Đề xuất nếu phòng hiện UNAVAILABLE thì chặn APPROVE bằng **409 FACILITY_UNAVAILABLE**;
giữ PENDING, Admin có thể từ chối. Điểm này cần xác nhận tại mục 9.

### AD03 Thêm phòng

`POST /api/admin/facilities`. Chỉ ADMIN.

```json
{
  "name":"Phòng tự học A104",
  "type":"STUDY_ROOM",
  "location":"Tầng 1, nhà A",
  "capacity":30,
  "description":"Phòng học nhóm",
  "accessPolicy":"ALL",
  "operationalStatus":"ACTIVE"
}
```

Bắt buộc name (1–150), type (1–50), location (1–255), capacity (số nguyên dương,
không vượt INT UNSIGNED), accessPolicy (ALL/TEACHER_ONLY).
description tùy chọn hoặc NULL, không vượt khả năng lưu TEXT của MySQL;
backend kiểm tra kích thước UTF-8 thay vì tự cắt nội dung.
operationalStatus mặc định ACTIVE nếu thiếu. unavailableReason/unavailableNote
tùy chọn, kiểm tra theo mục 5; note tối đa 255 ký tự.
Không nhận equipment, categoryId, imageUrl hoặc facilityId tự chọn.

Phản hồi: **201**, `data = Facility`.
Lỗi: **400 VALIDATION_ERROR**, **401 UNAUTHORIZED**, **403 FORBIDDEN**.
Không áp đặt tên phòng UNIQUE hoặc sức chứa tối đa 300 như web cũ.

### AD04 Sửa hoặc ngừng hoạt động phòng

`PATCH /api/admin/facilities/1`. Chỉ ADMIN.
Nhận ít nhất một trường của AD03, không nhận ID hoặc createdAt/updatedAt.
Trường thiếu được giữ nguyên; NULL chỉ được dùng cho description và metadata ngừng hoạt động.
Backend kiểm tra **trạng thái cuối cùng sau khi gộp** với dòng đang có.

Ví dụ ngừng phòng:

```json
{
  "operationalStatus":"UNAVAILABLE",
  "unavailableReason":"MAINTENANCE",
  "unavailableNote":"Bảo trì định kỳ"
}
```

Ví dụ mở lại:

```json
{"operationalStatus":"ACTIVE","unavailableReason":null,"unavailableNote":null}
```

Đề xuất khi chuyển từ ACTIVE sang UNAVAILABLE phải gửi rõ unavailableReason;
khi chuyển từ UNAVAILABLE sang ACTIVE phải gửi rõ hai trường NULL như ví dụ.
Mọi cập nhật kiểm tra trên dữ liệu cuối cùng và tuân thủ CHECK của schema.
Không tự hủy, từ chối hoặc sửa các booking cũ, kể cả khi đổi accessPolicy.
Đề xuất response có `data = {facility: Facility, affectedBookings: Booking[]}`:
affectedBookings chứa PENDING/APPROVED chưa kết thúc (`endTime > now`) khi chuyển
ACTIVE sang UNAVAILABLE để Admin rà soát; các lần sửa khác trả [].
Đây là JOIN phục vụ cảnh báo, không thêm bảng/cột, không phải danh sách đã bị hủy.

Phản hồi: **200** theo cấu trúc trên.
Lỗi: **400 VALIDATION_ERROR**, **401 UNAUTHORIZED**, **403 FORBIDDEN**,
**404 FACILITY_NOT_FOUND**. UPDATE không thay đổi giá trị vẫn trả 200 nếu phòng tồn tại.
Không thêm DELETE facility: nghiệp vụ ngừng hoạt động dùng PATCH này và giữ lịch sử.

## 5. Quy tắc nghiệp vụ đã chốt

| Chủ đề | Quy tắc |
| --- | --- |
| Role | STUDENT, TEACHER, ADMIN; ADMIN không mặc định được đặt |
| AccessPolicy | ALL cho STUDENT/TEACHER; TEACHER_ONLY chỉ TEACHER |
| Vận hành phòng | Chỉ ACTIVE/UNAVAILABLE; ACTIVE không mang reason/note |
| Lý do ngừng | UNAVAILABLE có CLEANING/MAINTENANCE/OTHER; OTHER có note không rỗng |
| Thời gian | Tương lai, start < end, cùng ngày campus, trong 07:00–21:00, tối đa 7200 giây |
| Biên thời gian | Đúng 07:00 và kết thúc đúng 21:00 hợp lệ; đúng 120 phút hợp lệ |
| Giữ chỗ | PENDING/APPROVED giữ; REJECTED/CANCELLED không giữ |
| PENDING | Không tự hết hạn; chỉ Admin xử lý hoặc chủ booking hủy |
| Duyệt | Chỉ PENDING; không APPROVE khi đã đến/qua giờ bắt đầu |
| Hủy | Chủ booking hủy PENDING; hủy APPROVED trước giờ bắt đầu |
| Lịch sử | Không xóa booking; giữ metadata duyệt khi hủy APPROVED |
| Ngừng phòng | Không nhận lịch mới; booking cũ giữ nguyên để Admin rà soát |

Ma trận chuyển trạng thái:

| Từ | Sang | Người thực hiện | Dữ liệu cập nhật |
| --- | --- | --- | --- |
| PENDING | APPROVED | ADMIN, trước giờ bắt đầu | reviewedBy, reviewedAt, updatedAt |
| PENDING | REJECTED | ADMIN | reviewedBy, reviewedAt, rejectionReason, updatedAt |
| PENDING | CANCELLED | Chủ booking | cancelledAt, updatedAt |
| APPROVED | CANCELLED | Chủ booking, trước giờ bắt đầu | cancelledAt, updatedAt; giữ metadata duyệt |

Không chuyển REJECTED/CANCELLED trở lại trạng thái giữ chỗ; không APPROVED → REJECTED.
Client không được điều khiển metadata. CHECK kiểm tra tính nhất quán của từng dòng;
backend kiểm tra người thao tác, thời điểm và trạng thái trước khi cập nhật.

## 6. Transaction và xử lý đồng thời dành cho backend mới

Khi tạo booking, dùng cùng một kết nối MySQL trong suốt transaction:

1. Kiểm tra định dạng đầu vào; BEGIN.
2. Khóa dòng facility bằng `SELECT ... WHERE facility_id = ? FOR UPDATE`.
3. Sau khi lấy khóa, kiểm tra lại ACTIVE, accessPolicy và thời gian hiện tại.
4. Đọc các booking PENDING/APPROVED có `start_time < newEnd AND end_time > newStart`.
   Dùng locking read `FOR UPDATE` để đọc dữ liệu hiện tại, tránh snapshot cũ.
5. Có xung đột: ROLLBACK, trả 409 BOOKING_CONFLICT. Không có: INSERT PENDING rồi COMMIT.
6. Chỉ trả 201 sau COMMIT. Có lỗi phải rollback và trả kết nối về pool.

Đây là mô tả yêu cầu triển khai, không phải code backend đã tồn tại.
Mọi luồng thay đổi lịch hoặc tạo trạng thái giữ chỗ phải theo cùng quy tắc khóa.
Không dùng UNIQUE(facility_id, start_time) thay cho kiểm tra overlap.

Để xử lý create/review/cancel/đổi trạng thái phòng nhất quán, đề xuất mọi luồng
ghi liên quan đều khóa **facility trước, booking sau**, đọc lại trạng thái rồi mới UPDATE.
Hủy và duyệt phải kiểm tra điều kiện và ghi metadata trong cùng transaction,
không SELECT ngoài transaction rồi UPDATE vô điều kiện. Nhờ đó cancel và approve
đồng thời không ghi đè kết quả của nhau.
Đổi trạng thái phòng và create booking cũng phải dùng chung khóa facility.
Backend xử lý deadlock/lock timeout, không trả thành công khi chưa COMMIT;
có thể trả 503 DATABASE_BUSY để client tải lại trạng thái, không tự tạo booking lặp.
Nếu client mất phản hồi sau khi POST, tải lại lịch của tôi trước khi gửi lại;
idempotency key chưa nằm trong bản API cơ bản này.

## 7. Bảng mã phản hồi đề xuất

| HTTP | error.code | Tình huống |
| --- | --- | --- |
| 200 | Không có lỗi | Đọc, sửa, hủy thành công |
| 201 | Không có lỗi | Tạo tài khoản, phòng, booking thành công |
| 400 | VALIDATION_ERROR | Thiếu trường, sai kiểu/ID/enum, trường ngoài danh sách |
| 400 | INVALID_BOOKING_TIME | Giờ quá khứ, ngoài giờ, khác ngày hoặc quá 120 phút |
| 401 | INVALID_CREDENTIALS | Sai thông tin đăng nhập |
| 401 | UNAUTHORIZED | Thiếu/sai/hết hạn JWT hoặc user không còn tồn tại |
| 403 | FORBIDDEN / FACILITY_ACCESS_DENIED / NOT_BOOKING_OWNER | Thiếu quyền hoặc không phải chủ booking |
| 404 | FACILITY_NOT_FOUND / BOOKING_NOT_FOUND | Không tìm thấy tài nguyên |
| 409 | EMAIL_EXISTS | Email đã được đăng ký |
| 409 | BOOKING_CONFLICT | Trùng PENDING/APPROVED |
| 409 | FACILITY_UNAVAILABLE | Phòng ngừng hoạt động |
| 409 | BOOKING_ALREADY_PROCESSED | Booking không còn PENDING khi Admin xử lý |
| 409 | BOOKING_STARTED | Admin duyệt khi đã đến giờ bắt đầu |
| 409 | BOOKING_NOT_CANCELLABLE | REJECTED hoặc APPROVED đã đến giờ bắt đầu |
| 503 | DATABASE_BUSY | Transaction không hoàn tất do tranh chấp khóa sau xử lý nội bộ |
| 500 | INTERNAL_ERROR | Lỗi không dự kiến, không lộ chi tiết nội bộ |

Mọi endpoint có thể gặp 500/503; các mục chi tiết chỉ liệt kê lỗi nghiệp vụ chính.
Giữ nguyên HTTP status có ý nghĩa, không trả HTTP 200 cho mọi lỗi.

## 8. Các tình huống kiểm thử khi backend được xây

Đây là checklist dự kiến, chưa phải các API đã chạy kiểm thử.
File REST_API.http kèm theo là request mẫu để gửi khi backend có sẵn.

| Mã | Tình huống | Kết quả mong đợi |
| --- | --- | --- |
| T01 | Register hợp lệ | 201; STUDENT; Database chứa hash bcrypt |
| T02 | Register gửi role ADMIN/TEACHER | 400; không tạo tài khoản đặc quyền |
| T03 | Hai request đăng ký cùng email | Một 201, một 409; chỉ một user |
| T04 | Login đúng/sai | 200 + JWT / 401; không trả hash |
| T05 | Thiếu/sai/hết hạn JWT | 401 ở route cần đăng nhập |
| T06 | STUDENT gọi API Admin hoặc ADMIN tạo booking | 403 |
| T07 | Danh sách phòng, lọc type, phân trang, danh sách rỗng | 200; đúng kết quả/total |
| T08 | ID sai hoặc phòng không tồn tại | 400 hoặc 404 theo nguyên nhân |
| T09 | Availability ngày campus 15/01/2030 | Chỉ lấy khoảng UTC tương ứng 14/01 17:00 đến 15/01 17:00 |
| T10 | Availability có PENDING/APPROVED/REJECTED/CANCELLED | Chỉ hai trạng thái đầu giữ chỗ; không lộ chủ booking |
| T11 | STUDENT đặt TEACHER_ONLY | 403 |
| T12 | Booking start bằng now/quá khứ, qua ngày, thiếu offset | 400 |
| T13 | Đúng 07:00, kết thúc 21:00, thời lượng 120 phút | Hợp lệ nếu còn phòng/đúng quyền |
| T14 | 06:59, 21:01, 120 phút 1 giây, start >= end | 400 |
| T15 | Hai lịch 09:00–10:00 và 10:00–12:00 | Đều có thể tạo nếu hợp lệ |
| T16 | Trùng toàn phần, bao chứa, giao đầu/cuối với lịch giữ chỗ | 409 |
| T17 | 10 request đồng thời cùng phòng/giờ còn trống | Một 201, chín 409; Database chỉ một booking mới |
| T18 | Hai request cùng giờ khác phòng | Có thể cùng thành công; không dùng khóa toàn hệ thống |
| T19 | PENDING đi qua giờ bắt đầu | Vẫn PENDING; user hủy được; Admin approve 409, reject được |
| T20 | Hủy APPROVED trước/đúng/sau giờ bắt đầu | Trước: 200, giữ metadata; đúng/sau: 409 |
| T21 | Hủy booking người khác; hủy lại CANCELLED của mình | 403; gọi lặp của mình 200, cancelledAt không đổi |
| T22 | Admin reject thiếu lý do; xử lý booking đã đóng | 400; 409 |
| T23 | Hai Admin review hoặc owner cancel cùng lúc | Chỉ chuyển trạng thái hợp lệ, metadata nhất quán |
| T24 | Chuyển phòng UNAVAILABLE trong lúc create | Kết quả tuần tự theo khóa; không INSERT sau khi đã kiểm tra thấy UNAVAILABLE |
| T25 | Đóng phòng có booking cũ | Booking không đổi; trả cảnh báo affectedBookings |
| T26 | OTHER thiếu note; ACTIVE có reason/note; capacity=0 | 400 |
| T27 | UNAVAILABLE được mở lại | Metadata NULL; chỉ nhận đặt mới nếu còn giờ trống |
| T28 | Lỗi trước COMMIT / lock timeout | Không lưu dở; trả lỗi, release connection |
| T29 | Chuỗi dài hơn cột, role/status lạ, client gửi reviewer | 400; không cắt dữ liệu hoặc nhận metadata trái phép |

## 9. Những điểm cần nhóm backend xác nhận

Không cần thay Database để quyết định các mục này.

| Điểm | Đề xuất trong bản này | Trạng thái |
| --- | --- | --- |
| Tên JSON và kiểu ID | camelCase, ID dạng chuỗi | Chờ xác nhận |
| Thời gian HTTP | RFC 3339 có timezone; phản hồi UTC Z, chính xác giây | Chờ xác nhận |
| Phân trang/sắp xếp | page/limit; mặc định 1/20, tối đa 100; thứ tự ở từng API | Chờ xác nhận |
| JWT | Access token 3600 giây; không refresh/revoke trong bản cơ bản | Chờ xác nhận |
| Password | Đề xuất tối thiểu 8 ký tự và tối đa 72 byte UTF-8 khi dùng bcrypt; không cắt ngầm | Chờ xác nhận |
| Email | Kiểm tra định dạng, trim/lowercase; domain trường và xác minh tài khoản cần xác nhận | Chưa áp dụng regex email sinh viên từ web |
| User seed | Địa chỉ example.test chỉ dùng local; nếu chốt domain trường cần đồng bộ dữ liệu thử | Chờ chính sách email |
| Cấp TEACHER/ADMIN | Cấp qua quy trình quản trị ngoài public register; không thêm API users tự phát | Chờ quy trình cấp tài khoản |
| APPROVE khi UNAVAILABLE | Chặn 409, giữ PENDING cho Admin từ chối | Chờ xác nhận cách xử lý booking cũ |
| PATCH phòng | Dữ liệu cuối cùng phải hợp lệ; gửi rõ metadata khi chuyển trạng thái | Chờ xác nhận hợp đồng request |
| Cảnh báo phòng | Trả affectedBookings chưa kết thúc khi ngừng hoạt động | Chờ xác nhận hợp đồng response |
| Hủy lặp | CANCELLED của chính mình trả 200, không sửa thời điểm | Chờ xác nhận |
| Method hủy | DELETE theo proposal, chỉ soft cancel trong Database | Giữ proposal; backend xác nhận triển khai |
| Ngừng phòng | PATCH facility thay cho DELETE; không thêm HIDDEN hoặc xóa lịch sử | Đề xuất ánh xạ thao tác, giữ scope |
| Dữ liệu API Admin | Booking cộng userFullName/facilityName từ JOIN | Chờ xác nhận |

Không tự thêm giới hạn 30 ngày đặt trước, mục đích tối thiểu 10 ký tự,
quy tắc email phải có mã sinh viên, hay sức chứa tối đa 300 từ web cũ.
Các quyết định nghiệp vụ tại mục 5 giữ nguyên trong mọi trường hợp.

## 10. Tham khảo kỹ thuật

- [MySQL locking reads](https://dev.mysql.com/doc/refman/8.0/en/innodb-locking-reads.html):
  khóa FOR UPDATE hoạt động trong transaction và được giải phóng khi commit/rollback.
- [RFC 3339](https://www.rfc-editor.org/rfc/rfc3339.html): định dạng thời điểm có offset.
- [RFC 9110 mục DELETE](https://www.rfc-editor.org/rfc/rfc9110.html#name-delete):
  method HTTP không bắt buộc phải xóa vật lý dữ liệu lưu trữ.
