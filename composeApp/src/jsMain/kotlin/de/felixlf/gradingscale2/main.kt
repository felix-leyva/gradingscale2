@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import androidx.compose.ui.window.ComposeViewport
import de.felixlf.gradingscale2.di.koinSetup
import kotlinx.browser.document
import org.jetbrains.skiko.wasm.onWasmReady
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    onWasmReady {
        koinSetup()
        CanvasBasedWindow(canvasElementId = "ComposeTarget") {
            ComposeViewport(document.body!!) {
                KoinContext {
                    var showApp by remember { mutableStateOf(false) }
                    val initializer: Initializer = koinInject()
                    val dbInitializer: DatabaseSchemaInitializer = koinInject()

                    LaunchedEffect(Unit) {
                        initializer()
                        dbInitializer.initSchema()
                        showApp = true
                    }

                    if (showApp) {
                        App()
                    }
                }
            }
        }
    }
}

