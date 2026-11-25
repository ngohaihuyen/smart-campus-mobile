package com.example.smart_campus.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val user: UserDto
)

@Serializable
data class UserDto(
    val userId: String,
    val fullName: String,
    val email: String,
    val role: String,               // STUDENT | TEACHER | ADMIN
    val createdAt: String
)

@Serializable
data class FacilityDto(
    val facilityId: String,
    val name: String,
    val type: String,
    val location: String,
    val capacity: Int,
    val description: String? = null,
    val accessPolicy: String,       // ALL | TEACHER_ONLY
    val operationalStatus: String,  // ACTIVE | UNAVAILABLE
    val unavailableReason: String? = null,  // CLEANING | MAINTENANCE | OTHER
    val unavailableNote: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class BookingDto(
    val bookingId: String,
    val userId: String,
    val facilityId: String,
    val facilityName: String? = null,   // có ở /bookings/my, POST, DELETE, notifications
    val startTime: String,
    val endTime: String,
    val reason: String,
    val status: String,             // PENDING | APPROVED | REJECTED | CANCELLED
    val rejectionReason: String? = null,
    val reviewedBy: String? = null,
    val reviewedAt: String? = null,
    val cancelledAt: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class CreateBookingRequest(
    val facilityId: String,
    val startTime: String,          // RFC 3339 có giây + offset, vd "2030-01-15T09:00:00+07:00"
    val endTime: String,
    val reason: String
)

@Serializable
data class TimeIntervalDto(
    val startTime: String,
    val endTime: String,
    val status: String? = null      // chỉ có ở occupiedIntervals
)

@Serializable
data class AvailabilityDto(
    val facilityId: String,
    val date: String,
    val timeZone: String,
    val operationalStatus: String,
    val unavailableReason: String? = null,
    val unavailableNote: String? = null,
    val canBook: Boolean,
    val openingTime: String,        // "07:00:00"
    val closingTime: String,        // "21:00:00"
    val maxDurationMinutes: Int,
    val occupiedIntervals: List<TimeIntervalDto>,
    val freeIntervals: List<TimeIntervalDto>
)

@Serializable
data class NotificationDto(
    val type: String,               // BOOKING_APPROVED | BOOKING_REJECTED
    val message: String,
    val createdAt: String,
    val booking: BookingDto
)