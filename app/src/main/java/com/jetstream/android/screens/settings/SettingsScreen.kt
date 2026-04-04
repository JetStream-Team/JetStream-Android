package com.jetstream.android.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.rounded.ContentPasteGo
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.jetstream.android.Routes
import com.jetstream.android.ui.theme.JetStreamTheme
import com.jetstream.android.R

@Composable
fun SettingsScreen(navController: NavController) {
    Column(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_launcher_monochrome),
                contentDescription = "JetStream Settings",
                modifier = Modifier
                    .size(128.dp)
            )
        }

        HorizontalDivider()

        SettingsItem(navController, "Connection", Icons.Rounded.Wifi, Routes.SETTINGS_CONNECTION)
        SettingsItem(navController, "Notification Sync", Icons.Rounded.Notifications, Routes.SETTINGS_NOTIFICATION)
        SettingsItem(navController, "Clipboard Sync", Icons.Rounded.ContentPasteGo, Routes.SETTINGS_CLIPBOARD)
        SettingsItem(navController, "About", Icons.Rounded.Info, Routes.ABOUT)
    }
}

@Composable
fun SettingsItem(
    navController: NavController,
    label: String,
    icon: ImageVector,
    route: String
    ) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(true, onClick = { navController.navigate(route) })
            .padding(16.dp)
            .height(32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        ) {
            Row {
                Icon(
                    imageVector = icon,
                    contentDescription = "Open $label",
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.width(16.dp))

                Text(
                    label,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open $label"
            )
    }

}


@Composable
fun SwitchItem(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    JetStreamTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SettingsScreen(rememberNavController())
        }
    }
}