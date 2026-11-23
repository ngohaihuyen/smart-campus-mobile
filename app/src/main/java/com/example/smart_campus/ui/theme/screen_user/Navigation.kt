package com.example.smart_campus.ui.theme.screen_user

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.smart_campus.ui.theme.*

// Định nghĩa danh sách đường dẫn (Routes) của ứng dụng
sealed class AppRoute(val route: String) {
    data object Login : AppRoute("login")
    data object UserMain : AppRoute("user_main")

    // 4 tab bên trong giao diện User
    data object Home : AppRoute("tab_home")
    data object Facilities : AppRoute("tab_facilities")
    data object Bookings : AppRoute("tab_bookings")
    data object Profile : AppRoute("tab_profile")
    // tab màn chi tiết phòng
    data object FacilityDetails : AppRoute("facility_details")
}

@Composable
fun AppNavGraph() {
    val rootNavController = rememberNavController()
    val context = LocalContext.current
    var selectedFacility by remember { mutableStateOf<UserFacilityUiModel?>(null) }
    var targetTabRoute by remember { mutableStateOf<String?>(null) }

    NavHost(
        navController = rootNavController,
        startDestination = AppRoute.Login.route
    ) {
        // 1. Màn hình Đăng nhập
        composable(AppRoute.Login.route) {
            LoginScreen(
                onLoginClick = { rawEmail, password ->
                    val email = rawEmail.trim().lowercase()
                    when {
                        email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                            Toast.makeText(context, "Vui lòng nhập email hợp lệ", Toast.LENGTH_SHORT).show()
                        }
                        !email.endsWith("@usth.edu.vn") -> {
                            Toast.makeText(context, "Chỉ chấp nhận email @usth.edu.vn", Toast.LENGTH_SHORT).show()
                        }
                        password.length < 6 -> {
                            Toast.makeText(context, "Mật khẩu tối thiểu 6 ký tự", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            // Chuyển sang Dashboard và xóa Login khỏi lịch sử Back
                            rootNavController.navigate(AppRoute.UserMain.route) {
                                popUpTo(AppRoute.Login.route) { inclusive = true }
                            }
                        }
                    }
                }
            )
        }

        // 2. Cụm giao diện chính của User
        composable(AppRoute.UserMain.route) {
            UserMainScreen(
                onLogout = {
                    rootNavController.navigate(AppRoute.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onOpenFacilityDetails = { facility ->
                    selectedFacility = facility
                    rootNavController.navigate(AppRoute.FacilityDetails.route)
                }
            )
        }

        // 3. Màn hình Chi tiết phòng & Đặt lịch
        composable(AppRoute.FacilityDetails.route) {
            FacilityDetailsScreen(
                facility = selectedFacility,
                userName = "Bimbim",
                onBackClick = { rootNavController.popBackStack() },
                // Bấm pill user -> đóng màn Details và mở thẳng tab Profile
                onProfileClick = {
                    targetTabRoute = AppRoute.Profile.route
                    rootNavController.popBackStack()
                },
                onConfirmBookingClick = { date, start, end, _ ->
                    Toast.makeText(
                        context,
                        "Đã gửi yêu cầu đặt phòng ($date • $start – $end)",
                        Toast.LENGTH_SHORT
                    ).show()
                    rootNavController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun UserMainScreen(
    targetTabRoute: String? = null,
    onTargetTabConsumed: () -> Unit = {},
    onLogout: () -> Unit,
    onOpenFacilityDetails: (UserFacilityUiModel?) -> Unit = {}
) {
    val tabNavController = rememberNavController()
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Danh sách 4 route tương ứng với thứ tự 0, 1, 2, 3 trên UserBottomNavBar
    val tabRoutes = listOf(
        AppRoute.Home.route,
        AppRoute.Facilities.route,
        AppRoute.Bookings.route,
        AppRoute.Profile.route
    )
    val selectedIndex = tabRoutes.indexOf(currentRoute).coerceAtLeast(0)

    // Hàm chuyển tab chuẩn Material 3 (không tạo chồng nhiều bản sao màn hình)
    fun switchToTab(route: String) {
        tabNavController.navigate(route) {
            popUpTo(tabNavController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    // Tự động chuyển tab ngay khi nhận được yêu cầu từ màn hình ngoài
    LaunchedEffect(targetTabRoute) {
        if (targetTabRoute != null) {
            switchToTab(targetTabRoute)
            onTargetTabConsumed()
        }
    }

    Scaffold(
        containerColor = ScreenBgGray,
        bottomBar = {
            UserBottomNavBar(
                selectedIndex = selectedIndex,
                onTabSelected = { index ->
                    switchToTab(tabRoutes[index])
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = tabNavController,
            startDestination = AppRoute.Home.route
        ) {
            // TAB 0: Home (User Dashboard)
            composable(AppRoute.Home.route) {
                UserDashboardScreen(
                    userName = "BimBim",
                    bottomPadding = innerPadding.calculateBottomPadding(),
                    onProfileClick = { switchToTab(AppRoute.Profile.route) },
                    onBookFacilityClick = { switchToTab(AppRoute.Facilities.route) },
                    onViewAllBookingsClick = { switchToTab(AppRoute.Bookings.route) },
                    onBookingItemClick = { onOpenFacilityDetails(null) }
                )
            }

            // TAB 1: Facilities (Bạn sẽ thay bằng FacilitiesScreen() khi viết xong)
            composable(AppRoute.Facilities.route) {
                UserFacilitiesScreen(
                    userName = "Bimbim",
                    bottomPadding = innerPadding.calculateBottomPadding(),
                    onBackClick = { switchToTab(AppRoute.Home.route) },
                    onProfileClick = { switchToTab(AppRoute.Profile.route) },
                    onViewDetailsClick = { selectedFacility -> onOpenFacilityDetails(selectedFacility)
                    }
                )
            }

            // TAB 2: Bookings
            composable(AppRoute.Bookings.route) {
                MyBookingsScreen(
                    userName = "BimBim",
                    bottomPadding = innerPadding.calculateBottomPadding(),
                    onBackClick = { switchToTab(AppRoute.Home.route) },
                    onProfileClick = { switchToTab(AppRoute.Profile.route) },
                    onBrowseFacilitiesClick = { switchToTab(AppRoute.Facilities.route) },
                    onViewBookingClick = { reservation ->
                        // Chuyển sang màn hình chi tiết của phòng đã đặt
                        onOpenFacilityDetails(
                            UserFacilityUiModel(
                                id = reservation.id,
                                name = reservation.facilityName,
                                category = reservation.category,
                                capacity = 60,
                                location = "Campus Building",
                                description = "Reserved on ${reservation.date} (${reservation.timeSlot})"
                            )
                        )
                    }
                )
            }

            // TAB 3: My Profile
            composable(AppRoute.Profile.route) {
                UserProfileScreen(
                    userName = "Bimbim",
                    userEmail = "thangdzai@usth.edu.vn",
                    bottomPadding = innerPadding.calculateBottomPadding(),
                    onBackClick = { switchToTab(AppRoute.Home.route) },
                    onLogoutClick = onLogout
                )
            }
        }
    }
}

@Composable
private fun TabPlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextNavyDark
        )
    }
}