package com.swmansion.kmpmaps.sample

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MapsScreen(
    map: @Composable (Modifier, onSettingsClick: () -> Unit, settingsExpanded: Boolean) -> Unit,
    controls: @Composable () -> Unit,
) {
    if (isJvm()) {
        var expanded by remember { mutableStateOf(false) }
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val collapsedHeight = 0.dp
            val handleHeight = 28.dp
            val maxExpandedHeight = (maxHeight - 120.dp).coerceAtLeast(handleHeight)
            val expandedHeight = (maxHeight * 0.6f).coerceAtMost(maxExpandedHeight)
            val sheetHeight by animateDpAsState(
                targetValue = if (expanded) expandedHeight else collapsedHeight,
                label = "controlsSheetHeight",
            )

            Column(Modifier.fillMaxSize()) {
                Column(Modifier.weight(1f).fillMaxWidth()) {
                    map(Modifier.fillMaxSize(), { expanded = !expanded }, expanded)
                }
                if (expanded) {
                    Surface(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(sheetHeight),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        tonalElevation = 6.dp,
                    ) {
                        Column(Modifier.fillMaxSize().clipToBounds()) {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(handleHeight)
                                        .clickable { expanded = false },
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier =
                                        Modifier
                                            .width(40.dp)
                                            .height(4.dp)
                                            .background(
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                                                RoundedCornerShape(2.dp),
                                            )
                                )
                            }
                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState())
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalAlignment = Alignment.Start,
                            ) {
                                controls()
                            }
                        }
                    }
                }
            }
        }
    } else {
        val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var showBottomSheet by remember { mutableStateOf(false) }

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { showBottomSheet = true }) {
                    Icon(imageVector = Icons.Filled.Settings, contentDescription = "Settings")
                }
            }
        ) {
            map(Modifier.fillMaxSize(), { showBottomSheet = true }, false)
        }
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = bottomSheetState,
            ) {
                Column(
                    Modifier.fillMaxWidth()
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.exclude(WindowInsets.statusBars)
                        )
                        .padding(vertical = 16.dp),
                    Arrangement.spacedBy(8.dp),
                    Alignment.CenterHorizontally,
                ) {
                    controls()
                }
            }
        }
    }
}
