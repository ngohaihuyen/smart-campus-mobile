package com.example.smart_campus.model

import java.time.Instant

enum class AccessPolicy {
    ALL, TEACHER_ONLY;
    companion object {
        // nếu gtri lạ -> chọn phương án chặt nhất để không cho đặt nhầm
        fun from(value: String?) = entries.firstOrNull { it.name == value } ?: TEACHER_ONLY
    }
}

enum class OperationalStatus {
    ACTIVE, UNAVAILABLE;
    companion object {
        fun from(value: String?) = entries.firstOrNull { it.name == value } ?: UNAVAILABLE
    }
}

enum class UnavailableReason {
    CLEANING, MAINTENANCE, OTHER;
    companion object {
        fun from(value: String?) = entries.firstOrNull { it.name == value }
    }
}

data class Facility(
    val id: String,
    val name: String,
    val type: String,
    val location: String,
    val capacity: Int,
    val description: String?,
    val accessPolicy: AccessPolicy,
    val operationalStatus: OperationalStatus,
    val unavailableReason: UnavailableReason?,
    val unavailableNote: String?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val isActive: Boolean get() = operationalStatus == OperationalStatus.ACTIVE

    // Ccỉ để ẩn/hiện nút đặt; backend vx ktra lại khi POST
    fun isBookableBy(role: Role): Boolean = isActive && when (role) {
        Role.TEACHER -> true
        Role.STUDENT -> accessPolicy == AccessPolicy.ALL
        else -> false
    }
}