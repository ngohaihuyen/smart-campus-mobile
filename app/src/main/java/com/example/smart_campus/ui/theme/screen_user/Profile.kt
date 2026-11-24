package com.example.smart_campus.ui.theme.screen_user

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_campus.ui.theme.*

@Composable
fun UserProfileScreen(
    userName: String = "BimBim",
    userEmail: String = "thangdzai@usth.edu.vn",
    studentId: String = "USTH-24089",
    department: String = "Computer Science & ICT",
    phoneNumber: String = "+84 912 345 678",
    bottomPadding: Dp = 0.dp,
    onBackClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    var bookingRemindersEnabled by remember { mutableStateOf(true) }
    var showRulesDialog by remember { mutableStateOf(false) }

    // Hộp thoại quy định đặt phòng của trường
    if (showRulesDialog) {
        AlertDialog(
            onDismissRequest = { showRulesDialog = false },
            containerColor = Color.White,
            title = {
                Text(
                    text = "Campus Booking Rules",
                    color = TextNavyDark,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. Reservations must be made at least 2 hours in advance.", color = TextMuted, fontSize = 12.sp)
                    Text("2. Cancel at least 1 hour prior if your schedule changes.", color = TextMuted, fontSize = 12.sp)
                    Text("3. Keep classrooms and laboratories clean after use.", color = TextMuted, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showRulesDialog = false },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = TealAccent)
                ) {
                    Text("Got It", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
        // LỚP 1: Nền Header Gradient bo cong 2 góc dưới
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp)
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(TealGradientStart, TealGradientEnd)
                    )
                )
        )

        // LỚP 2: Card mô tả
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // Top Bar: Nút Back bên trái + Tiêu đề chính giữa
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "My Profile",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // CARD 1: Thẻ hồ sơ chính đè lên mép dưới Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Ảnh đại diện tròn kèm huy hiệu camera nhỏ
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE6F2F3))
                                .border(2.5.dp, TealAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = "Avatar",
                                tint = TealGradientStart,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(NavyPrimary)
                                .border(1.5.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CameraAlt,
                                contentDescription = "Edit Avatar",
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = userName,
                        color = TextNavyDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = userEmail,
                        color = TextMuted,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Badge vai trò sinh viên USTH
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFE6F2F3))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "USTH Student",
                            color = TealAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // CARD 2: Thông tin tài khoản (Account Information)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Account Information",
                        color = TextNavyDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    ProfileInfoRow(
                        icon = Icons.Outlined.Badge,
                        label = "Student ID",
                        value = studentId
                    )
                    HorizontalDivider(thickness = 1.dp, color = Color(0xFFF2F4F7))
                    ProfileInfoRow(
                        icon = Icons.Outlined.School,
                        label = "Department",
                        value = department
                    )
                    HorizontalDivider(thickness = 1.dp, color = Color(0xFFF2F4F7))
                    ProfileInfoRow(
                        icon = Icons.Outlined.Phone,
                        label = "Phone Number",
                        value = phoneNumber
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // CARD 3: Cài đặt & Hỗ trợ (Settings & Support)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Settings & Support",
                        color = TextNavyDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Dòng Switch bật/tắt thông báo nhắc lịch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFE6F2F3)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.NotificationsActive,
                                    contentDescription = "Reminders",
                                    tint = TealAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Booking Reminders",
                                    color = TextNavyDark,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Notify 30 mins before session",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Switch(
                            checked = bookingRemindersEnabled,
                            onCheckedChange = { bookingRemindersEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = TealAccent,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = BorderGray
                            )
                        )
                    }

                    HorizontalDivider(thickness = 1.dp, color = Color(0xFFF2F4F7))

                    ProfileActionRow(
                        icon = Icons.Outlined.Gavel,
                        title = "Campus Booking Rules",
                        onClick = { showRulesDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // NÚT LOG OUT
            OutlinedButton(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = CircleShape,
                border = BorderStroke(1.dp, Color(0xFFFECDCA)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFFFEF3F2),
                    contentColor = Color(0xFFD92D20)
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = "Log Out",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LOG OUT",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF2F4F7)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = TextNavyDark,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = TextMuted,
                fontSize = 11.sp
            )
            Text(
                text = value,
                color = TextNavyDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ProfileActionRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE6F2F3)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = TealAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = TextNavyDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 428, heightDp = 926)
@Composable
fun UserProfileScreenPreview() {
    MaterialTheme {
        Scaffold(
            bottomBar = {
                UserBottomNavBar(selectedIndex = 3, onTabSelected = {})
            }
        ) { innerPadding ->
            UserProfileScreen(bottomPadding = innerPadding.calculateBottomPadding())
        }
    }
}