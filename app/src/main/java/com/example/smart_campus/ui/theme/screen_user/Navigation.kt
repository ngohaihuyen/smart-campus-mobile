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
}

@Composable
fun AppNavGraph() {
    val rootNavController = rememberNavController()
    val context = LocalContext.current

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

        // 2. Cụm giao diện chính của User (gồm 4 tab + thanh Bottom Bar)
        composable(AppRoute.UserMain.route) {
            UserMainScreen(
                onLogout = {
                    // Đăng xuất: quay về Login và xóa sạch lịch sử các tab
                    rootNavController.navigate(AppRoute.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun UserMainScreen(onLogout: () -> Unit) {
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
                    userName = "Jennie",
                    bottomPadding = innerPadding.calculateBottomPadding(),
                    onProfileClick = { switchToTab(AppRoute.Profile.route) },
                    onBookFacilityClick = { switchToTab(AppRoute.Facilities.route) },
                    onViewAllBookingsClick = { switchToTab(AppRoute.Bookings.route) }
                )
            }

            // TAB 1: Facilities (Bạn sẽ thay bằng FacilitiesScreen() khi viết xong)
            composable(AppRoute.Facilities.route) {
                UserFacilitiesScreen(
                    userName = "Bimbim",
                    bottomPadding = innerPadding.calculateBottomPadding(),
                    onBackClick = { switchToTab(AppRoute.Home.route) },
                    onProfileClick = { switchToTab(AppRoute.Profile.route) },
                    onViewDetailsClick = { selectedFacility ->
                        // Bước tiếp theo sẽ điều hướng sang FacilityDetailsScreen(selectedFacility)
                    }
                )
            }

            // TAB 2: Bookings (Bạn sẽ thay bằng MyBookingsScreen() khi viết xong)
            composable(AppRoute.Bookings.route) {
                TabPlaceholderScreen(title = "My Bookings")
            }

            // TAB 3: Profile (Có sẵn nút LOG OUT để test quay lại màn Login)
            composable(AppRoute.Profile.route) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "My Profile (Jennie)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextNavyDark
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onLogout,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD92D20))
                    ) {
                        Text("LOG OUT", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
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