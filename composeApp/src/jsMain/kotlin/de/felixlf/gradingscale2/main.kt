@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.koin.compose.koinInject

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
//    koinSetup()
//    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
    ComposeViewport(document.body!!) {
        val initializer: Initializer = koinInject()
        val dbInitializer: DatabaseSchemaInitializer = koinInject()

        LaunchedEffect(Unit) {
            initializer()
            dbInitializer.initSchema()
        }
        App()
    }
//    ComposeViewport(document.body!!) {
//
//        App()
//    }
}
