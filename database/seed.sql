USE smart_campus;
SET NAMES utf8mb4;
SET SESSION time_zone = '+00:00';
START TRANSACTION;

-- Thêm tài khoản mẫu để kiểm thử.
INSERT INTO users (user_id, full_name, email, password_hash, role) VALUES
    (1, 'Nguyễn An', 'student1@example.test', '$2a$10$pVka.CuwW.kBfbNUDi1lZu30Y8Etm4a8ZQE2bsWMCLxm9OV5ErFq.', 'STUDENT'),
    (2, 'Trần Bình', 'student2@example.test', '$2a$10$pVka.CuwW.kBfbNUDi1lZu30Y8Etm4a8ZQE2bsWMCLxm9OV5ErFq.', 'STUDENT'),
    (3, 'Lê Minh', 'teacher@example.test', '$2a$10$pVka.CuwW.kBfbNUDi1lZu30Y8Etm4a8ZQE2bsWMCLxm9OV5ErFq.', 'TEACHER'),
    (4, 'Admin Demo', 'admin@example.test', '$2a$10$pVka.CuwW.kBfbNUDi1lZu30Y8Etm4a8ZQE2bsWMCLxm9OV5ErFq.', 'ADMIN');

-- Thêm phòng mẫu với các trạng thái vận hành.
INSERT INTO facilities
    (facility_id, name, type, location, capacity, description, access_policy,
     operational_status, unavailable_reason, unavailable_note) VALUES
    (1, 'Phòng tự học A101', 'STUDY_ROOM', 'Tầng 1, nhà A', 30,
     'Phòng học nhóm', 'ALL', 'ACTIVE', NULL, NULL),
    (2, 'Hội trường B201', 'AUDITORIUM', 'Tầng 2, nhà B', 120,
     'Phòng tổ chức sự kiện và kỳ thi', 'TEACHER_ONLY', 'ACTIVE', NULL, NULL),
    (3, 'Phòng tự học A102', 'STUDY_ROOM', 'Tầng 1, nhà A', 20,
     NULL, 'ALL', 'UNAVAILABLE', 'CLEANING', 'Đang dọn phòng'),
    (4, 'Phòng thí nghiệm C301', 'LAB', 'Tầng 3, nhà C', 25,
     NULL, 'TEACHER_ONLY', 'UNAVAILABLE', 'MAINTENANCE', 'Bảo trì định kỳ'),
    (5, 'Phòng tự học A103', 'STUDY_ROOM', 'Tầng 1, nhà A', 15,
     NULL, 'ALL', 'UNAVAILABLE', 'OTHER', 'Tạm đóng để kiểm kê');

-- Thêm lịch mẫu để kiểm thử duyệt, từ chối và hủy.
INSERT INTO bookings
    (booking_id, user_id, facility_id, start_time, end_time, reason, status,
     rejection_reason, reviewed_by, reviewed_at, cancelled_at, created_at, updated_at)
VALUES
    (1, 1, 1, '2030-01-15 02:00:00', '2030-01-15 03:00:00', 'Học nhóm', 'PENDING',
     NULL, NULL, NULL, NULL, '2030-01-14 01:00:00', '2030-01-14 01:00:00'),
    (2, 2, 1, '2030-01-15 03:00:00', '2030-01-15 05:00:00', 'Ôn tập', 'APPROVED',
     NULL, 4, '2030-01-14 02:10:00', NULL, '2030-01-14 02:00:00', '2030-01-14 02:10:00'),
    (3, 2, 1, '2030-01-15 02:00:00', '2030-01-15 03:00:00', 'Thảo luận bài tập', 'REJECTED',
     'Trùng với yêu cầu đang giữ chỗ', 4, '2030-01-14 03:10:00', NULL,
     '2030-01-14 03:00:00', '2030-01-14 03:10:00'),
    (4, 1, 1, '2030-01-15 06:00:00', '2030-01-15 07:00:00', 'Học cá nhân', 'CANCELLED',
     NULL, NULL, NULL, '2030-01-14 04:10:00', '2030-01-14 04:00:00', '2030-01-14 04:10:00'),
    (5, 3, 2, '2030-01-15 07:00:00', '2030-01-15 09:00:00', 'Sinh hoạt chuyên đề', 'CANCELLED',
     NULL, 4, '2030-01-14 05:10:00', '2030-01-14 05:20:00',
     '2030-01-14 05:00:00', '2030-01-14 05:20:00');

COMMIT;

-- Kiểm tra số dòng đã nạp.
SELECT 'users' AS table_name, COUNT(*) AS row_count FROM users
UNION ALL SELECT 'facilities', COUNT(*) FROM facilities
UNION ALL SELECT 'bookings', COUNT(*) FROM bookings;
