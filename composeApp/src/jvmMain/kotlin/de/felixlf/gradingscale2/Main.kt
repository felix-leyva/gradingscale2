@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import de.felixlf.gradingscale2.di.koinSetup
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.reload.DevelopmentEntryPoint
import org.koin.java.KoinJavaComponent.inject

fun main() {
    initJvmApp()
    application {
        // Used by hot reload
        DevelopmentEntryPoint {
            Window(
                alwaysOnTop = true,
                onCloseRequest = ::exitApplication,
                title = "GradingScale2",
            ) {
                App()
            }
        }
    }
}

private fun initJvmApp() =
    runBlocking {
        koinSetup()
        val dbInitializer: DatabaseSchemaInitializer by inject(
            DatabaseSchemaInitializer::class.java,
        )
        dbInitializer.initSchema()
    }
