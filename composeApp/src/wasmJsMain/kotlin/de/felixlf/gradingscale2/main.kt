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
    println("=== WasmJS App Starting ===")

    try {
        println("Step 1: Initializing Koin...")

        // Initialize Koin dependency injection
        de.felixlf.gradingscale2.di.koinSetup()

        println("Step 2: Creating ComposeViewport...")

        ComposeViewport(document.body!!) {
            org.koin.compose.KoinContext {
                App()
            }
        }

        println("Step 3: ComposeViewport created successfully")
    } catch (e: Throwable) {
        println("=== ERROR in main ===")
        println("Error type: ${e::class.simpleName}")
        println("Error message: ${e.message}")
        println("Stack trace: ${e.stackTraceToString()}")
        println("=== END ERROR ===")
    }
}
