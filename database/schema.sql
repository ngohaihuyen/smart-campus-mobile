SET NAMES utf8mb4;
SET SESSION time_zone = '+00:00';

-- Tạo database Smart Campus.
CREATE DATABASE smart_campus
    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE smart_campus;

-- Lưu tài khoản và vai trò người dùng.
CREATE TABLE users (
    user_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) COLLATE utf8mb4_0900_as_cs NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('STUDENT', 'TEACHER', 'ADMIN'))
) ENGINE = InnoDB;

-- Lưu thông tin và trạng thái vận hành phòng.
CREATE TABLE facilities (
    facility_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    type VARCHAR(50) NOT NULL,
    location VARCHAR(255) NOT NULL,
    capacity INT UNSIGNED NOT NULL,
    description TEXT NULL,
    access_policy VARCHAR(20) COLLATE utf8mb4_0900_as_cs NOT NULL,
    operational_status VARCHAR(20) COLLATE utf8mb4_0900_as_cs NOT NULL DEFAULT 'ACTIVE',
    unavailable_reason VARCHAR(20) COLLATE utf8mb4_0900_as_cs NULL,
    unavailable_note VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (facility_id),
    CONSTRAINT chk_facilities_capacity CHECK (capacity > 0),
    CONSTRAINT chk_facilities_access CHECK (access_policy IN ('ALL', 'TEACHER_ONLY')),
    CONSTRAINT chk_facilities_status CHECK (operational_status IN ('ACTIVE', 'UNAVAILABLE')),
    CONSTRAINT chk_facilities_reason CHECK (
        unavailable_reason IS NULL
        OR unavailable_reason IN ('CLEANING', 'MAINTENANCE', 'OTHER')
    ),
    CONSTRAINT chk_facilities_availability CHECK (
        (operational_status = 'ACTIVE'
            AND unavailable_reason IS NULL AND unavailable_note IS NULL)
        OR
        (operational_status = 'UNAVAILABLE'
            AND unavailable_reason IS NOT NULL
            AND (unavailable_reason <> 'OTHER'
                OR (unavailable_note IS NOT NULL
                    AND CHAR_LENGTH(TRIM(unavailable_note)) > 0)))
    ),
    INDEX idx_facilities_status_type (operational_status, type)
) ENGINE = InnoDB;

-- Lưu lịch đặt, thông tin xét duyệt và hủy.
CREATE TABLE bookings (
    booking_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    facility_id BIGINT UNSIGNED NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(20) COLLATE utf8mb4_0900_as_cs NOT NULL DEFAULT 'PENDING',
    rejection_reason VARCHAR(500) NULL,
    reviewed_by BIGINT UNSIGNED NULL,
    reviewed_at DATETIME NULL,
    cancelled_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (booking_id),
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id)
        REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_bookings_facility FOREIGN KEY (facility_id)
        REFERENCES facilities (facility_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_bookings_reviewer FOREIGN KEY (reviewed_by)
        REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_bookings_time CHECK (start_time < end_time),
    CONSTRAINT chk_bookings_reason CHECK (CHAR_LENGTH(TRIM(reason)) > 0),
    CONSTRAINT chk_bookings_status CHECK (
        status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')
    ),
    CONSTRAINT chk_bookings_state_data CHECK (
        (status = 'PENDING'
            AND reviewed_by IS NULL AND reviewed_at IS NULL
            AND rejection_reason IS NULL AND cancelled_at IS NULL)
        OR
        (status = 'APPROVED'
            AND reviewed_by IS NOT NULL AND reviewed_at IS NOT NULL
            AND rejection_reason IS NULL AND cancelled_at IS NULL)
        OR
        (status = 'REJECTED'
            AND reviewed_by IS NOT NULL AND reviewed_at IS NOT NULL
            AND rejection_reason IS NOT NULL
            AND CHAR_LENGTH(TRIM(rejection_reason)) > 0
            AND cancelled_at IS NULL)
        OR
        (status = 'CANCELLED'
            AND cancelled_at IS NOT NULL AND rejection_reason IS NULL
            AND ((reviewed_by IS NULL AND reviewed_at IS NULL)
                OR (reviewed_by IS NOT NULL AND reviewed_at IS NOT NULL)))
    ),
    INDEX idx_bookings_user_created (user_id, created_at),
    INDEX idx_bookings_facility_status_start (facility_id, status, start_time),
    INDEX idx_bookings_status_created (status, created_at),
    INDEX idx_bookings_reviewer (reviewed_by)
) ENGINE = InnoDB;
