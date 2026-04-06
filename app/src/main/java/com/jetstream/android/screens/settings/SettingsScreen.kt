package com.jetstream.android.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.rounded.ContentPasteGo
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.jetstream.android.R
import com.jetstream.android.Routes
import com.jetstream.android.ui.theme.JetStreamTheme

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
            .padding(24.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun StandardPageHeader(
    title: String,
    onBack: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
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