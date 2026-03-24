package com.jetstream.android

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class OnBoardActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            currentPage.value = 2
        }

    private val currentPage = mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isOnboardingComplete()) {
            navigateToMain()
            return
        }

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF82AAFF),
                    onPrimary = Color(0xFF00174D),
                    primaryContainer = Color(0xFF1A3473),
                    onPrimaryContainer = Color(0xFFD9E2FF),
                    secondary = Color(0xFFBFC6DC),
                    onSecondary = Color(0xFF293041),
                    secondaryContainer = Color(0xFF3F4759),
                    onSecondaryContainer = Color(0xFFDBE2F9),
                    tertiary = Color(0xFFE0BBFF),
                    onTertiary = Color(0xFF3F1662),
                    tertiaryContainer = Color(0xFF572E7A),
                    onTertiaryContainer = Color(0xFFF3DAFF),
                    surface = Color(0xFF111318),
                    onSurface = Color(0xFFE2E2E9),
                    surfaceVariant = Color(0xFF44474F),
                    onSurfaceVariant = Color(0xFFC4C6D0),
                    background = Color(0xFF111318),
                    outline = Color(0xFF8E9099),
                    outlineVariant = Color(0xFF44474F),
                )
            ) {
                OnBoardingScreen(
                    currentPage = currentPage.value,
                    onNotificationListenerRequest = { openNotificationListenerSettings() },
                    onNotificationPermissionRequest = { requestPostNotificationPermission() },
                    onFinish = { completeOnboarding() }
                )
            }
        }
    }

    private fun requestPostNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            currentPage.value = 2
        }
    }

    private fun openNotificationListenerSettings() {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    override fun onResume() {
        super.onResume()
        if (currentPage.value == 0 && isNotificationListenerEnabled()) {
            currentPage.value = 1
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners") ?: return false
        return flat.contains(packageName)
    }

    private fun isOnboardingComplete(): Boolean =
        getSharedPreferences("jetstream", Context.MODE_PRIVATE).getBoolean("onboarding_complete", false)

    private fun completeOnboarding() {
        getSharedPreferences("jetstream", Context.MODE_PRIVATE).edit()
            .putBoolean("onboarding_complete", true).apply()
        navigateToMain()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

@Composable
fun OnBoardingScreen(
    currentPage: Int,
    onNotificationListenerRequest: () -> Unit,
    onNotificationPermissionRequest: () -> Unit,
    onFinish: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .size(500.dp)
                    .offset(x = (-100).dp, y = (-150).dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                colorScheme.primary.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(400.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 100.dp, y = 100.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                colorScheme.tertiary.copy(alpha = 0.07f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                AnimatedContent(
                    targetState = currentPage,
                    transitionSpec = {
                        (slideInHorizontally { it } + fadeIn()) togetherWith
                                (slideOutHorizontally { -it } + fadeOut())
                    },
                    label = "page_content",
                    modifier = Modifier.weight(1f)
                ) { page ->
                    when (page) {
                        0 -> NotificationListenerPage()
                        1 -> NotificationPermissionPage()
                        else -> AllSetPage()
                    }
                }

                Column(
                    modifier = Modifier.padding(bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { (currentPage + 1) / 3f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = colorScheme.primary,
                        trackColor = colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        strokeCap = StrokeCap.Round,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    FilledTonalButton(
                        onClick = {
                            when (currentPage) {
                                0 -> onNotificationListenerRequest()
                                1 -> onNotificationPermissionRequest()
                                2 -> onFinish()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = colorScheme.primaryContainer,
                            contentColor = colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text(
                            text = when (currentPage) {
                                0 -> "Open Notification Settings"
                                1 -> "Allow Notifications"
                                else -> "Start Using JetStream"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    if (currentPage < 2) {
                        TextButton(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Skip for now",
                                color = colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationListenerPage() {
    val colorScheme = MaterialTheme.colorScheme
    val infiniteTransition = rememberInfiniteTransition(label = "icon_anim")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "icon_scale"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .size(88.dp)
                .scale(iconScale)
                .background(
                    colorScheme.primaryContainer,
                    RoundedCornerShape(28.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("🔔", fontSize = 40.sp)
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Notification\nAccess",
                style = MaterialTheme.typography.displaySmall,
                color = colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 40.sp
            )
            Text(
                text = "JetStream reads your notifications to mirror them across all your connected devices instantly.",
                style = MaterialTheme.typography.bodyLarge,
                color = colorScheme.onSurfaceVariant,
                lineHeight = 26.sp
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FeatureCard(
                icon = "⚡",
                title = "Real-time sync",
                description = "Notifications appear on all devices instantly"
            )
            FeatureCard(
                icon = "🔒",
                title = "Stays on your network",
                description = "Data never leaves your local connection"
            )
            FeatureCard(
                icon = "📱",
                title = "Every app",
                description = "Works with all apps — including system ones"
            )
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text("ℹ️", fontSize = 16.sp)
                Text(
                    text = "You'll be redirected to Android Settings. Find JetStream in the list and enable it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onTertiaryContainer,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun NotificationPermissionPage() {
    val colorScheme = MaterialTheme.colorScheme
    val infiniteTransition = rememberInfiniteTransition(label = "bell_anim")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(1000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "bell_scale"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .size(88.dp)
                .scale(iconScale)
                .background(
                    colorScheme.tertiaryContainer,
                    RoundedCornerShape(28.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("📣", fontSize = 40.sp)
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Send\nNotifications",
                style = MaterialTheme.typography.displaySmall,
                color = colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 40.sp
            )
            Text(
                text = "Allow JetStream to keep you informed about connection status, incoming data, and sync events.",
                style = MaterialTheme.typography.bodyLarge,
                color = colorScheme.onSurfaceVariant,
                lineHeight = 26.sp
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FeatureCard(
                icon = "🔗",
                title = "Connection status",
                description = "Alerts when devices connect or drop"
            )
            FeatureCard(
                icon = "📋",
                title = "Clipboard activity",
                description = "Notified when clipboard data arrives"
            )
            FeatureCard(
                icon = "🛡️",
                title = "No spam, ever",
                description = "Only critical service updates — nothing else"
            )
        }
    }
}

@Composable
fun AllSetPage() {
    val colorScheme = MaterialTheme.colorScheme
    val infiniteTransition = rememberInfiniteTransition(label = "done_anim")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "done_scale"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(scale)
                .background(colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✓",
                fontSize = 64.sp,
                color = colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "You're all set!",
            style = MaterialTheme.typography.displaySmall,
            color = colorScheme.onBackground,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "JetStream is ready. Connect to your server and start syncing across all your devices.",
            style = MaterialTheme.typography.bodyLarge,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("🔔", "📋", "🔗").forEach { emoji ->
                Surface(
                    shape = CircleShape,
                    color = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(emoji, fontSize = 22.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureCard(icon: String, title: String, description: String) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(icon, fontSize = 22.sp)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}