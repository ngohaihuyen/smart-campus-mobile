package com.example.smart_campus.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant

enum class BookingStatus {
    PENDING, APPROVED, REJECTED, CANCELLED;
    companion object {
        fun from(value: String?) = entries.firstOrNull { it.name == value } ?: PENDING
    }
}

data class Booking(
    val id: String,
    val userId: String,
    val facilityId: String,
    val facilityName: String?,
    val startTime: Instant,
    val endTime: Instant,
    val reason: String,
    val status: BookingStatus,
    val rejectionReason: String?,
    val reviewedAt: Instant?,
    val cancelledAt: Instant?,
    val createdAt: Instant
) {
    // B04: PENDING luôn hủy được, APPROVED chỉ trước giờ bắt đầu
    @RequiresApi(Build.VERSION_CODES.O)
    fun canCancel(now: Instant = Instant.now()): Boolean = when (status) {
        BookingStatus.PENDING -> true
        BookingStatus.APPROVED -> now.isBefore(startTime)
        else -> false
    }
}