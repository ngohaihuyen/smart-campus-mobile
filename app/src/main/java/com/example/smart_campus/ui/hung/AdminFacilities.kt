package com.example.smart_campus.ui.hung

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_campus.R

/** Figma admin-facilities; photo is a low-resolution crop from the supplied Figma screenshot. */
@Composable
fun AdminFacilitiesScreen(
    facilities: List<AdminFacility>,
    onAddFacility: () -> Unit,
    modifier: Modifier = Modifier,
    onEditFacility: (AdminFacility) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("All Categories") }
    var status by remember { mutableStateOf("Active Status") }
    val categories = listOf("All Categories") + facilities.map { it.type }.distinct()
    val displayed = facilities.filter { item ->
        (category == "All Categories" || item.type == category) &&
            (status == "Active Status" ||
                (status == "Active" && item.operationalStatus == FacilityStatus.ACTIVE) ||
                (status == "Unavailable" && item.operationalStatus == FacilityStatus.UNAVAILABLE)) &&
            (query.isBlank() || listOf(item.name, item.type, item.location).any {
                it.contains(query.trim(), ignoreCase = true)
            })
    }

    Column(modifier.fillMaxSize().background(AdminColors.Background)) {
        // Figma: compact teal header, search/filter card overlapping the header.
        Box(Modifier.fillMaxWidth().height(230.dp)) {
            Box(Modifier.fillMaxWidth().height(142.dp).background(AdminColors.Teal)) {
                AdminCompactHeader("Facilities Management")
            }
            Card(
                modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 16.dp).fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    CompactInput(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = "Search name, location, equipment, ...",
                        leading = { CampusVectorIcon(CampusIconType.SEARCH, AdminColors.Muted, Modifier.size(16.dp)) }
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CompactDropdown(
                            selected = category,
                            options = categories,
                            onSelect = { category = it },
                            modifier = Modifier.weight(1f)
                        )
                        CompactDropdown(
                            selected = status,
                            options = listOf("Active Status", "Active", "Unavailable"),
                            onSelect = { status = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Button(
                        onClick = onAddFacility,
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        shape = RoundedCornerShape(22.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102752))
                    ) { Text("+ Add New Facility", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (displayed.isEmpty()) item {
                Text("No facilities found", modifier = Modifier.padding(16.dp), color = AdminColors.Muted)
            }
            items(displayed, key = { it.id }) { facility ->
                FacilityFigmaCard(facility = facility, onEdit = { onEditFacility(facility) })
            }
        }
    }
}

@Composable
private fun FacilityFigmaCard(facility: AdminFacility, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.hung_lecture_hall_preview),
                contentDescription = "Lecture hall sample image from Figma",
                modifier = Modifier.size(58.dp).clip(RoundedCornerShape(9.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(facility.name, fontSize = 13.sp, color = AdminColors.Text,
                    fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${facility.type} · ${facility.capacity} people", fontSize = 10.sp,
                    color = AdminColors.Muted, maxLines = 1)
                Text(facility.location, fontSize = 10.sp, color = AdminColors.Muted, maxLines = 1)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val active = facility.operationalStatus == FacilityStatus.ACTIVE
                Surface(color = if (active) Color(0xFFD9FBEA) else Color(0xFFFFF0DA), shape = CircleShape) {
                    Text(if (active) "ACTIVE" else "UNAVAILABLE", fontSize = 8.sp,
                        color = if (active) Color(0xFF119369) else Color(0xFFC0811F),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.size(29.dp).clip(CircleShape).clickable(onClick = onEdit),
                        contentAlignment = Alignment.Center) {
                        CampusVectorIcon(CampusIconType.PENCIL, Color(0xFF66778B), Modifier.size(18.dp))
                    }
                    // Delete appears in Figma but is intentionally disabled: no DELETE Facility API in proposal.
                    Box(Modifier.size(29.dp), contentAlignment = Alignment.Center) {
                        CampusVectorIcon(CampusIconType.TRASH, Color(0xFFCB5C65), Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Preview(name = "Admin Facilities - rieng", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun AdminFacilitiesPreview() {
    // Chi preview man Facilities trong file nay; khong goi AdminNavigation.
    MaterialTheme {
        AdminStandalonePreview(AdminTab.FACILITIES) {
            AdminFacilitiesScreen(
                facilities = exampleFacilities(),
                onAddFacility = {},
                onEditFacility = {}
            )
        }
    }
}
