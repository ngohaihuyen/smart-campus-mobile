package com.example.smart_campus.ui.theme.screen_user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.example.smart_campus.ui.theme.*

// Data model mẫu cho danh sách lịch đặt phòng sắp tới
data class UpcomingBookingUiModel(
    val id: String,
    val badgeLetter: String,
    val facilityName: String,
    val dateTimeText: String,
    val status: String // "PENDING" hoặc "APPROVED"
)

@Composable
fun UserDashboardScreen(
    userName: String = "BimBim",
    bottomPadding: Dp = 0.dp,
    onProfileClick: () -> Unit = {},
    onBookFacilityClick: () -> Unit = {},
    onViewAllBookingsClick: () -> Unit = {},
    onBookingItemClick: (UpcomingBookingUiModel) -> Unit = {},
    onNavigateTab: (Int) -> Unit = {}
) {


    val sampleBookings = remember {
        listOf(
            UpcomingBookingUiModel(
                id = "1",
                badgeLetter = "A",
                facilityName = "Lecture Hall A21",
                dateTimeText = "Oct 24, 2026 • 14:30 – 16:45",
                status = "PENDING"
            ),
            UpcomingBookingUiModel(
                id = "2",
                badgeLetter = "S",
                facilityName = "Seminar Room 102",
                dateTimeText = "Oct 28, 2026 • 09:00 – 11:00",
                status = "APPROVED"
            )
        )
    }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBgGray)
                .padding(bottom = bottomPadding)
        ) {
            // LỚP 1: Nền Header Gradient bo cong 2 góc dưới
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

            // LỚP 2: Nội dung cuộn dọc đè lên Header
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(44.dp))

                // Thanh Top Bar: Logo App bên trái + Pill User bên phải
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cụm Logo Campus Facility Booking
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Apartment,
                                contentDescription = "Campus Logo",
                                tint = TealGradientStart,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "CAMPUS",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 13.sp
                            )
                            Text(
                                text = "FACILITY BOOKING",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 11.sp
                            )
                        }
                    }

                    // Pill User góc trên bên phải
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

                Spacer(modifier = Modifier.height(16.dp))

                // Tiêu đề Dashboard
                Text(
                    text = "Dashboard",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Book and manage your campus spaces",
                    color = Color(0xFFE6F2F3),
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // LƯỚI 2x2 THẺ THỐNG KÊ (Đè lên phần đáy bo cong của Header)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DashboardStatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.Event,
                            iconTint = TealAccent,
                            iconBg = Color(0xFFE6F2F3),
                            value = "2",
                            label = "Upcoming Bookings"
                        )
                        DashboardStatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.Schedule,
                            iconTint = Color(0xFFD97706),
                            iconBg = Color(0xFFFEF3C7),
                            value = "1",
                            label = "Pending Approval"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DashboardStatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.CalendarToday,
                            iconTint = TealAccent,
                            iconBg = Color(0xFFE6F2F3),
                            value = "12",
                            label = "Total Bookings"
                        )
                        DashboardStatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.Domain,
                            iconTint = Color(0xFF15803D),
                            iconBg = Color(0xFFDCFCE7),
                            value = "24",
                            label = "Available Spaces"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // THẺ KÊU GỌI ĐẶT PHÒNG NHANH (Quick Action CTA Card)
                QuickActionBookingCard(onBookClick = onBookFacilityClick)

                Spacer(modifier = Modifier.height(18.dp))

                // TIÊU ĐỀ DANH SÁCH LỊCH ĐẶT PHÒNG
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Upcoming Bookings",
                        color = TextNavyDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "View all",
                        color = TealAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { onViewAllBookingsClick() }
                            .padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // DANH SÁCH CÁC THẺ LỊCH HẸN SẮP TỚI
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    sampleBookings.forEach { booking ->
                        UpcomingBookingItemCard(
                            booking = booking,
                            onClick = { onBookingItemClick(booking) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }


@Composable
private fun DashboardStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    value: String,
    label: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                color = TextNavyDark,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 26.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = TextMuted,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuickActionBookingCard(onBookClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // Vẽ dải viền nhấn màu xanh cổ vịt dày 4dp dọc theo mép trái thẻ
                .drawBehind {
                    drawRect(
                        color = TealAccent,
                        size = Size(width = 4.dp.toPx(), height = size.height)
                    )
                }
                .padding(start = 16.dp, end = 14.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE6F2F3)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = "Quick Book",
                    tint = TealAccent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Need a study or event space?",
                    color = TextNavyDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Browse 24 rooms & labs available today",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onBookClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavyPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Book a Facility",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun UpcomingBookingItemCard(
    booking: UpcomingBookingUiModel,
    onClick: () -> Unit
) {
    val isApproved = booking.status.equals("APPROVED", ignoreCase = true)
    val badgeBg = if (isApproved) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
    val badgeTextColor = if (isApproved) Color(0xFF15803D) else Color(0xFFB45309)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ô chữ cái đầu tên phòng
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE6F2F3)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = booking.badgeLetter,
                    color = TealAccent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Tên phòng & Thời gian đặt
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = booking.facilityName,
                    color = TextNavyDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = booking.dateTimeText,
                    color = TextMuted,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Cụm Badge trạng thái + Mũi tên chi tiết bên phải
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(badgeBg)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = booking.status.uppercase(),
                        color = badgeTextColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = "Details",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun UserBottomNavBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val navItems = listOf(
        Pair("Home", Icons.Rounded.GridView),
        Pair("Facilities", Icons.Outlined.CalendarToday),
        Pair("Bookings", Icons.Outlined.Domain),
        Pair("Profile", Icons.Outlined.PersonOutline)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        HorizontalDivider(thickness = 1.dp, color = Color(0xFFEAECF0))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEachIndexed { index, item ->
                val isSelected = selectedIndex == index
                val contentColor = if (isSelected) TealAccent else TextMuted

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onTabSelected(index) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Vạch chỉ báo trên cùng cho Tab đang chọn
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
                            .background(if (isSelected) TealAccent else Color.Transparent)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Icon(
                        imageVector = item.second,
                        contentDescription = item.first,
                        tint = contentColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.first,
                        color = contentColor,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 428, heightDp = 926)
@Composable
fun UserDashboardScreenPreview() {
    MaterialTheme {
        UserDashboardScreen()
    }
}

