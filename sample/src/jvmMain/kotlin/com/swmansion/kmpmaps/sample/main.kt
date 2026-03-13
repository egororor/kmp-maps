package com.swmansion.kmpmaps.sample

import androidx.compose.material.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.datlag.kcef.KCEF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun main() = application {
    Window(title = "KMP Maps - Desktop", onCloseRequest = ::exitApplication) {
        var initialized by remember { mutableStateOf(false) }
        var restartRequired by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                KCEF.init(
                    builder = {
                        // When running with JetBrains Runtime (JBR), JCEF is bundled
                        // and no download/install directory is needed
                        progress { onInitialized { initialized = true } }
                        settings { noSandbox = true }
                    },
                    onError = { it?.printStackTrace() },
                    onRestartRequired = { restartRequired = true },
                )
            }
        }

        if (restartRequired) {
            Text("Restart required to complete initialization.")
        } else if (initialized) {
            App()
        } else {
            Text("Initializing Map Engine...")
        }

        DisposableEffect(Unit) {
            onDispose {
                KCEF.disposeBlocking()
            }
        }
    }
}
