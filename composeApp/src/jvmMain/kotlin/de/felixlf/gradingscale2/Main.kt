package de.felixlf.gradingscale2

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import de.felixlf.gradingscale2.di.koinSetup
import gradingscale2.composeapp.generated.resources.Res
import gradingscale2.composeapp.generated.resources.ic_launcher_foreground
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.reload.DevelopmentEntryPoint
import org.jetbrains.compose.resources.painterResource
import org.koin.java.KoinJavaComponent.inject

fun main() {
    initJvmApp()
    application {
        // Used by hot reload
        DevelopmentEntryPoint {
            val icon = painterResource(Res.drawable.ic_launcher_foreground)
            Window(
                alwaysOnTop = true,
                onCloseRequest = ::exitApplication,
                title = "Grading Scale",
                icon = icon,
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
