@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

/**
 * Main entry point for the WasmJS application
 * Uses ComposeViewport like the working project
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    try {
        de.felixlf.gradingscale2.di.koinSetup()
        ComposeViewport(document.body!!) {
            App()
        }
    } catch (e: Throwable) {
        println("=== ERROR in main ===")
        println("Error type: ${e::class.simpleName}")
        println("Error message: ${e.message}")
        println("Stack trace: ${e.stackTraceToString()}")
        println("=== END ERROR ===")
    }
}
