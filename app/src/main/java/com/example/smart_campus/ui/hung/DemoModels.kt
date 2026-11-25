package com.example.smart_campus.ui.hung

/** Dữ liệu demo UI — không phải model/API thật của nhóm. */
enum class BookingStatus { PENDING, APPROVED, REJECTED, CANCELLED }
enum class AccessPolicy { ALL, TEACHER_ONLY }
enum class FacilityStatus { ACTIVE, UNAVAILABLE }

data class AdminBooking(
    val id: String,
    val requester: String,
    val email: String,
    val facility: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val purpose: String,
    val status: BookingStatus = BookingStatus.PENDING,
    val rejectionReason: String? = null,
    val hasScheduleConflict: Boolean = false
)

data class AdminFacility(
    val id: Int,
    val name: String,
    val type: String,
    val location: String,
    val capacity: Int,
    val description: String,
    val accessPolicy: AccessPolicy = AccessPolicy.ALL,
    val operationalStatus: FacilityStatus = FacilityStatus.ACTIVE,
    val unavailableReason: String? = null,
    val unavailableNote: String? = null
)

internal fun exampleBookings(): List<AdminBooking> = listOf(
    AdminBooking("BK-0148", "Nguyen Khac Hung", "student@example.edu.vn", "Study Room 801", "25 Nov 2026", "09:00", "10:00", "Group project discussion"),
    AdminBooking("BK-0149", "Nguyen Van A", "student2@example.edu.vn", "Meeting Room 506", "25 Nov 2026", "10:30", "11:00", "Team discussion"),
    AdminBooking("BK-0150", "Le Thi Mai", "student3@example.edu.vn", "Lecture Hall A21", "26 Nov 2026", "08:00", "09:30", "Study group"),
    AdminBooking("BK-0145", "Tran Thi B", "student4@example.edu.vn", "Computer Lab C201", "17 Nov 2026", "13:00", "14:00", "Course project", BookingStatus.APPROVED),
    AdminBooking("BK-0142", "Pham Van C", "student5@example.edu.vn", "Lecture Hall A21", "16 Nov 2026", "14:00", "15:30", "Group study", BookingStatus.REJECTED, "Room unavailable"),
    AdminBooking("BK-0138", "Nguyen Thi D", "student6@example.edu.vn", "Study Room 801", "15 Nov 2026", "16:00", "17:00", "Discussion", BookingStatus.CANCELLED)
)

internal fun exampleFacilities(): List<AdminFacility> = listOf(
    AdminFacility(1, "Lecture Hall A21", "Lecture Hall", "Building A, Floor 2", 120, "Large lecture hall with flexible seating", AccessPolicy.ALL),
    AdminFacility(2, "Study Room 801", "Study Room", "Building B, Floor 8", 20, "Small group discussion room", AccessPolicy.ALL),
    AdminFacility(3, "Meeting Room 506", "Meeting Room", "Building B, Floor 5", 24, "Room for meetings and presentations", AccessPolicy.TEACHER_ONLY),
    AdminFacility(4, "Computer Lab C201", "Computer Lab", "Building C, Floor 2", 40, "Computer practice room", AccessPolicy.TEACHER_ONLY),
    AdminFacility(5, "Lecture Hall A22", "Lecture Hall", "Building A, Floor 2", 90, "Temporarily under maintenance", AccessPolicy.ALL, FacilityStatus.UNAVAILABLE, "MAINTENANCE")
)
