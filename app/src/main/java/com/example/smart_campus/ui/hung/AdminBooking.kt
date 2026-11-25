package com.example.smart_campus.ui.hung

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class BookingFilter { PENDING, APPROVED, REJECTED, ALL }

/** Figma admin-booking: danh sách / tìm kiếm / lọc và xử lý demo. */
@Composable
fun AdminBookingScreen(
    bookings: List<AdminBooking>,
    onOpenDetail: (String) -> Unit,
    onApprove: (String) -> Unit,
    onReject: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var search by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(BookingFilter.PENDING) }
    var rejectId by remember { mutableStateOf<String?>(null) }
    var rejectReason by remember { mutableStateOf("") }
    val visible = bookings.filter { booking ->
        val matchStatus = when (filter) {
            BookingFilter.PENDING -> booking.status == BookingStatus.PENDING
            BookingFilter.APPROVED -> booking.status == BookingStatus.APPROVED
            BookingFilter.REJECTED -> booking.status == BookingStatus.REJECTED
            BookingFilter.ALL -> true
        }
        matchStatus && (search.isBlank() || listOf(booking.requester, booking.facility, booking.id).any {
            it.contains(search.trim(), ignoreCase = true)
        })
    }
    LazyColumn(modifier.background(AdminColors.Background), verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 20.dp)) {
        item { AdminHeader("Booking", "Review and approve room reservations") }
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                CounterCard(bookings.count { it.status == BookingStatus.PENDING }, "Awaiting review", AdminColors.Amber, Modifier.weight(1f))
                CounterCard(bookings.count { it.status == BookingStatus.APPROVED }, "Approved", AdminColors.Green, Modifier.weight(1f))
                CounterCard(bookings.count { it.status == BookingStatus.REJECTED }, "Rejected", AdminColors.Red, Modifier.weight(1f))
            }
        }
        item { Box(Modifier.padding(horizontal = 14.dp)) { SectionHeading("Approval queue") } }
        item {
            Column(Modifier.padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BookingFilter.entries.forEach { tab ->
                        val selected = tab == filter
                        Surface(shape = CircleShape, color = if (selected) AdminColors.Blue else Color.White,
                            modifier = Modifier.clickable { filter = tab }) {
                            Text(tab.name.lowercase().replaceFirstChar { it.uppercase() },
                                fontSize = 11.sp, color = if (selected) Color.White else AdminColors.Muted,
                                modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp))
                        }
                    }
                }
                OutlinedTextField(value = search, onValueChange = { search = it }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Search student, room or booking ID", fontSize = 11.sp) },
                    leadingIcon = { CampusVectorIcon(CampusIconType.SEARCH, AdminColors.Muted, Modifier.size(19.dp)) })
            }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Needs your decision", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AdminColors.Text)
                Text("${visible.size} requests", fontSize = 10.sp, color = AdminColors.Muted)
            }
        }
        if (visible.isEmpty()) item {
            Text("No bookings match this filter", modifier = Modifier.padding(18.dp), color = AdminColors.Muted)
        }
        items(visible, key = { it.id }) { booking ->
            Box(Modifier.padding(horizontal = 14.dp)) {
                AdminBookingCard(booking, onOpen = { onOpenDetail(booking.id) },
                    onApprove = { onApprove(booking.id) }, onReject = {
                        rejectId = booking.id; rejectReason = ""
                    })
            }
        }
    }
    if (rejectId != null) {
        AlertDialog(onDismissRequest = { rejectId = null }, title = { Text("Decline booking") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("A rejection reason is required by the project rules.", fontSize = 12.sp)
                    OutlinedTextField(rejectReason, onValueChange = { rejectReason = it },
                        label = { Text("Rejection reason") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                }
            },
            confirmButton = {
                TextButton(enabled = rejectReason.isNotBlank(), onClick = {
                    rejectId?.let { onReject(it, rejectReason.trim()) }; rejectId = null
                }) { Text("Decline") }
            }, dismissButton = { TextButton(onClick = { rejectId = null }) { Text("Cancel") } })
    }
}

@Composable
internal fun CounterCard(count: Int, title: String, accent: Color, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(10.dp)) {
        Column(Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(3.dp).height(19.dp).background(accent, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text("%02d".format(count), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminColors.Text)
            }
            Text(title, fontSize = 9.sp, color = AdminColors.Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
internal fun AdminBookingCard(
    booking: AdminBooking, onOpen: () -> Unit, onApprove: () -> Unit, onReject: () -> Unit
) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onOpen),
        shape = RoundedCornerShape(13.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(booking.id, fontSize = 10.sp, color = AdminColors.Blue, fontWeight = FontWeight.SemiBold)
                StatusPill(booking.status)
            }
            Text(booking.requester, color = AdminColors.Text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(booking.email, color = AdminColors.Muted, fontSize = 10.sp)
            HorizontalDivider(color = AdminColors.Line)
            Text(booking.facility, color = AdminColors.Text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text("${booking.date}  •  ${booking.startTime}–${booking.endTime}", color = AdminColors.Muted, fontSize = 10.sp)
            Text("Purpose: ${booking.purpose}", color = AdminColors.Muted, fontSize = 10.sp)
            if (booking.hasScheduleConflict) Text("Schedule conflict needs checking", color = AdminColors.Red, fontSize = 10.sp)
            else Text("No scheduling conflict found (demo)", color = AdminColors.Green, fontSize = 10.sp)
            if (booking.status == BookingStatus.PENDING) {
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) {
                        Text("Decline", color = AdminColors.Red, fontSize = 11.sp)
                    }
                    FilledPrimaryButton("Approve", onApprove, Modifier.weight(1f))
                }
            }
            // Clear entry point from any booking card (including PENDING) to Booking Detail.
            HorizontalDivider(color = AdminColors.Line)
            TextButton(
                onClick = onOpen,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("View booking details", color = AdminColors.Blue,
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    CampusVectorIcon(CampusIconType.CHEVRON, AdminColors.Blue, Modifier.size(17.dp))
                }
            }
        }
    }
}

@Preview(name = "Admin Booking - rieng", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun AdminBookingPreview() {
    // Chi preview man Booking trong file nay; khong goi AdminNavigation.
    MaterialTheme {
        AdminStandalonePreview(AdminTab.BOOKING) {
            AdminBookingScreen(
                bookings = exampleBookings(),
                onOpenDetail = {},
                onApprove = {},
                onReject = { _, _ -> }
            )
        }
    }
}
