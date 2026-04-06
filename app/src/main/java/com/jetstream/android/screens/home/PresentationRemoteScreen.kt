package com.jetstream.android.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.PresentToAll
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jetstream.android.screens.settings.StandardPageHeader
import com.jetstream.android.ui.theme.JetStreamTheme

@Composable
fun PresentationRemoteScreen(navController: NavController) {
    val viewModel: PresentationRemoteScreenViewModel = viewModel()
    PresentationRemoteContent(
        onBack = { navController.popBackStack() },
        onPrevious = { viewModel.sendPresentationPrevious() },
        onNext = { viewModel.sendPresentationNext() },
        onPresent = { viewModel.sendPresentationPresent() },
        onFullscreen = { viewModel.sendPresentationFullscreen() },
        onVisibility = { viewModel.sendPresentationVisibility() }
    )
}

@Composable
fun PresentationRemoteContent(
    onBack: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPresent: () -> Unit,
    onFullscreen: () -> Unit,
    onVisibility: () -> Unit
) {
    val lowRounding = 42.dp
    val highRounding = 100.dp
    val spacing = 8.dp
    val iconSize = 52.dp
    Column {
        StandardPageHeader("Presentation Remote", onBack)
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(highRounding, lowRounding, lowRounding, highRounding),
                    onClick = onPrevious
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronLeft,
                        contentDescription = "Previous Slide",
                        modifier = Modifier.size(iconSize)
                    )
                }
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(lowRounding, highRounding,  highRounding, lowRounding),
                    onClick = onNext
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = "Next Slide",
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                ElevatedButton (
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(lowRounding),
                    onClick = onPresent
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PresentToAll,
                        contentDescription = "Present to All",
                        modifier = Modifier.size(iconSize)
                    )
                }
                ElevatedButton (
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(lowRounding),
                    onClick = onFullscreen
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Fullscreen,
                        contentDescription = "Fullscreen",
                        modifier = Modifier.size(iconSize)
                    )
                }
                ElevatedButton (
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(lowRounding),
                    onClick = onVisibility
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Contrast,
                        contentDescription = "Hide/Unhide Presentation",
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PresentationRemoteScreenPreview() {
    JetStreamTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            PresentationRemoteContent(
                onBack = { },
                onPrevious = { },
                onNext = { },
                onPresent = { },
                onFullscreen = { },
                onVisibility = { }
            )
        }
    }
}