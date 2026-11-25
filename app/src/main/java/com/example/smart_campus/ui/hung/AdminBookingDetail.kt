package com.example.smart_campus.ui.hung

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Figma admin-booking-detail: chi tiết yêu cầu, timeline, approve/decline demo. */
@Composable
fun AdminBookingDetailScreen(
    booking: AdminBooking?,
    onBack: () -> Unit,
    onApprove: (String) -> Unit,
    onReject: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showReject by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    Column(modifier.fillMaxSize().background(AdminColors.Background)) {
        AdminHeader("Booking Details", "Review booking information", onBack)
        if (booking == null) {
            Text("Booking not found", Modifier.padding(20.dp), color = AdminColors.Text)
        } else {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) {
                    Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text(booking.requester, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminColors.Text)
                        Text(booking.email, color = AdminColors.Muted, fontSize = 11.sp)
                        StatusPill(booking.status)
                        HorizontalDivider(color = AdminColors.Line)
                        LabelValue("Booking ID", booking.id)
                        LabelValue("Facility", booking.facility)
                        LabelValue("Date", booking.date)
                        LabelValue("Time", "${booking.startTime} - ${booking.endTime}")
                        Text("Purpose of reservation", color = AdminColors.Muted, fontSize = 11.sp)
                        Text(booking.purpose, color = AdminColors.Text, fontSize = 12.sp)
                        if (booking.status == BookingStatus.REJECTED) {
                            Text("Rejection reason: ${booking.rejectionReason.orEmpty()}",
                                color = AdminColors.Red, fontSize = 11.sp)
                        }
                    }
                }
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Text("Approval Timeline", color = AdminColors.Text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CampusVectorIcon(CampusIconType.CALENDAR, AdminColors.Green, Modifier.size(18.dp))
                            Text("Request submitted", color = AdminColors.Green, fontSize = 12.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CampusVectorIcon(CampusIconType.CLOCK, AdminColors.Amber, Modifier.size(18.dp))
                            Text(when (booking.status) {
                                BookingStatus.PENDING -> "Awaiting department administrator approval"
                                BookingStatus.APPROVED -> "Approved (demo only)"
                                BookingStatus.REJECTED -> "Rejected (demo only)"
                                BookingStatus.CANCELLED -> "Cancelled (demo only)"
                            }, color = AdminColors.Muted, fontSize = 11.sp)
                        }
                        Text("Timeline is visual only. Backend review timestamps are not connected.",
                            color = AdminColors.Muted, fontSize = 10.sp)
                    }
                }
                if (booking.status == BookingStatus.PENDING) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showReject = true }, modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)) { Text("Reject Booking", color = AdminColors.Red, fontSize = 11.sp) }
                        FilledPrimaryButton("Approve Reservation", onClick = { onApprove(booking.id) }, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
    if (showReject && booking != null) {
        AlertDialog(onDismissRequest = { showReject = false },
            title = { Text("Reject booking") },
            text = { OutlinedTextField(reason, onValueChange = { reason = it },
                label = { Text("Rejection reason (required)") }, modifier = Modifier.fillMaxWidth()) },
            confirmButton = { TextButton(enabled = reason.isNotBlank(), onClick = {
                onReject(booking.id, reason.trim()); showReject = false
            }) { Text("Reject") } },
            dismissButton = { TextButton(onClick = { showReject = false }) { Text("Cancel") } })
    }
}

@Preview(name = "Admin Booking Detail", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun AdminBookingDetailPreview() {
    MaterialTheme { AdminStandalonePreview(AdminTab.BOOKING) { AdminBookingDetailScreen(exampleBookings().first(), {}, {}, { _, _ -> }) } }
}
