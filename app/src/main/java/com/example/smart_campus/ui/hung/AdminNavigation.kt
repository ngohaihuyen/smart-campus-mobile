package com.example.smart_campus.ui.hung

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/**
 * CHI DIEU HUONG: file nay khong ve noi dung cua tung man hinh.
 * Moi Composable giao dien duoc dinh nghia trong file Admin*.kt tuong ung.
 * Nav hien thi duy nhat 1 screen tai 1 thoi diem qua when(page).
 * Trang thai nay chi la demo, chua goi API.
 */
enum class AdminTab { HOME, BOOKING, FACILITIES, PROFILE }
private enum class AdminPage { HOME, BOOKING, FACILITIES, PROFILE, BOOKING_DETAIL, ADD_FACILITY }

@Composable
fun HungAdminNavigation(
    modifier: Modifier = Modifier,
    startTab: AdminTab = AdminTab.HOME
) {
    var page by remember(startTab) {
        mutableStateOf(when (startTab) {
            AdminTab.HOME -> AdminPage.HOME
            AdminTab.BOOKING -> AdminPage.BOOKING
            AdminTab.FACILITIES -> AdminPage.FACILITIES
            AdminTab.PROFILE -> AdminPage.PROFILE
        })
    }
    var selectedBookingId by remember { mutableStateOf<String?>(null) }
    var editingFacilityId by remember { mutableStateOf<Int?>(null) }
    val bookings = remember { mutableStateListOf<AdminBooking>().apply { addAll(exampleBookings()) } }
    val facilities = remember { mutableStateListOf<AdminFacility>().apply { addAll(exampleFacilities()) } }

    fun approve(id: String) {
        val i = bookings.indexOfFirst { it.id == id && it.status == BookingStatus.PENDING }
        if (i >= 0) bookings[i] = bookings[i].copy(status = BookingStatus.APPROVED)
    }
    fun reject(id: String, reason: String) {
        val i = bookings.indexOfFirst { it.id == id && it.status == BookingStatus.PENDING }
        if (i >= 0 && reason.isNotBlank()) {
            bookings[i] = bookings[i].copy(status = BookingStatus.REJECTED, rejectionReason = reason)
        }
    }
    val mainTab = when (page) {
        AdminPage.HOME -> AdminTab.HOME
        AdminPage.BOOKING, AdminPage.BOOKING_DETAIL -> AdminTab.BOOKING
        AdminPage.FACILITIES, AdminPage.ADD_FACILITY -> AdminTab.FACILITIES
        AdminPage.PROFILE -> AdminTab.PROFILE
    }
    val isDetail = page == AdminPage.BOOKING_DETAIL || page == AdminPage.ADD_FACILITY
    BackHandler(enabled = isDetail) {
        page = if (page == AdminPage.BOOKING_DETAIL) AdminPage.BOOKING else AdminPage.FACILITIES
    }
    Scaffold(modifier = modifier, containerColor = AdminColors.Background,
        bottomBar = { AdminBottomBar(mainTab) { tab ->
            page = when (tab) {
                AdminTab.HOME -> AdminPage.HOME
                AdminTab.BOOKING -> AdminPage.BOOKING
                AdminTab.FACILITIES -> AdminPage.FACILITIES
                AdminTab.PROFILE -> AdminPage.PROFILE
            }
        } }
    ) { insets ->
        Box(Modifier.fillMaxSize().padding(insets)) {
            when (page) {
                AdminPage.HOME -> AdminHomeScreen(bookings, facilities.size,
                    onOpenBooking = { page = AdminPage.BOOKING },
                    onOpenFacilities = { page = AdminPage.FACILITIES })
                AdminPage.BOOKING -> AdminBookingScreen(bookings,
                    onOpenDetail = { selectedBookingId = it; page = AdminPage.BOOKING_DETAIL },
                    onApprove = { approve(it) }, onReject = { id, reason -> reject(id, reason) })
                AdminPage.FACILITIES -> AdminFacilitiesScreen(
                    facilities = facilities,
                    onAddFacility = { editingFacilityId = null; page = AdminPage.ADD_FACILITY },
                    onEditFacility = { editingFacilityId = it.id; page = AdminPage.ADD_FACILITY }
                )
                AdminPage.PROFILE -> AdminProfileScreen()
                AdminPage.BOOKING_DETAIL -> AdminBookingDetailScreen(
                    booking = bookings.firstOrNull { it.id == selectedBookingId },
                    onBack = { page = AdminPage.BOOKING },
                    onApprove = { approve(it); page = AdminPage.BOOKING },
                    onReject = { id, reason -> reject(id, reason); page = AdminPage.BOOKING }
                )
                AdminPage.ADD_FACILITY -> AdminAddFacilityScreen(
                    onBack = { page = AdminPage.FACILITIES },
                    initialFacility = facilities.firstOrNull { it.id == editingFacilityId },
                    onSave = { candidate ->
                        val existingIndex = facilities.indexOfFirst { it.id == candidate.id && candidate.id != 0 }
                        if (existingIndex >= 0) {
                            facilities[existingIndex] = candidate
                        } else {
                            val newId = (facilities.maxOfOrNull { it.id } ?: 0) + 1
                            facilities.add(candidate.copy(id = newId))
                        }
                        editingFacilityId = null
                        page = AdminPage.FACILITIES
                    }
                )
            }
        }
    }
}

