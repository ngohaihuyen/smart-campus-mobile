package com.example.smart_campus.model


import java.time.Instant

enum class Role {
    STUDENT, TEACHER, ADMIN, UNKNOWN;
    companion object {
        fun from(value: String?) = entries.firstOrNull { it.name == value } ?: UNKNOWN
    }
}

data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val role: Role,
    val createdAt: Instant
) {
    val canBook: Boolean get() = role == Role.STUDENT || role == Role.TEACHER
}