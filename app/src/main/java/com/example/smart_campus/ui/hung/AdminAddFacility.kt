package com.example.smart_campus.ui.hung

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Figma admin-facilities-add: mock UI, saves only into parent Compose state, no backend. */
@Composable
fun AdminAddFacilityScreen(
    onBack: () -> Unit,
    onSave: (AdminFacility) -> Unit,
    modifier: Modifier = Modifier,
    initialFacility: AdminFacility? = null
) {
    var name by remember(initialFacility?.id) { mutableStateOf(initialFacility?.name.orEmpty()) }
    var type by remember(initialFacility?.id) { mutableStateOf(initialFacility?.type.orEmpty()) }
    var location by remember(initialFacility?.id) { mutableStateOf(initialFacility?.location.orEmpty()) }
    var capacity by remember(initialFacility?.id) { mutableStateOf(initialFacility?.capacity?.toString().orEmpty()) }
    var description by remember(initialFacility?.id) { mutableStateOf(initialFacility?.description.orEmpty()) }
    var active by remember(initialFacility?.id) { mutableStateOf(initialFacility?.operationalStatus != FacilityStatus.UNAVAILABLE) }
    var unavailableReason by remember(initialFacility?.id) { mutableStateOf(initialFacility?.unavailableReason ?: "MAINTENANCE") }
    var unavailableNote by remember(initialFacility?.id) { mutableStateOf(initialFacility?.unavailableNote.orEmpty()) }
    val selectedEquipment = remember(initialFacility?.id) { mutableStateListOf<String>() }
    var showError by remember { mutableStateOf(false) }
    val capacityInt = capacity.toIntOrNull()
    val valid = name.isNotBlank() && type.isNotBlank() && location.isNotBlank() &&
        capacityInt != null && capacityInt > 0 &&
        (active || unavailableReason != "OTHER" || unavailableNote.isNotBlank())

    Column(modifier.fillMaxSize().background(AdminColors.Teal)) {
        AdminCompactHeader(
            title = if (initialFacility == null) "Add New Facility" else "Edit Facility",
            onBack = onBack
        )
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = Color.White
        ) {
            Column(Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FacilityFieldLabel("Facility Name")
                    CompactInput(name, { name = it }, "e.g Physics Lab 1")
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            FacilityFieldLabel("Category")
                            CompactDropdown(
                                selected = type.ifBlank { "Select category" },
                                options = listOf("Lecture Hall", "Study Room", "Meeting Room", "Computer Lab", "Physics Lab"),
                                onSelect = { type = it }
                            )
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            FacilityFieldLabel("Capacity")
                            CompactInput(capacity, { capacity = it.filter(Char::isDigit) }, "e.g 50",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        }
                    }
                    FacilityFieldLabel("Location")
                    CompactInput(location, { location = it }, "e.g Building B, Floor 1")
                    FacilityFieldLabel("Description")
                    CompactInput(description, { description = it }, "Enter facility details, hours, instructions...",
                        height = 72.dp, singleLine = false)
                    FacilityFieldLabel("Equipment")
                    // Only a Figma UI detail; equipment is not stored in the agreed 3-table proposal.
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        listOf("Projector", "Whiteboard", "AC", "Wi-Fi", "Podium").forEach { equipment ->
                            val checked = equipment in selectedEquipment
                            Surface(
                                shape = RoundedCornerShape(5.dp),
                                color = if (checked) Color(0xFFE4ECFF) else Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AdminColors.Blue),
                                modifier = Modifier.clickable {
                                    if (checked) selectedEquipment.remove(equipment) else selectedEquipment.add(equipment)
                                }
                            ) { Text(equipment, modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                fontSize = 10.sp, color = AdminColors.Blue) }
                        }
                    }
                    FacilityFieldLabel("Facility Image")
                    Box(
                        modifier = Modifier.fillMaxWidth().height(80.dp)
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                            .drawBehind {
                                drawRoundRect(
                                    color = Color(0xFFD7E0EC),
                                    style = Stroke(
                                        width = 1.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f, 7f))
                                    ),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx())
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            CampusVectorIcon(CampusIconType.CLOUD_UPLOAD, Color(0xFF94A4B4), Modifier.size(23.dp))
                            Text("Upload image", fontSize = 11.sp, color = AdminColors.Muted)
                        }
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        FacilityFieldLabel("Status")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (active) "Active" else "Unavailable", fontSize = 11.sp, color = AdminColors.Muted)
                            Spacer(Modifier.width(7.dp))
                            Switch(checked = active, onCheckedChange = { active = it },
                                modifier = Modifier.height(30.dp),
                                colors = SwitchDefaults.colors(checkedTrackColor = AdminColors.Blue))
                        }
                    }
                    if (!active) {
                        FacilityFieldLabel("Unavailable reason")
                        CompactDropdown(unavailableReason,
                            listOf("CLEANING", "MAINTENANCE", "OTHER"), { unavailableReason = it })
                        if (unavailableReason == "OTHER") {
                            FacilityFieldLabel("Reason note (required)")
                            CompactInput(unavailableNote, { unavailableNote = it }, "Why is this facility unavailable?")
                        }
                    }
                    if (showError && !valid) Text("Please fill in required fields and a valid capacity.",
                        color = AdminColors.Red, fontSize = 11.sp)
                }
                HorizontalDivider(color = Color(0xFFF0F2F5))
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AdminColors.Text),
                        contentPadding = PaddingValues(0.dp)) { Text("Cancel", fontSize = 12.sp) }
                    Button(onClick = {
                        showError = true
                        if (valid) onSave(
                            AdminFacility(
                                id = initialFacility?.id ?: 0,
                                name = name.trim(), type = type.trim(), location = location.trim(),
                                capacity = capacityInt ?: 0, description = description.trim(),
                                accessPolicy = initialFacility?.accessPolicy ?: AccessPolicy.ALL,
                                operationalStatus = if (active) FacilityStatus.ACTIVE else FacilityStatus.UNAVAILABLE,
                                unavailableReason = if (active) null else unavailableReason,
                                unavailableNote = if (!active && unavailableReason == "OTHER") unavailableNote.trim() else null
                            )
                        )
                    }, modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(25.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AdminColors.Blue)
                    ) { Text(if (initialFacility == null) "Save Facility" else "Save Changes", fontSize = 12.sp) }
                }
            }
        }
    }
}

@Composable
private fun FacilityFieldLabel(text: String) {
    Text(text, color = Color(0xFF4E6075), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
}

@Preview(name = "Admin Add Facility · Figma + taskbar", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun AdminAddFacilityPreview() {
    MaterialTheme {
        AdminStandalonePreview(AdminTab.FACILITIES) {
            AdminAddFacilityScreen(onBack = {}, onSave = {})
        }
    }
}
