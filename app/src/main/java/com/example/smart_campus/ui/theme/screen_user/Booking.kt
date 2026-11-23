package com.example.smart_campus.ui.theme.screen_user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_campus.ui.theme.*

enum class BookingStatusType(
    val label: String,
    val bgColor: Color,
    val textColor: Color
) {
    APPROVED("APPROVED", Color(0xFFDCFCE7), Color(0xFF15803D)),
    PENDING("PENDING", Color(0xFFFEF3C7), Color(0xFFB45309)),
    CANCELLED("CANCELLED", Color(0xFFF2F4F7), Color(0xFF475467)),
    REJECTED("REJECTED", Color(0xFFFEE4E2), Color(0xFFD92D20))
}

data class UserReservationUiModel(
    val id: String,
    val facilityName: String,
    val category: String,
    val date: String,
    val timeSlot: String,
    val duration: String,
    val status: BookingStatusType,
    val isUpcoming: Boolean
)

@Composable
fun MyBookingsScreen(
    userName: String = "Jennie",
    bottomPadding: Dp = 0.dp,
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onBrowseFacilitiesClick: () -> Unit = {},
    onViewBookingClick: (UserReservationUiModel) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val filterTabs = listOf("All", "Upcoming", "Past", "Pending", "Cancelled")
    var selectedFilter by remember { mutableStateOf("All") }
    var bookingToCancel by remember { mutableStateOf<UserReservationUiModel?>(null) }

    // Danh sách 6 lịch đặt đồng bộ từ thiết kế Desktop
    val reservations = remember {
        mutableStateListOf(
            UserReservationUiModel(
                id = "1",
                facilityName = "Seminar Room 102",
                category = "Seminar",
                date = "Oct 24, 2026",
                timeSlot = "14:30 – 16:45",
                duration = "2h 15m",
                status = BookingStatusType.APPROVED,
                isUpcoming = true
            ),
            UserReservationUiModel(
                id = "2",
                facilityName = "Engineering Lab A",
                category = "Lab",
                date = "Oct 26, 2026",
                timeSlot = "09:00 – 12:00",
                duration = "3h 00m",
                status = BookingStatusType.PENDING,
                isUpcoming = true
            ),
            UserReservationUiModel(
                id = "3",
                facilityName = "Indoor Basketball Court",
                category = "Sports",
                date = "Oct 28, 2026",
                timeSlot = "18:00 – 19:30",
                duration = "1h 30m",
                status = BookingStatusType.APPROVED,
                isUpcoming = true
            ),
            UserReservationUiModel(
                id = "4",
                facilityName = "Physics Lab 3",
                category = "Lab",
                date = "Oct 15, 2026",
                timeSlot = "11:00 – 13:00",
                duration = "2h 00m",
                status = BookingStatusType.CANCELLED,
                isUpcoming = false
            ),
            UserReservationUiModel(
                id = "5",
                facilityName = "Studio",
                category = "Arts",
                date = "Oct 10, 2026",
                timeSlot = "14:00 – 18:00",
                duration = "4h 00m",
                status = BookingStatusType.APPROVED,
                isUpcoming = false
            ),
            UserReservationUiModel(
                id = "6",
                facilityName = "Meeting Room",
                category = "Meeting",
                date = "Sep 28, 2026",
                timeSlot = "10:00 – 11:30",
                duration = "1h 30m",
                status = BookingStatusType.REJECTED,
                isUpcoming = false
            )
        )
    }

    val filteredReservations = reservations.filter { item ->
        val matchesSearch = searchQuery.isBlank() ||
                item.facilityName.contains(searchQuery.trim(), ignoreCase = true) ||
                item.category.contains(searchQuery.trim(), ignoreCase = true) ||
                item.date.contains(searchQuery.trim(), ignoreCase = true)

        val matchesTab = when (selectedFilter) {
            "Upcoming" -> item.isUpcoming && item.status != BookingStatusType.CANCELLED
            "Past" -> !item.isUpcoming
            "Pending" -> item.status == BookingStatusType.PENDING
            "Cancelled" -> item.status == BookingStatusType.CANCELLED || item.status == BookingStatusType.REJECTED
            else -> true
        }
        matchesSearch && matchesTab
    }

    // Hộp thoại xác nhận khi bấm Cancel
    bookingToCancel?.let { target ->
        AlertDialog(
            onDismissRequest = { bookingToCancel = null },
            containerColor = Color.White,
            title = {
                Text(
                    text = "Cancel Reservation?",
                    color = TextNavyDark,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to cancel your booking for ${target.facilityName} on ${target.date} (${target.timeSlot})?",
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val idx = reservations.indexOfFirst { it.id == target.id }
                        if (idx != -1) {
                            reservations[idx] = reservations[idx].copy(
                                status = BookingStatusType.CANCELLED,
                                isUpcoming = false
                            )
                        }
                        bookingToCancel = null
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD92D20))
                ) {
                    Text("Confirm Cancel", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { bookingToCancel = null }) {
                    Text("Keep Booking", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBgGray)
            .padding(bottom = bottomPadding)
    ) {
        // LỚP 1: Header Gradient bo cong 2 góc dưới (Cố định)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(225.dp)
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(TealGradientStart, TealGradientEnd)
                    )
                )
        )

        // LỚP 2: Phần trên cố định + Danh sách thẻ đặt phòng cuộn bên dưới
        Column(modifier = Modifier.fillMaxSize()) {
            // --- PHẦN 1: CỐ ĐỊNH (Top Bar + Thẻ tìm kiếm & filter chips + Thanh đếm số lượng) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(44.dp))

                // Hàng trên cùng: Nút Back + Pill User
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .height(34.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.22f))
                            .clickable { onProfileClick() }
                            .padding(start = 6.dp, end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccountCircle,
                            contentDescription = "User Avatar",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = userName,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Icon lịch & Tiêu đề chính giữa Header
                Icon(
                    imageVector = Icons.Rounded.EventAvailable,
                    contentDescription = "My Bookings Icon",
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "My Bookings",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // THẺ TÌM KIẾM & DẢI TAB TRẠNG THÁI (Đè lên đáy Header)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Ô tìm kiếm đặt chỗ
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = "Search my reservations...",
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = "Search",
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TealAccent,
                                unfocusedBorderColor = BorderGray,
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            )
                        )

                        // Hàng 5 chip lọc trạng thái cuộn ngang
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filterTabs.forEach { tab ->
                                val isSelected = selectedFilter == tab
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (isSelected) TealAccent else Color.White)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) TealAccent else BorderGray,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedFilter = tab }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = tab,
                                        color = if (isSelected) Color.White else TextNavyDark,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Thanh đếm số lượng đặt phòng
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredReservations.size} reservations found",
                        color = TextNavyDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (searchQuery.isNotBlank() || selectedFilter != "All") {
                        Text(
                            text = "Reset",
                            color = TealAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                searchQuery = ""
                                selectedFilter = "All"
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
            }

            // --- PHẦN 2: DANH SÁCH THẺ ĐẶT PHÒNG HOẶC EMPTY STATE ---
            if (filteredReservations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    EmptyBookingsCard(onBrowseClick = onBrowseFacilitiesClick)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(top = 6.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = filteredReservations,
                        key = { it.id }
                    ) { booking ->
                        UserReservationCard(
                            booking = booking,
                            modifier = Modifier.padding(horizontal = 20.dp),
                            onViewClick = { onViewBookingClick(booking) },
                            onCancelClick = { bookingToCancel = booking }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserReservationCard(
    booking: UserReservationUiModel,
    modifier: Modifier = Modifier,
    onViewClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    val canCancel = booking.status == BookingStatusType.APPROVED ||
            booking.status == BookingStatusType.PENDING

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onViewClick() },
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Hàng 1: Tên phòng + Badge loại phòng bên trái | Badge trạng thái bên phải
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.facilityName,
                        color = TextNavyDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE6F2F3))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = booking.category,
                            color = TealAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(booking.status.bgColor)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = booking.status.label,
                        color = booking.status.textColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hàng 2: Ngày đặt & Khung giờ + Thời lượng
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = "Date",
                        tint = TextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = booking.date,
                        color = Color(0xFF344054),
                        fontSize = 12.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = "Time",
                        tint = TextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${booking.timeSlot} (${booking.duration})",
                        color = Color(0xFF344054),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 1.dp, color = Color(0xFFF2F4F7))
            Spacer(modifier = Modifier.height(8.dp))

            // Hàng 3: Hành động View & Cancel đồng bộ với bảng Desktop
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View Details",
                    color = TealAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onViewClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                if (canCancel) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cancel",
                        color = Color(0xFFD92D20),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onCancelClick() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyBookingsCard(onBrowseClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF2F4F7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.EventBusy,
                    contentDescription = "No bookings",
                    tint = TextMuted,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "No bookings yet",
                color = TextNavyDark,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "You haven't made any facility reservations yet",
                color = TextMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onBrowseClick,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavyPrimary,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Browse Facilities",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 428, heightDp = 926)
@Composable
fun MyBookingsScreenPreview() {
    MaterialTheme {
        Scaffold(
            bottomBar = {
                UserBottomNavBar(selectedIndex = 2, onTabSelected = {})
            }
        ) { innerPadding ->
            MyBookingsScreen(bottomPadding = innerPadding.calculateBottomPadding())
        }
    }
}

