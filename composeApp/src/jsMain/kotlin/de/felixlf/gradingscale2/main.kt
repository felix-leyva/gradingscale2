@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import androidx.compose.ui.window.ComposeViewport
import de.felixlf.gradingscale2.di.koinSetup
import kotlinx.browser.document
import org.koin.compose.KoinContext

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    koinSetup()
    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        ComposeViewport(document.body!!) {
            KoinContext {
                App()
            }
        }
    }
}
