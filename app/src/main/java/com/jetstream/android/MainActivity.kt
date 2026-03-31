package com.jetstream.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import com.jetstream.android.ui.theme.JetStreamTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jetstream.android.screens.home.HomeScreen
import com.jetstream.android.screens.settings.ClipboardSettingsScreen
import com.jetstream.android.screens.settings.ConnectionSettingsScreen
import com.jetstream.android.screens.settings.NotificationSettingsScreen
import com.jetstream.android.screens.settings.SettingsScreen
import com.jetstream.android.service.JetStreamService

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val SETTINGS_CONNECTION = "settings/connection"
    const val SETTINGS_NOTIFICATION = "settings/notification"
    const val SETTINGS_CLIPBOARD = "settings/clipboard"
    const val ABOUT = "settings/about"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startForegroundService(Intent(this, JetStreamService::class.java))
        setContent {
            JetStreamTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        AppNavigation()
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300, easing = FastOutSlowInEasing),
            )
        },
        exitTransition = {
            fadeOut(tween(300, easing = FastOutSlowInEasing))
        },
        popEnterTransition = {
            fadeIn(tween(300, easing = FastOutSlowInEasing))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300, easing = FastOutSlowInEasing),
            )
        },
    ) {
        composable(Routes.HOME) { HomeScreen(navController) }
        composable(Routes.SETTINGS) { SettingsScreen(navController) }
        composable(Routes.SETTINGS_CONNECTION) { ConnectionSettingsScreen(navController) }
        composable(Routes.SETTINGS_NOTIFICATION) { NotificationSettingsScreen(navController) }
        composable(Routes.SETTINGS_CLIPBOARD) { ClipboardSettingsScreen(navController) }
    }
}

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    JetStreamTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            AppNavigation()
        }
    }
}