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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_campus.ui.theme.*

// Trạng thái các khung giờ trong ngày trên Availability Timeline
enum class TimelineSlotStatus(val color: Color, val label: String) {
    AVAILABLE(Color(0xFF12B76A), "Available"),
    OCCUPIED(Color(0xFFF04438), "Occupied"),
    PENDING(Color(0xFFF79009), "Pending"),
    MY_SELECTION(Color(0xFF2E90FA), "My Selection")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FacilityDetailsScreen(
    facility: UserFacilityUiModel? = null,
    userName: String = "Bimbim",
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onConfirmBookingClick: (date: String, startTime: String, endTime: String, duration: String) -> Unit = { _, _, _, _ -> }
) {
    // Dữ liệu hiển thị (lấy từ facility được truyền sang hoặc dùng mẫu Lecture Hall A21)
    val facilityName = facility?.name ?: "Lecture Hall A21"
    val facilityLocation = facility?.location ?: "Building A21, Floor 8"
    val facilityCapacity = facility?.capacity ?: 300
    val facilityType = facility?.category ?: "Lecture Hall"
    val facilityDescription = facility?.description
        ?: "Lecture Hall A21 is one of the premier spaces, optimized for large lectures, keynotes, and hybrid classes"

    // State cho 3 tab thông tin
    val tabs = listOf("Description", "Equipment", "Rules")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val equipmentList = remember {
        listOf("Projector", "Whiteboard", "Wifi", "Microphone", "Podium")
    }

    // State cho ngày và khung giờ đặt phòng
    val dateOptions = listOf(
        "Thursday, October 24, 2026" to "Oct 24, 2026",
        "Friday, October 25, 2026" to "Oct 25, 2026",
        "Monday, October 28, 2026" to "Oct 28, 2026"
    )
    var selectedDateIndex by remember { mutableIntStateOf(0) }

    val startTimeOptions = listOf("08:00", "12:00", "14:30", "18:00")
    val endTimeOptions = listOf("10:00", "14:00", "16:45", "20:00")
    var startTimeIndex by remember { mutableIntStateOf(2) } // Mặc định 14:30
    var endTimeIndex by remember { mutableIntStateOf(2) }   // Mặc định 16:45

    val currentStartTime = startTimeOptions[startTimeIndex]
    val currentEndTime = endTimeOptions[endTimeIndex]
    val totalDurationText = remember(currentStartTime, currentEndTime) {
        calculateDurationLabel(currentStartTime, currentEndTime)
    }

    // Dữ liệu 6 đoạn trên thanh Availability Timeline (08:00 -> 22:00)
    val timelineSegments = remember {
        listOf(
            TimelineSlotStatus.AVAILABLE,    // 08:00 - 10:00
            TimelineSlotStatus.OCCUPIED,     // 10:00 - 12:00
            TimelineSlotStatus.AVAILABLE,    // 12:00 - 14:00
            TimelineSlotStatus.MY_SELECTION, // 14:00 - 16:00
            TimelineSlotStatus.PENDING,      // 16:00 - 18:00
            TimelineSlotStatus.AVAILABLE     // 18:00 - 22:00
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBgGray)
    ) {
        // LỚP 1: Header Gradient bo cong 2 góc dưới
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(185.dp)
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(TealGradientStart, TealGradientEnd)
                    )
                )
        )

        // LỚP 2: Chia màn hình thành 2 vùng: Vùng trên cố định + Vùng dưới chỉ cuộn Card 2
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // --- PHẦN 1: CỐ ĐỊNH (Top Bar + Card 1 Thông tin phòng) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(44.dp))

                // Thanh Top Bar: Back + Tiêu đề + Pill User
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

                    Text(
                        text = "Facilities Details",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

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

                // CARD 1: Ảnh bìa + Thông tin phòng + Tabs (Cố định, đè lên đáy Header)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(145.dp)
                                .background(Color(0xFFD6E6E8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Apartment,
                                contentDescription = facilityName,
                                tint = TealGradientStart.copy(alpha = 0.45f),
                                modifier = Modifier.size(50.dp)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = facilityName,
                                color = TextNavyDark,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.LocationOn,
                                    contentDescription = "Location",
                                    tint = TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = facilityLocation,
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                InfoPillBadge(text = "Capacity: $facilityCapacity people")
                                InfoPillBadge(text = "Type: $facilityType")
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Thanh 3 Tabs: Description | Equipment | Rules
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                    tabs.forEachIndexed { index, title ->
                                        val isSelected = selectedTabIndex == index
                                        Column(
                                            modifier = Modifier
                                                .width(IntrinsicSize.Max)
                                                .clickable { selectedTabIndex = index }
                                        ) {
                                            Text(
                                                text = title,
                                                color = if (isSelected) TealAccent else TextMuted,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(2.dp)
                                                    .background(if (isSelected) TealAccent else Color.Transparent)
                                            )
                                        }
                                    }
                                }
                                HorizontalDivider(thickness = 1.dp, color = Color(0xFFEAECF0))
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            when (selectedTabIndex) {
                                0 -> {
                                    Text(
                                        text = facilityDescription,
                                        color = TextMuted,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Available Equipment",
                                        color = TextNavyDark,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        equipmentList.forEach { item ->
                                            EquipmentTagChip(label = item)
                                        }
                                    }
                                }
                                1 -> {
                                    Text(
                                        text = "All equipment is pre-installed and tested prior to scheduled sessions:",
                                        color = TextMuted,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        equipmentList.forEach { item ->
                                            EquipmentTagChip(label = item)
                                        }
                                    }
                                }
                                2 -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        RuleBulletItem("Check in within 15 minutes of your reserved start time.")
                                        RuleBulletItem("No food or open beverages allowed near audio/projector consoles.")
                                        RuleBulletItem("Turn off lights, AC, and microphones before leaving.")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- PHẦN 2: CHỈ CUỘN CARD 2 (Book This Facility) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 24.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Book This Facility",
                            color = TextNavyDark,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // 1. Select Date
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Select Date",
                                color = Color(0xFF344054),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .border(1.dp, BorderGray, RoundedCornerShape(10.dp))
                                    .clickable {
                                        selectedDateIndex = (selectedDateIndex + 1) % dateOptions.size
                                    }
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarToday,
                                    contentDescription = "Select Date",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = dateOptions[selectedDateIndex].first,
                                    color = Color(0xFF101828),
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // 2. Start Time & End Time
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TimeSelectorField(
                                label = "Start Time",
                                timeText = currentStartTime,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    startTimeIndex = (startTimeIndex + 1) % startTimeOptions.size
                                }
                            )
                            TimeSelectorField(
                                label = "End Time",
                                timeText = currentEndTime,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    endTimeIndex = (endTimeIndex + 1) % endTimeOptions.size
                                }
                            )
                        }

                        // 3. Availability Timeline
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Availability Timeline",
                                    color = TextNavyDark,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "8:00 AM – 10:00 PM",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                timelineSegments.forEach { slot ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .background(slot.color)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf("08:00", "10:00", "12:00", "14:00", "16:00", "18:00", "20:00", "22:00")
                                    .forEach { tick ->
                                        Text(
                                            text = tick,
                                            color = TextMuted,
                                            fontSize = 9.sp
                                        )
                                    }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TimelineSlotStatus.entries.forEachIndexed { index, status ->
                                    if (index > 0) Spacer(modifier = Modifier.width(10.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(status.color)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = status.label,
                                            color = Color(0xFF475467),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }

                        // 4. Hộp Booking Summary
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(1.dp, Color(0xFFEAECF0), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Booking Summary",
                                color = TextNavyDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            SummaryDetailRow(
                                label = "Date:",
                                value = dateOptions[selectedDateIndex].second
                            )
                            SummaryDetailRow(
                                label = "Time slot:",
                                value = "$currentStartTime – $currentEndTime"
                            )
                            SummaryDetailRow(
                                label = "Total Duration:",
                                value = totalDurationText,
                                valueColor = TealAccent
                            )
                        }

                        // 5. Nút Confirm Booking
                        Button(
                            onClick = {
                                onConfirmBookingClick(
                                    dateOptions[selectedDateIndex].second,
                                    currentStartTime,
                                    currentEndTime,
                                    totalDurationText
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NavyPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Confirm Booking",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoPillBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color(0xFFE6F2F3))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = TealAccent,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EquipmentTagChip(label: String) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color(0xFFF2F4F7))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = Color(0xFF344054),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun RuleBulletItem(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = "•",
            color = TealAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 6.dp)
        )
        Text(
            text = text,
            color = TextMuted,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun TimeSelectorField(
    label: String,
    timeText: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            color = Color(0xFF344054),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, BorderGray, RoundedCornerShape(10.dp))
                .clickable { onClick() }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = label,
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = timeText,
                color = Color(0xFF101828),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SummaryDetailRow(
    label: String,
    value: String,
    valueColor: Color = TextNavyDark
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextMuted,
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun calculateDurationLabel(start: String, end: String): String {
    val startParts = start.split(":").mapNotNull { it.toIntOrNull() }
    val endParts = end.split(":").mapNotNull { it.toIntOrNull() }
    if (startParts.size < 2 || endParts.size < 2) return "2h15m"

    val startMinutes = startParts[0] * 60 + startParts[1]
    val endMinutes = endParts[0] * 60 + endParts[1]
    val diff = (endMinutes - startMinutes).coerceAtLeast(30)

    val hours = diff / 60
    val minutes = diff % 60
    return if (minutes == 0) "${hours}h" else "${hours}h${minutes}m"
}

@Preview(showBackground = true, widthDp = 428, heightDp = 1040)
@Composable
fun FacilityDetailsScreenPreview() {
    MaterialTheme {
        FacilityDetailsScreen()
    }
}