package com.example.smart_campus.ui.hung

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal object AdminColors {
    val Teal = Color(0xFF166F73)
    val TealDeep = Color(0xFF164F55)
    val Blue = Color(0xFF2861EF)
    val Background = Color(0xFFF3F5F7)
    val Text = Color(0xFF192638)
    val Muted = Color(0xFF7E8B9D)
    val Amber = Color(0xFFD08A16)
    val Green = Color(0xFF15966D)
    val Red = Color(0xFFD04F5C)
    val Line = Color(0xFFE7EBF2)
}

internal enum class CampusIconType {
    BUILDING, CALENDAR, CLOCK, USERS, GRID, PERSON, SEARCH, SPARKLE, CHEVRON,
    PENCIL, TRASH, CLOUD_UPLOAD, CARET_DOWN
}

@Composable
internal fun CampusVectorIcon(
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
            CampusIconType.CARET_DOWN -> linePath(6f to 9f, 12f to 15f, 18f to 9f)
            CampusIconType.PENCIL -> {
                linePath(5f to 16.8f, 4f to 21f, 8.2f to 20f, 20f to 8.2f, 16f to 4.2f, 5f to 16.8f)
                linePath(14.7f to 5.6f, 18.4f to 9.3f)
            }
            CampusIconType.TRASH -> {
                rect(6f, 7f, 12f, 15f, 1.1f)
                ln(4f, 5f, 20f, 5f)
                ln(10f, 3f, 14f, 3f)
                ln(10f, 10f, 10f, 18f)
                ln(14f, 10f, 14f, 18f)
            }
            CampusIconType.CLOUD_UPLOAD -> {
                val outline = Path().apply {
                    moveTo(6f*k,18f*k)
                    cubicTo(2f*k,17f*k,2f*k,11f*k,6.3f*k,10.3f*k)
                    cubicTo(7f*k,3f*k,17f*k,3f*k,18.6f*k,9.9f*k)
                    cubicTo(23f*k,10f*k,23f*k,17f*k,18f*k,18f*k)
                }
                drawPath(outline, tint, style = pen)
                linePath(12f to 20f, 12f to 12f, 9f to 15f)
                linePath(12f to 12f, 15f to 15f)
            }
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
internal fun AdminHeader(title: String, subtitle: String, onBack: (() -> Unit)? = null) {
    Column(Modifier.fillMaxWidth().background(AdminColors.Teal).padding(start = 16.dp, end = 16.dp, top = 17.dp, bottom = 19.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                Box(Modifier.size(30.dp).clip(CircleShape).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
                    CampusVectorIcon(CampusIconType.CHEVRON, Color.White, Modifier.size(19.dp).rotate(180f))
                }
                Spacer(Modifier.width(7.dp))
            }
            Box(Modifier.size(30.dp).background(Color.White, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                CampusVectorIcon(CampusIconType.BUILDING, AdminColors.TealDeep, Modifier.size(19.dp))
            }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text("CAMPUS", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 11.sp)
                Text("FACILITY BOOKING", color = Color.White.copy(alpha = .88f), fontSize = 8.sp, lineHeight = 10.sp)
            }
            Surface(color = Color(0xFF06191C), shape = CircleShape) {
                Text("ADMIN", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 6.dp))
            }
            Spacer(Modifier.width(9.dp))
            Box(Modifier.size(33.dp).background(AdminColors.TealDeep, CircleShape), contentAlignment = Alignment.Center) {
                NotificationBellIcon()
                Box(Modifier.align(Alignment.TopEnd).size(6.dp).background(Color(0xFF64D2D1), CircleShape))
            }
        }
        Spacer(Modifier.height(17.dp))
        Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Color.White.copy(alpha = .75f), fontSize = 11.sp)
    }
}

@Composable
internal fun AdminBottomBar(selected: AdminTab, onSelect: (AdminTab) -> Unit) {
    Surface(shadowElevation = 9.dp, color = Color.White) {
        Row(Modifier.fillMaxWidth().height(62.dp), horizontalArrangement = Arrangement.SpaceAround) {
            val items = listOf(
                Triple(AdminTab.HOME, "Home", CampusIconType.GRID),
                Triple(AdminTab.BOOKING, "Booking", CampusIconType.CALENDAR),
                Triple(AdminTab.FACILITIES, "Facilities", CampusIconType.BUILDING),
                Triple(AdminTab.PROFILE, "Profile", CampusIconType.PERSON)
            )
            items.forEach { (tab, label, symbol) ->
                val active = selected == tab
                val tint = if (active) AdminColors.Blue else Color(0xFF99A7B8)
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight().clickable { onSelect(tab) }.padding(top = 7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    if (active) Box(Modifier.width(42.dp).height(3.dp).background(AdminColors.Blue))
                    else Spacer(Modifier.height(3.dp))
                    CampusVectorIcon(symbol, tint, Modifier.size(20.dp))
                    Text(label, color = tint, fontSize = 10.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}

@Composable
internal fun SectionHeading(title: String, trailing: String? = null, onTrailing: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AdminColors.Text)
        if (trailing != null && onTrailing != null) {
            Text(trailing, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AdminColors.Blue,
                modifier = Modifier.clickable(onClick = onTrailing).padding(5.dp))
        }
    }
}

@Composable
internal fun StatCard(number: String, label: String, icon: CampusIconType, accent: Color, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.fillMaxWidth().padding(9.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            CampusVectorIcon(icon, accent, Modifier.size(18.dp))
            Text(number, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = AdminColors.Text)
            Text(label, fontSize = 9.sp, color = AdminColors.Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
internal fun StatusPill(status: BookingStatus) {
    val (name, tint) = when (status) {
        BookingStatus.PENDING -> "Pending" to AdminColors.Amber
        BookingStatus.APPROVED -> "Approved" to AdminColors.Green
        BookingStatus.REJECTED -> "Rejected" to AdminColors.Red
        BookingStatus.CANCELLED -> "Cancelled" to AdminColors.Muted
    }
    Surface(shape = CircleShape, color = tint.copy(alpha = .13f)) {
        Text(name, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = tint)
    }
}

@Composable
internal fun FilledPrimaryButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(onClick = onClick, modifier = modifier, enabled = enabled,
        shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = AdminColors.Blue)) {
        Text(label, fontSize = 12.sp)
    }
}

@Composable
internal fun LabelValue(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = AdminColors.Muted, fontSize = 11.sp)
        Text(value, color = AdminColors.Text, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
internal fun AdminInfoTile(
    title: String, subtitle: String, symbol: CampusIconType, action: String, onClick: () -> Unit
) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(35.dp).background(Color(0xFFE9F0FF), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                CampusVectorIcon(symbol, AdminColors.Blue, Modifier.size(21.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = AdminColors.Text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = AdminColors.Muted, fontSize = 10.sp)
            }
            Text(action, color = AdminColors.Blue, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}


/** Figma admin-facilities / add facility: centered compact header with back + Admin badge. */
@Composable
internal fun AdminCompactHeader(title: String, onBack: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().height(66.dp).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                CampusVectorIcon(CampusIconType.CHEVRON, Color.White, Modifier.size(20.dp).rotate(180f))
            }
        } else {
            // Facilities is a root tab: keep spacing, do not render a back arrow.
            Spacer(Modifier.size(32.dp))
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
        }
        Row(
            modifier = Modifier.background(AdminColors.TealDeep, RoundedCornerShape(18.dp))
                .padding(start = 4.dp, end = 8.dp, top = 3.dp, bottom = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(23.dp).background(Color(0xFF0F3036), CircleShape),
                contentAlignment = Alignment.Center) {
                CampusVectorIcon(CampusIconType.PERSON, Color.White, Modifier.size(15.dp))
            }
            Spacer(Modifier.width(5.dp))
            Text("Admin", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Medium)
        }
    }
}

/** Dense Figma-style input; the label is rendered separately when used in forms. */
@Composable
internal fun CompactInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 39.dp,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    leading: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth().height(height),
        shape = RoundedCornerShape(8.dp), color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE1E7EF))
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 5.dp),
            verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            if (leading != null) leading()
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                textStyle = TextStyle(color = AdminColors.Text, fontSize = 12.sp),
                singleLine = singleLine,
                keyboardOptions = keyboardOptions,
                decorationBox = { innerField ->
                    Box(contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart) {
                        if (value.isEmpty()) Text(placeholder, color = Color(0xFF9EAABA),
                            fontSize = 11.sp, lineHeight = 15.sp)
                        innerField()
                    }
                }
            )
        }
    }
}

@Composable
internal fun CompactDropdown(
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(39.dp).clickable { expanded = true },
            shape = RoundedCornerShape(8.dp), color = Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, Color(0xFFE1E7EF))
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text(selected, fontSize = 11.sp, color = Color(0xFF596B82),
                    modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                CampusVectorIcon(CampusIconType.CARET_DOWN, Color(0xFF66788B), Modifier.size(15.dp))
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option, fontSize = 12.sp) }, onClick = {
                    onSelect(option)
                    expanded = false
                })
            }
        }
    }
}

/** Standalone Previews receive the same persistent tab bar as the actual Admin Navigation. */
@Composable
internal fun AdminStandalonePreview(tab: AdminTab, content: @Composable () -> Unit) {
    Scaffold(containerColor = AdminColors.Background,
        bottomBar = { AdminBottomBar(selected = tab, onSelect = {}) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) { content() }
    }
}
