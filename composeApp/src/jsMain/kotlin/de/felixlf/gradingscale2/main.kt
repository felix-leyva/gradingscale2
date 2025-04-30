@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import de.felixlf.gradingscale2.di.koinSetup
import org.koin.compose.KoinContext

/**
 * Main entry point for the application
 * Uses CanvasBasedWindow for rendering with Skiko
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Initialize Koin
    koinSetup()

    // Use CanvasBasedWindow which requires skiko.js to be loaded
    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        KoinContext {
            App()
        }
    }
}
