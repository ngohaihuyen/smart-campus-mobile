package com.example.smart_campus.ui.hung

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Figma admin-dashboard: trang Home của ADMIN. */
@Composable
fun AdminHomeScreen(
    bookings: List<AdminBooking>,
    facilitiesCount: Int,
    onOpenBooking: () -> Unit,
    onOpenFacilities: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pending = bookings.count { it.status == BookingStatus.PENDING }
    LazyColumn(modifier.background(AdminColors.Background), verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 20.dp)) {
        item { AdminHeader("Dashboard", "Manage campus facilities at a glance") }
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                StatCard("%02d".format(facilitiesCount), "Facilities", CampusIconType.BUILDING, AdminColors.Blue, Modifier.weight(1f))
                StatCard("%02d".format(bookings.size), "Bookings", CampusIconType.CALENDAR, AdminColors.Blue, Modifier.weight(1f))
                StatCard("%02d".format(pending), "Pending", CampusIconType.CLOCK, AdminColors.Amber, Modifier.weight(1f))
                StatCard("126", "Users*", CampusIconType.USERS, AdminColors.Green, Modifier.weight(1f))
            }
        }
        item { Box(Modifier.padding(horizontal = 14.dp)) { SectionHeading("Management center") } }
        item {
            Card(Modifier.fillMaxWidth().padding(horizontal = 14.dp), shape = RoundedCornerShape(13.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.fillMaxWidth().padding(13.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CampusVectorIcon(CampusIconType.CLOCK, AdminColors.Amber, Modifier.size(25.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("$pending bookings need approval", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AdminColors.Text)
                            Text("Review requests before room use", fontSize = 10.sp, color = AdminColors.Muted)
                        }
                    }
                    Spacer(Modifier.height(9.dp))
                    FilledPrimaryButton("Open approval queue   ›", onOpenBooking, Modifier.fillMaxWidth())
                }
            }
        }
        item { Box(Modifier.padding(horizontal = 14.dp)) { SectionHeading("Today's operations") } }
        item { Box(Modifier.padding(horizontal = 14.dp)) {
            AdminInfoTile("Facility directory", "View ${facilitiesCount} demo rooms", CampusIconType.BUILDING, "Manage", onOpenFacilities)
        } }
        item { Box(Modifier.padding(horizontal = 14.dp)) {
            AdminInfoTile("Booking calendar", "Check booking requests", CampusIconType.CALENDAR, "View", onOpenBooking)
        } }
        item { Box(Modifier.padding(horizontal = 14.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
                Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                    CampusVectorIcon(CampusIconType.USERS, AdminColors.Blue, Modifier.size(23.dp))
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("User accounts", fontWeight = FontWeight.Bold, color = AdminColors.Text, fontSize = 12.sp)
                        Text("Not implemented in this UI scope", color = AdminColors.Muted, fontSize = 10.sp)
                    }
                }
            }
        } }
        item { Text("*User count is a visual placeholder from the Figma layout.",
            modifier = Modifier.padding(horizontal = 16.dp), color = AdminColors.Muted, fontSize = 10.sp) }
    }
}

@Preview(name = "Admin Home", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun AdminHomePreview() {
    MaterialTheme { AdminStandalonePreview(AdminTab.HOME) { AdminHomeScreen(exampleBookings(), exampleFacilities().size, {}, {}) } }
}
