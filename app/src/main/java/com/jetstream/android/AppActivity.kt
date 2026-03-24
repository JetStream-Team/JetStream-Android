package com.jetstream.android

import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// --- 1. DYNAMIC THEME ---

@Composable
fun MaterialLinkTheme(
    darkTheme: Boolean = true, // default to dark; flip as needed
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    // Dynamic color is supported on Android 12+ (API 31+)
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context)
            else           dynamicLightColorScheme(context)
        }
        // Fallback for older Android versions
        darkTheme -> darkColorScheme()
        else      -> lightColorScheme()
    }

    // Sync status bar color with the top app bar surface
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content     = content
    )
}

// --- 2. THE MAIN ACTIVITY ---

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialLinkTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = MaterialTheme.colorScheme.background
                ) {
                    MaterialLinkMainApp()
                }
            }
        }
    }
}

// --- 3. THE MAIN APP WRAPPER ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialLinkMainApp() {
    var currentRoute by remember { mutableStateOf("dashboard") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MaterialLinkTopAppBar(onProfileClick = { })
        },
        bottomBar = {
            MaterialLinkBottomBar(
                currentRoute = currentRoute,
                onNavigate   = { newRoute -> currentRoute = newRoute }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentRoute) {
                "dashboard" -> DashboardScreen()
                "devices"   -> DiscoveryScreen()
                "history"   -> HistoryScreen()
            }
        }
    }
}

// --- 4. SHARED COMPONENTS & MODELS ---

data class ActivityItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val timestamp: String,
    val type: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialLinkTopAppBar(onProfileClick: () -> Unit) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor      = MaterialTheme.colorScheme.surface,
            titleContentColor   = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.SettingsInputAntenna,
                    contentDescription = null,
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Material Link",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        actions = {
            IconButton(onClick = onProfileClick) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    modifier = Modifier.size(32.dp),
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

@Composable
fun MaterialLinkBottomBar(currentRoute: String, onNavigate: (String) -> Unit) {
    NavigationBar(
        modifier       = Modifier.clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        val items = listOf(
            Triple("Dashboard", Icons.Default.Dashboard, "dashboard"),
            Triple("Devices",   Icons.Default.Devices,   "devices"),
            Triple("History",   Icons.Default.History,   "history")
        )
        items.forEach { (label, icon, route) ->
            val isSelected = currentRoute == route
            NavigationBarItem(
                selected = isSelected,
                onClick  = { onNavigate(route) },
                icon     = { Icon(icon, contentDescription = null) },
                label    = { Text(label) },
                colors   = NavigationBarItemDefaults.colors(
                    selectedIconColor       = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor       = MaterialTheme.colorScheme.onSecondaryContainer,
                    indicatorColor          = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor     = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor     = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

// --- 5. DASHBOARD SCREEN ---

@Composable
fun DashboardScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        // Hero connection card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Status badge
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(8.dp).background(Color.Green, CircleShape))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "CONNECTED",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "Connected to\nDesktop-PC",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Laptop,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint     = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "192.168.1.15",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Text(
            "Quick Actions",
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            QuickActionCard(Icons.Default.Upload,       "Send File",      Modifier.weight(1f))
            QuickActionCard(Icons.Default.ContentPaste, "Send Clipboard", Modifier.weight(1f))
        }
    }
}

@Composable
fun QuickActionCard(icon: ImageVector, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.aspectRatio(1f),
        shape    = RoundedCornerShape(32.dp),
        color    = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(
            modifier            = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint     = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.height(12.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

// --- 6. DEVICE DISCOVERY SCREEN ---

@Composable
fun DiscoveryScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            "Discovering Devices",
            style      = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onBackground
        )

        Box(
            modifier        = Modifier.fillMaxWidth().aspectRatio(1.2f),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Bluetooth,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint     = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            "Nearby Devices",
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(16.dp))

        DeviceItem("Work Laptop",     "Windows 11 • Local Network")
        DeviceItem("Media Center PC", "Ubuntu 22.04 • Bluetooth")
    }
}

@Composable
fun DeviceItem(name: String, details: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape    = RoundedCornerShape(24.dp),
        color    = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Computer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    name,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    details,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Button(
                onClick = { },
                shape   = RoundedCornerShape(12.dp),
                colors  = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor   = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Pair", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- 7. HISTORY SCREEN ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            "History",
            style      = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = true,
                onClick  = {},
                label    = { Text("All") },
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    selectedLabelColor     = MaterialTheme.colorScheme.onTertiaryContainer
                )
            )
            FilterChip(
                selected = false,
                onClick  = {},
                label    = { Text("Files") },
                colors   = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor     = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            FilterChip(
                selected = false,
                onClick  = {},
                label    = { Text("Links") },
                colors   = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor     = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding      = PaddingValues(vertical = 16.dp)
        ) {
            items(sampleHistory) { item ->
                HistoryCard(item)
            }
        }
    }
}

@Composable
fun HistoryCard(item: ActivityItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(24.dp),
        color    = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(
                item.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    item.title,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${item.timestamp} • ${item.subtitle}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {}) {
                        Text("Open",   color = MaterialTheme.colorScheme.tertiary)
                    }
                    TextButton(onClick = {}) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

val sampleHistory = listOf(
    ActivityItem("1", "Document.pdf",        "4.2 MB",   Icons.Default.Description, "2 mins ago",  "File"),
    ActivityItem("2", "https://example.com", "Web Link", Icons.Default.Link,        "15 mins ago", "Link")
)