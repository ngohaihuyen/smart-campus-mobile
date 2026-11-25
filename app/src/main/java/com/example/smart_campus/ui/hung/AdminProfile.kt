package com.example.smart_campus.ui.hung

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Leader yêu cầu file Profile; chưa có frame Admin Profile trong Figma, chỉ để khung tối thiểu. */
@Composable
fun AdminProfileScreen(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().background(AdminColors.Background), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AdminHeader("Profile", "Administrator account")
        Card(Modifier.fillMaxWidth().padding(horizontal = 14.dp), shape = RoundedCornerShape(13.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Administrator", color = AdminColors.Text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Role: ADMIN", color = AdminColors.Teal, fontSize = 12.sp)
                HorizontalDivider(color = AdminColors.Line)
                Text("This is a navigation placeholder. The Admin Profile design has not been provided in Figma.",
                    color = AdminColors.Muted, fontSize = 12.sp)
                Text("No account editing or authentication functions are implemented here.",
                    color = AdminColors.Muted, fontSize = 11.sp)
            }
        }
    }
}

@Preview(name = "Admin Profile placeholder", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun AdminProfilePreview() { MaterialTheme { AdminStandalonePreview(AdminTab.PROFILE) { AdminProfileScreen() } } }
