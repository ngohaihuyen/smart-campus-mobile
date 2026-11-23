package com.example.smart_campus.ui.theme.screen_user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_campus.ui.theme.*

data class UserFacilityUiModel(
    val id: String,
    val name: String,
    val category: String,
    val capacity: Int,
    val location: String,
    val description: String,
    val isAvailableToday: Boolean = true,
    val coverTint: Color = Color(0xFFD9E6E8)
)

@Composable
fun UserFacilitiesScreen(
    userName: String = "Bimbim",
    bottomPadding: Dp = 0.dp,
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onViewDetailsClick: (UserFacilityUiModel) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All Spaces") }
    var selectedCapacity by remember { mutableStateOf("Any Capacity") }
    var availableTodayOnly by remember { mutableStateOf(true) }
    var appliedQuery by remember { mutableStateOf("") }

    val allFacilities = remember {
        listOf(
            UserFacilityUiModel(
                id = "1",
                name = "Lecture Hall 201",
                category = "Lecture Hall",
                capacity = 120,
                location = "Building A, Floor 2",
                description = "Equipped with dual projectors, advanced audio system, and climate control",
                isAvailableToday = true,
                coverTint = Color(0xFFD6E6E8)
            ),
            UserFacilityUiModel(
                id = "2",
                name = "Chemistry Lab C",
                category = "Laboratory",
                capacity = 30,
                location = "Building A21, Floor 3",
                description = "Modern analytical workstations, fume hoods, and safety eyewash stations",
                isAvailableToday = true,
                coverTint = Color(0xFFDEE8E2)
            ),
            UserFacilityUiModel(
                id = "3",
                name = "Seminar Room 102",
                category = "Meeting Room",
                capacity = 24,
                location = "Building B, Floor 1",
                description = "Interactive smart board, video conferencing camera, and modular seating",
                isAvailableToday = false,
                coverTint = Color(0xFFE2E6EE)
            )
        )
    }

    val filteredFacilities = remember(appliedQuery, selectedCategory, availableTodayOnly) {
        allFacilities.filter { item ->
            val matchesQuery = appliedQuery.isBlank() ||
                    item.name.contains(appliedQuery, ignoreCase = true) ||
                    item.location.contains(appliedQuery, ignoreCase = true) ||
                    item.description.contains(appliedQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "All Spaces" ||
                    item.category.equals(selectedCategory, ignoreCase = true)
            val matchesAvailability = !availableTodayOnly || item.isAvailableToday
            matchesQuery && matchesCategory && matchesAvailability
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBgGray)
            .padding(bottom = bottomPadding)
    ) {
        // LỚP 1: Header Gradient bo cong 2 góc dưới
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(235.dp)
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(TealGradientStart, TealGradientEnd)
                    )
                )
        )

        // LỚP 2: Chia màn hình thành 2 phần: Phần trên cố định + Phần dưới chỉ cuộn thẻ phòng
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // --- PHẦN 1: CỐ ĐỊNH (Header + Thẻ tìm kiếm & bộ lọc + Thanh đếm kết quả) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(44.dp))

                // Hàng trên cùng: Nút Back bên trái + Pill User bên phải
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

                // Biểu tượng tòa nhà & Tiêu đề chính giữa Header
                Icon(
                    imageVector = Icons.Rounded.AccountBalance,
                    contentDescription = "Facilities Icon",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Facilities",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // THẺ TÌM KIẾM & BỘ LỌC (Cố định, đè lên mép dưới bo cong của Header)
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
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = "Search name, location, equipment...",
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterDropdownChip(
                                label = selectedCategory,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedCategory = when (selectedCategory) {
                                        "All Spaces" -> "Lecture Hall"
                                        "Lecture Hall" -> "Laboratory"
                                        else -> "All Spaces"
                                    }
                                }
                            )
                            FilterDropdownChip(
                                label = selectedCapacity,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedCapacity = if (selectedCapacity == "Any Capacity") "50+ People" else "Any Capacity"
                                }
                            )
                            AvailabilityToggleChip(
                                checked = availableTodayOnly,
                                modifier = Modifier.weight(1.1f),
                                onToggle = { availableTodayOnly = !availableTodayOnly }
                            )
                        }

                        Button(
                            onClick = { appliedQuery = searchQuery.trim() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NavyPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Apply Filters",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Hàng đếm kết quả & Sắp xếp (Cố định ngay bên trên danh sách cuộn)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredFacilities.size} facilities found",
                        color = TextNavyDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Sort: Most Popular",
                        color = TealAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
            }

            // --- PHẦN 2: CHỈ CUỘN DANH SÁCH THẺ PHÒNG ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(top = 6.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(
                    items = filteredFacilities,
                    key = { it.id }
                ) { facility ->
                    UserFacilityCard(
                        facility = facility,
                        modifier = Modifier.padding(horizontal = 20.dp),
                        onViewDetailsClick = { onViewDetailsClick(facility) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterDropdownChip(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(1.dp, BorderGray, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextNavyDark,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Rounded.KeyboardArrowDown,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun AvailabilityToggleChip(
    checked: Boolean,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit
) {
    val bgColor = if (checked) Color(0xFFE6F2F3) else Color.White
    val borderColor = if (checked) TealAccent else BorderGray
    val textColor = if (checked) TealAccent else TextNavyDark

    Row(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onToggle() }
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Available Today",
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (checked) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Selected",
                tint = TealAccent,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

@Composable
private fun UserFacilityCard(
    facility: UserFacilityUiModel,
    modifier: Modifier = Modifier,
    onViewDetailsClick: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Khung ảnh bìa phòng (Có thể thay bằng AsyncImage / Image painterResource khi gắn ảnh thật)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
                    .background(facility.coverTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Apartment,
                    contentDescription = facility.name,
                    tint = TealGradientStart.copy(alpha = 0.45f),
                    modifier = Modifier.size(48.dp)
                )
            }

            // Phần thân thông tin bên dưới ảnh
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Hàng 1: Badge loại phòng + Sức chứa
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFE6F2F3))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = facility.category,
                            color = TealAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.People,
                            contentDescription = "Capacity",
                            tint = TextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${facility.capacity} people",
                            color = TextNavyDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tên phòng
                Text(
                    text = facility.name,
                    color = TextNavyDark,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Vị trí
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "Location",
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = facility.location,
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Mô tả ngắn
                Text(
                    text = facility.description,
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Nút VIEW DETAILS
                Button(
                    onClick = onViewDetailsClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavyPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "VIEW DETAILS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 428, heightDp = 926)
@Composable
fun UserFacilitiesScreenPreview() {
    MaterialTheme {
        Scaffold(
            bottomBar = {
                UserBottomNavBar(selectedIndex = 1, onTabSelected = {})
            }
        ) { innerPadding ->
            UserFacilitiesScreen(bottomPadding = innerPadding.calculateBottomPadding())
        }
    }
}