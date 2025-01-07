@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import androidx.compose.ui.window.ComposeViewport
import io.github.aakira.napier.Napier
import kotlinx.browser.document
import org.jetbrains.skiko.wasm.onWasmReady
import org.koin.compose.koinInject

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    onWasmReady {
        CanvasBasedWindow(canvasElementId = "ComposeTarget") {
            ComposeViewport(document.body!!) {
                val initializer: Initializer = koinInject()
                val dbInitializer: DatabaseSchemaInitializer = koinInject()

                LaunchedEffect(Unit) {
                    initializer()
                    dbInitializer.initSchema()
                    Napier.d { "Finished initializer" }
                }
                App()
            }
        }
    }
    Napier.d { "App started"}
//    koinSetup()
//    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
    
//    ComposeViewport(document.body!!) {
//
//        App()
//    }
}
