// Change this package to match your Android app package before copying.
package com.example.smart_campus.ui.hung

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Scoped to Hung's 4 Figma frames. No API, database or authentication code.
private val Teal = Color(0xFF116F76)
private val TealDark = Color(0xFF0C6269)
private val ActionBlue = Color(0xFF2563EB)
private val PageGray = Color(0xFFF4F6F8)
private val Ink = Color(0xFF1C2737)
private val Muted = Color(0xFF78889A)
private val Positive = Color(0xFF14946B)
private val Negative = Color(0xFFBF4656)
private val Amber = Color(0xFFB97B12)

// Demo-only model, deliberately kept independent of your team's API/Room models.
enum class HungBookingStatus { PENDING, APPROVED, REJECTED }

data class HungBooking(
    val id: String,
    val requester: String,
    val isMine: Boolean,
    val facility: String,
    val date: String,
    val time: String,
    val purpose: String,
    val status: HungBookingStatus,
    val conflictFree: Boolean = true
)

private fun sampleBookings(): List<HungBooking> = listOf(
    HungBooking("BK-001", "Nguyen Khac Hung", true, "Study Room A101", "15 Nov 2026", "08:00 - 09:30", "Study group", HungBookingStatus.PENDING),
    HungBooking("BK-002", "Nguyen Van A", false, "Meeting Room B205", "15 Nov 2026", "10:00 - 11:00", "Team discussion", HungBookingStatus.PENDING),
    HungBooking("BK-003", "Nguyen Khac Hung", true, "Meeting Room B205", "16 Nov 2026", "14:00 - 15:30", "Project meeting", HungBookingStatus.APPROVED),
    HungBooking("BK-004", "Tran Thi B", false, "Study Room A101", "17 Nov 2026", "09:00 - 10:00", "Seminar", HungBookingStatus.REJECTED),
    HungBooking("BK-005", "Nguyen Khac Hung", true, "Sport Hall", "18 Nov 2026", "16:00 - 17:00", "Training", HungBookingStatus.PENDING),
    HungBooking("BK-006", "Le Minh C", false, "Computer Lab C102", "18 Nov 2026", "09:00 - 10:30", "Lab session", HungBookingStatus.APPROVED)
)

private enum class DemoRole { USER, ADMIN }
private enum class ScreenTab { HOME, BOOKING }
private enum class FilterTab { PENDING, APPROVED, REJECTED, ALL }

/**
 * Optional demo entry point for Figma review. Do not replace the team's MainActivity.
 * The role toggle here is a DEMO switch, NOT real authorization.
 */
@Composable
fun HungUiDemo() {
    var role by remember { mutableStateOf(DemoRole.USER) }
    var tab by remember { mutableStateOf(ScreenTab.HOME) }
    val bookings = remember { mutableStateListOf<HungBooking>().apply { addAll(sampleBookings()) } }

    MaterialTheme(colorScheme = lightColorScheme(primary = ActionBlue, background = PageGray)) {
        Column(Modifier.fillMaxSize().background(PageGray)) {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("UI DEMO · HUNG", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RoleDemoChip("User", role == DemoRole.USER) { role = DemoRole.USER; tab = ScreenTab.HOME }
                    RoleDemoChip("Admin", role == DemoRole.ADMIN) { role = DemoRole.ADMIN; tab = ScreenTab.HOME }
                }
            }
            Scaffold(
                modifier = Modifier.weight(1f),
                containerColor = PageGray,
                bottomBar = { CampusBottomNav(tab) { tab = it } }
            ) { innerPadding ->
                val screenModifier = Modifier.fillMaxSize().padding(innerPadding)
                when {
                    role == DemoRole.USER && tab == ScreenTab.HOME -> HungUserDashboardScreen(
                        bookings = bookings.filter { it.isMine },
                        onOpenBookings = { tab = ScreenTab.BOOKING },
                        modifier = screenModifier
                    )
                    role == DemoRole.USER -> HungUserBookingScreen(
                        bookings = bookings.filter { it.isMine }, modifier = screenModifier
                    )
                    role == DemoRole.ADMIN && tab == ScreenTab.HOME -> HungAdminDashboardScreen(
                        bookings = bookings,
                        onOpenApprovalQueue = { tab = ScreenTab.BOOKING },
                        modifier = screenModifier
                    )
                    else -> HungAdminBookingScreen(
                        bookings = bookings,
                        onDecision = { id, newStatus ->
                            val index = bookings.indexOfFirst { it.id == id }
                            if (index >= 0 && bookings[index].status == HungBookingStatus.PENDING) {
                                bookings[index] = bookings[index].copy(status = newStatus)
                            }
                        },
                        modifier = screenModifier
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleDemoChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) Teal else PageGray,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 15.dp, vertical = 6.dp),
            color = if (selected) Color.White else Muted, fontSize = 12.sp)
    }
}

@Composable
private fun CampusBottomNav(selected: ScreenTab, onNavigate: (ScreenTab) -> Unit) {
    Surface(color = Color.White, shadowElevation = 8.dp) {
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceAround) {
            BottomNavEntry(CampusIconType.GRID, "Home", selected == ScreenTab.HOME, true) { onNavigate(ScreenTab.HOME) }
            BottomNavEntry(CampusIconType.CALENDAR, "Booking", selected == ScreenTab.BOOKING, true) { onNavigate(ScreenTab.BOOKING) }
            BottomNavEntry(CampusIconType.BUILDING, "Facilities", false, false) {}
            BottomNavEntry(CampusIconType.PERSON, "Profile", false, false) {}
        }
    }
}

@Composable
private fun BottomNavEntry(symbol: CampusIconType, text: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val color = if (selected) ActionBlue else Color(0xFF98A6B8)
    Column(
        modifier = Modifier.clip(RoundedCornerShape(10.dp))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 10.dp, vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        CampusVectorIcon(symbol, color, Modifier.size(21.dp))
        Text(text, color = color, fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

/**
 * Outline pictograms matched to the visual icon categories shown in the supplied Figma.
 * No Unicode stand-ins: icon geometry is independent of the device font.
 * For pixel-perfect original vectors, replace these with SVGs exported from Figma.
 */
private enum class CampusIconType {
    BUILDING, CALENDAR, CLOCK, USERS, GRID, PERSON, SEARCH, SPARKLE, CHEVRON
}

@Composable
private fun CampusVectorIcon(
    type: CampusIconType,
    tint: Color,
    modifier: Modifier = Modifier.size(20.dp)
) {
    Canvas(modifier) {
        val k = size.minDimension / 24f
        val pen = Stroke(width = 1.9f * k, cap = StrokeCap.Round, join = StrokeJoin.Round)
        fun pt(x: Float, y: Float) = Offset(x * k, y * k)
        fun ln(x1: Float, y1: Float, x2: Float, y2: Float) =
            drawLine(tint, pt(x1, y1), pt(x2, y2), 1.9f * k, StrokeCap.Round)
        fun linePath(vararg points: Pair<Float, Float>) {
            val path = Path().apply {
                points.forEachIndexed { i, p ->
                    if (i == 0) moveTo(p.first * k, p.second * k)
                    else lineTo(p.first * k, p.second * k)
                }
            }
            drawPath(path, tint, style = pen)
        }
        fun rect(x: Float, y: Float, w: Float, h: Float, r: Float = 1.2f) {
            drawRoundRect(tint, pt(x, y), Size(w * k, h * k),
                CornerRadius(r * k, r * k), style = pen)
        }
        fun circle(x: Float, y: Float, r: Float) {
            drawCircle(tint, r * k, pt(x, y), style = pen)
        }
        when (type) {
            CampusIconType.BUILDING -> {
                rect(6f, 3f, 12f, 18f, 1.2f)
                rect(9f, 6f, 2f, 2f, 0.15f)
                rect(13f, 6f, 2f, 2f, 0.15f)
                rect(9f, 10f, 2f, 2f, 0.15f)
                rect(13f, 10f, 2f, 2f, 0.15f)
                ln(10f, 21f, 10f, 16f); ln(14f, 21f, 14f, 16f)
                ln(3f, 21f, 21f, 21f)
            }
            CampusIconType.CALENDAR -> {
                rect(3f, 5f, 18f, 16f, 1.8f)
                ln(3f, 10f, 21f, 10f)
                ln(8f, 2.8f, 8f, 7.3f)
                ln(16f, 2.8f, 16f, 7.3f)
            }
            CampusIconType.CLOCK -> {
                circle(12f, 12f, 9f)
                linePath(12f to 6.5f, 12f to 12f, 16f to 14.4f)
            }
            CampusIconType.USERS -> {
                circle(9f, 8.3f, 3.1f)
                val a = Path().apply { moveTo(2.5f*k,20f*k); cubicTo(2.5f*k,12f*k,15.5f*k,12f*k,15.5f*k,20f*k) }
                drawPath(a, tint, style = pen)
                val b = Path().apply { moveTo(16.6f*k,5.2f*k); cubicTo(21.4f*k,4.9f*k,21.6f*k,11f*k,17.2f*k,11.2f*k) }
                drawPath(b, tint, style = pen)
                val c = Path().apply { moveTo(18f*k,14f*k); cubicTo(21.2f*k,14.3f*k,22f*k,16.7f*k,22f*k,19.7f*k) }
                drawPath(c, tint, style = pen)
            }
            CampusIconType.PERSON -> {
                circle(12f, 7.8f, 3.5f)
                val path = Path().apply { moveTo(4f*k,21f*k); cubicTo(4f*k,13.2f*k,20f*k,13.2f*k,20f*k,21f*k) }
                drawPath(path, tint, style = pen)
            }
            CampusIconType.GRID -> {
                rect(3f, 3f, 7.4f, 7.4f, 0.6f)
                rect(13.6f, 3f, 7.4f, 7.4f, 0.6f)
                rect(3f, 13.6f, 7.4f, 7.4f, 0.6f)
                rect(13.6f, 13.6f, 7.4f, 7.4f, 0.6f)
            }
            CampusIconType.SEARCH -> {
                circle(10.5f, 10.5f, 6f)
                ln(15f, 15f, 21f, 21f)
            }
            CampusIconType.SPARKLE -> {
                linePath(12f to 2f, 14.6f to 9.4f, 22f to 12f, 14.6f to 14.6f, 12f to 22f, 9.4f to 14.6f, 2f to 12f, 9.4f to 9.4f, 12f to 2f)
            }
            CampusIconType.CHEVRON -> linePath(9f to 5f, 16f to 12f, 9f to 19f)
        }
    }
}

/** White outline notification bell used in every campus header; no extra icon library needed. */
@Composable
private fun NotificationBellIcon() {
    Canvas(modifier = Modifier.size(22.dp)) {
        val w = size.width
        val h = size.height
        val outline = Stroke(
            width = 1.7.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
        val bell = Path().apply {
            moveTo(w * 0.18f, h * 0.73f)
            lineTo(w * 0.28f, h * 0.62f)
            lineTo(w * 0.28f, h * 0.43f)
            cubicTo(w * 0.28f, h * 0.28f, w * 0.37f, h * 0.19f, w * 0.50f, h * 0.19f)
            cubicTo(w * 0.63f, h * 0.19f, w * 0.72f, h * 0.28f, w * 0.72f, h * 0.43f)
            lineTo(w * 0.72f, h * 0.62f)
            lineTo(w * 0.82f, h * 0.73f)
            close()
        }
        drawPath(path = bell, color = Color.White, style = outline)
        drawCircle(
            color = Color.White,
            radius = w * 0.055f,
            center = Offset(w * 0.50f, h * 0.84f)
        )
    }
}

@Composable
private fun CampusHeader(title: String, subtitle: String, admin: Boolean = false) {
    Box(Modifier.fillMaxWidth().background(Teal).padding(start = 16.dp, end = 16.dp, top = 15.dp, bottom = 20.dp)) {
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(30.dp).background(Color.White, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    CampusVectorIcon(CampusIconType.BUILDING, Ink, Modifier.size(20.dp))
                }
                Spacer(Modifier.width(7.dp))
                Column(Modifier.weight(1f)) {
                    Text("CAMPUS", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 11.sp)
                    Text("FACILITY BOOKING", color = Color.White.copy(alpha = .84f), fontSize = 8.sp, lineHeight = 10.sp)
                }
                if (admin) {
                    Surface(color = Color(0xFF0B1C25), shape = CircleShape) {
                        Text("ADMIN", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp))
                    }
                    Spacer(Modifier.width(9.dp))
                }
                Box(Modifier.size(32.dp).background(TealDark, CircleShape), contentAlignment = Alignment.Center) {
                    NotificationBellIcon()
                    Box(Modifier.align(Alignment.TopEnd).size(6.dp)
                        .background(Color(0xFF52C8CD), CircleShape))
                }
            }
            Spacer(Modifier.height(17.dp))
            Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, color = Color.White.copy(alpha = .74f), fontSize = 11.sp)
        }
    }
}

@Composable
private fun PageSectionTitle(title: String, actionText: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, fontSize = 14.sp, color = Ink, fontWeight = FontWeight.Bold)
        if (actionText != null && onAction != null) {
            Text(actionText, fontSize = 11.sp, color = ActionBlue, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onAction).padding(4.dp))
        }
    }
}

@Composable
private fun MetricCard(value: String, label: String, symbol: CampusIconType, tint: Color, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Box(Modifier.size(27.dp).background(tint.copy(alpha = .12f), RoundedCornerShape(7.dp)),
                contentAlignment = Alignment.Center) { CampusVectorIcon(symbol, tint, Modifier.size(16.dp)) }
            Text(value, color = Ink, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 27.sp)
            Text(label, color = Muted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun CompactMetric(value: String, label: String, symbol: CampusIconType, tint: Color, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.fillMaxWidth().padding(9.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            CampusVectorIcon(symbol, tint, Modifier.size(18.dp))
            Text(value, color = Ink, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
            Text(label, color = Muted, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun StatusBadge(status: HungBookingStatus) {
    val color = when (status) {
        HungBookingStatus.PENDING -> Amber
        HungBookingStatus.APPROVED -> Positive
        HungBookingStatus.REJECTED -> Negative
    }
    val label = when (status) {
        HungBookingStatus.PENDING -> "Pending"
        HungBookingStatus.APPROVED -> "Approved"
        HungBookingStatus.REJECTED -> "Rejected"
    }
    Surface(shape = CircleShape, color = color.copy(alpha = .12f)) {
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp))
    }
}

@Composable
private fun InfoCard(title: String, detail: String, symbol: CampusIconType = CampusIconType.BUILDING, actionLabel: String? = null) {
    Card(shape = RoundedCornerShape(13.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(34.dp).background(Color(0xFFEAF2FF), RoundedCornerShape(9.dp)), contentAlignment = Alignment.Center) {
                CampusVectorIcon(symbol, ActionBlue, Modifier.size(21.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(detail, color = Muted, fontSize = 10.sp)
            }
            if (actionLabel != null) Text(actionLabel, color = ActionBlue, fontSize = 10.sp)
        }
    }
}

/** Figma frame: user-dashboard */
@Composable
fun HungUserDashboardScreen(
    bookings: List<HungBooking>,
    onOpenBookings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pending = bookings.count { it.status == HungBookingStatus.PENDING }
    LazyColumn(modifier.background(PageGray), verticalArrangement = Arrangement.spacedBy(11.dp),
        contentPadding = PaddingValues(bottom = 18.dp)) {
        item { CampusHeader("Dashboard", "Manage campus facilities at a glance") }
        item {
            Column(Modifier.padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    MetricCard("24", "Total facilities", CampusIconType.BUILDING, ActionBlue, Modifier.weight(1f))
                    MetricCard("%02d".format(pending), "Pending requests", CampusIconType.CLOCK, Amber, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    MetricCard("18", "Today's bookings", CampusIconType.CALENDAR, Color(0xFF15A4A7), Modifier.weight(1f))
                    MetricCard("126", "Active users", CampusIconType.USERS, ActionBlue, Modifier.weight(1f))
                }
            }
        }
        item {
            Surface(Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                shape = RoundedCornerShape(13.dp), color = Color(0xFFE6F2FF)) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    CampusVectorIcon(CampusIconType.SPARKLE, ActionBlue, Modifier.size(21.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("$pending bookings awaiting review", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text("Track the status of your requests", fontSize = 10.sp, color = Muted)
                        TextButton(onClick = onOpenBookings, contentPadding = PaddingValues(0.dp)) {
                            Text("Review requests  →", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
        item {
            Box(Modifier.padding(horizontal = 14.dp)) {
                PageSectionTitle("Recent bookings", "View all", onOpenBookings)
            }
        }
        items(bookings.take(3), key = { it.id }) { booking ->
            Box(Modifier.padding(horizontal = 14.dp)) { RecentBookingCard(booking) }
        }
        if (bookings.isEmpty()) item { EmptyState("You do not have any bookings yet") }
    }
}

@Composable
private fun RecentBookingCard(booking: HungBooking) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(36.dp).background(Color(0xFFEAF2FF), RoundedCornerShape(9.dp)), contentAlignment = Alignment.Center) {
                Text("A", color = ActionBlue, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(booking.facility, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink)
                Text("${booking.date} · ${booking.time}", fontSize = 10.sp, color = Muted)
            }
            StatusBadge(booking.status)
        }
    }
}

/** Figma frame: user-booking. Users may view their own requests, not approve others' requests. */
@Composable
fun HungUserBookingScreen(bookings: List<HungBooking>, modifier: Modifier = Modifier) {
    var search by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(FilterTab.PENDING) }
    val visible = bookings.filter { booking ->
        (filter == FilterTab.ALL || booking.status.name == filter.name) &&
            (search.isBlank() || (booking.facility + booking.id + booking.purpose).contains(search, ignoreCase = true))
    }
    LazyColumn(modifier.background(PageGray), verticalArrangement = Arrangement.spacedBy(11.dp),
        contentPadding = PaddingValues(bottom = 18.dp)) {
        item { CampusHeader("Booking", "Review and track your reservations") }
        item {
            Column(Modifier.padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = search, onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    placeholder = { Text("Search rooms or booking ID", fontSize = 12.sp) },
                    leadingIcon = { CampusVectorIcon(CampusIconType.SEARCH, Muted, Modifier.size(20.dp)) },
                    shape = RoundedCornerShape(12.dp))
                FilterBar(filter) { filter = it }
            }
        }
        item {
            Box(Modifier.padding(horizontal = 14.dp)) {
                PageSectionTitle(
                    when (filter) {
                        FilterTab.PENDING -> "Pending requests"
                        FilterTab.APPROVED -> "Approved requests"
                        FilterTab.REJECTED -> "Rejected requests"
                        FilterTab.ALL -> "All bookings"
                    }
                )
            }
        }
        if (visible.isEmpty()) item { EmptyState("No bookings found for this filter") }
        items(visible, key = { it.id }) { booking ->
            Box(Modifier.padding(horizontal = 14.dp)) { BookingDetailsCard(booking, admin = false) }
        }
        item {
            Text("Booking status is demo data until the API is connected.",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 6.dp),
                color = Muted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun FilterBar(selected: FilterTab, onSelect: (FilterTab) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        FilterTab.entries.forEach { item ->
            val label = when (item) {
                FilterTab.PENDING -> "Pending"
                FilterTab.APPROVED -> "Approved"
                FilterTab.REJECTED -> "Rejected"
                FilterTab.ALL -> "All"
            }
            Surface(shape = CircleShape,
                color = if (selected == item) ActionBlue else Color.White,
                modifier = Modifier.clickable { onSelect(item) }) {
                Text(label, color = if (selected == item) Color.White else Muted,
                    fontSize = 11.sp, fontWeight = if (selected == item) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 17.dp, vertical = 9.dp))
            }
        }
    }
}

/** Figma frame: admin-dashboard */
@Composable
fun HungAdminDashboardScreen(
    bookings: List<HungBooking>,
    onOpenApprovalQueue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pending = bookings.count { it.status == HungBookingStatus.PENDING }
    LazyColumn(modifier.background(PageGray), verticalArrangement = Arrangement.spacedBy(11.dp),
        contentPadding = PaddingValues(bottom = 18.dp)) {
        item { CampusHeader("Dashboard", "Manage campus facilities at a glance", admin = true) }
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CompactMetric("24", "Facilities", CampusIconType.BUILDING, ActionBlue, Modifier.weight(1f))
                CompactMetric("18", "Bookings", CampusIconType.CALENDAR, ActionBlue, Modifier.weight(1f))
                CompactMetric("%02d".format(pending), "Pending", CampusIconType.CLOCK, Amber, Modifier.weight(1f))
                CompactMetric("126", "Users", CampusIconType.USERS, Positive, Modifier.weight(1f))
            }
        }
        item { Box(Modifier.padding(horizontal = 14.dp)) { PageSectionTitle("Management center") } }
        item {
            Card(Modifier.fillMaxWidth().padding(horizontal = 14.dp), shape = RoundedCornerShape(13.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CampusVectorIcon(CampusIconType.CLOCK, Amber, Modifier.size(23.dp))
                        Spacer(Modifier.width(9.dp))
                        Column {
                            Text("$pending bookings need approval", color = Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Review requests before their start time", color = Muted, fontSize = 10.sp)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = onOpenApprovalQueue, modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                        shape = RoundedCornerShape(9.dp)) { Text("Open approval queue   ›", fontSize = 12.sp) }
                }
            }
        }
        item { Box(Modifier.padding(horizontal = 14.dp)) { PageSectionTitle("Today's operations") } }
        item { Box(Modifier.padding(horizontal = 14.dp)) {
            InfoCard("Facility directory", "View the facility inventory", CampusIconType.BUILDING, "Preview")
        } }
        item { Box(Modifier.padding(horizontal = 14.dp)) {
            InfoCard("Booking calendar", "Check the schedule and conflicts", CampusIconType.CALENDAR, "Preview")
        } }
        item { Box(Modifier.padding(horizontal = 14.dp)) {
            InfoCard("User accounts", "Account administration (out of scope)", CampusIconType.USERS)
        } }
    }
}

/** Figma frame: admin-booking. The only screen that can approve/reject demo requests. */
@Composable
fun HungAdminBookingScreen(
    bookings: List<HungBooking>,
    onDecision: (bookingId: String, newStatus: HungBookingStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    var search by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(FilterTab.PENDING) }
    val pending = bookings.count { it.status == HungBookingStatus.PENDING }
    val approved = bookings.count { it.status == HungBookingStatus.APPROVED }
    val rejected = bookings.count { it.status == HungBookingStatus.REJECTED }
    val visible = bookings.filter { booking ->
        (filter == FilterTab.ALL || booking.status.name == filter.name) &&
            (search.isBlank() || (booking.requester + booking.facility + booking.id).contains(search, ignoreCase = true))
    }
    LazyColumn(modifier.background(PageGray), verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 18.dp)) {
        item { CampusHeader("Booking", "Review and approve room reservations", admin = true) }
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusMetric("%02d".format(pending), "Awaiting review", Amber, Modifier.weight(1f))
                StatusMetric("%02d".format(approved), "Approved", Positive, Modifier.weight(1f))
                StatusMetric("%02d".format(rejected), "Rejected", Negative, Modifier.weight(1f))
            }
        }
        item { Box(Modifier.padding(horizontal = 14.dp)) { PageSectionTitle("Approval queue") } }
        item {
            Column(Modifier.padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterBar(filter) { filter = it }
                OutlinedTextField(value = search, onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    placeholder = { Text("Search student, room or booking ID", fontSize = 12.sp) },
                    leadingIcon = { CampusVectorIcon(CampusIconType.SEARCH, Muted, Modifier.size(20.dp)) },
                    shape = RoundedCornerShape(12.dp))
            }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (filter == FilterTab.PENDING) "Needs your decision" else "Booking requests",
                    color = Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("${visible.size} requests", fontSize = 10.sp, color = Muted)
            }
        }
        if (visible.isEmpty()) item { EmptyState("No requests found") }
        items(visible, key = { it.id }) { booking ->
            Box(Modifier.padding(horizontal = 14.dp)) {
                BookingDetailsCard(booking, admin = true,
                    onApprove = { onDecision(booking.id, HungBookingStatus.APPROVED) },
                    onReject = { onDecision(booking.id, HungBookingStatus.REJECTED) })
            }
        }
    }
}

@Composable
private fun StatusMetric(value: String, label: String, accent: Color, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(11.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(3.dp).height(19.dp).background(accent, CircleShape))
                Spacer(Modifier.width(5.dp))
                Text(value, color = Ink, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            }
            Text(label, color = Muted, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun BookingDetailsCard(
    booking: HungBooking,
    admin: Boolean,
    onApprove: () -> Unit = {},
    onReject: () -> Unit = {}
) {
    Card(shape = RoundedCornerShape(13.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(booking.id, color = ActionBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.background(Color(0xFFEAF2FF), CircleShape).padding(horizontal = 9.dp, vertical = 4.dp))
                StatusBadge(booking.status)
            }
            if (admin) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(34.dp).background(Color(0xFFE6F8F6), CircleShape), contentAlignment = Alignment.Center) {
                        Text("NH", color = Teal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(9.dp))
                    Column {
                        Text(booking.requester, color = Ink, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Student · USTH", color = Muted, fontSize = 10.sp)
                    }
                }
                HorizontalDivider(color = Color(0xFFF0F2F4))
            }
            Text(booking.facility, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                CampusVectorIcon(CampusIconType.CALENDAR, Muted, Modifier.size(14.dp))
                Text(booking.date, color = Muted, fontSize = 11.sp)
                Spacer(Modifier.width(8.dp))
                CampusVectorIcon(CampusIconType.CLOCK, Muted, Modifier.size(14.dp))
                Text(booking.time, color = Muted, fontSize = 11.sp)
            }
            Text("Purpose: ${booking.purpose}", color = Muted, fontSize = 11.sp)
            if (admin && booking.conflictFree) {
                Text("✓ No scheduling conflicts found (demo)", color = Positive, fontSize = 10.sp)
            }
            if (admin && booking.status == HungBookingStatus.PENDING) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(vertical = 3.dp)) {
                        Text("×  Decline", color = Negative, fontSize = 12.sp)
                    }
                    Button(onClick = onApprove, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(vertical = 3.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ActionBlue)) {
                        Text("✓  Approve", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
        Text(message, color = Muted, fontSize = 12.sp)
    }
}

@Preview(name = "All 4 screens (demo)", showBackground = true, widthDp = 393, heightDp = 830)
@Composable
private fun PreviewHungUiDemo() { HungUiDemo() }

@Preview(name = "User dashboard", showBackground = true, widthDp = 393, heightDp = 780)
@Composable
private fun PreviewUserDashboard() {
    MaterialTheme { HungUserDashboardScreen(sampleBookings().filter { it.isMine }, {}) }
}

@Preview(name = "User booking", showBackground = true, widthDp = 393, heightDp = 780)
@Composable
private fun PreviewUserBooking() {
    MaterialTheme { HungUserBookingScreen(sampleBookings().filter { it.isMine }) }
}

@Preview(name = "Admin dashboard", showBackground = true, widthDp = 393, heightDp = 780)
@Composable
private fun PreviewAdminDashboard() {
    MaterialTheme { HungAdminDashboardScreen(sampleBookings(), {}) }
}

@Preview(name = "Admin booking", showBackground = true, widthDp = 393, heightDp = 780)
@Composable
private fun PreviewAdminBooking() {
    MaterialTheme { HungAdminBookingScreen(sampleBookings(), { _, _ -> }) }
}
