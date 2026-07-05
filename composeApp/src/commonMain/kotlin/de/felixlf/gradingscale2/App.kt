package de.felixlf.gradingscale2

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.rememberNavBackStack
import de.felixlf.gradingscale2.entities.network.DiagnosticsProvider
import de.felixlf.gradingscale2.navigation.AppNavController
import de.felixlf.gradingscale2.navigation.Destinations
import de.felixlf.gradingscale2.navigation.MainNavHost
import de.felixlf.gradingscale2.navigation.navSavedStateConfiguration
import de.felixlf.gradingscale2.scaffold.AppNavigationSuite
import de.felixlf.gradingscale2.theme.AppTheme
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

// Public because :androidApp calls this from its MainActivity since the AGP 9 module split
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
@Preview
fun App() {
    val diagnosticsProvider = koinInject<DiagnosticsProvider>()
    LaunchedEffect(Unit) { diagnosticsProvider.initDiagnostics() }
    LaunchedEffect(Unit) { Napier.base(DebugAntilog()) }
    val backStack = rememberNavBackStack(navSavedStateConfiguration, Destinations.GradeScaleList)
    val appNavController = koinInject<AppNavController> { parametersOf(backStack) }

    AppTheme {
        SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
            koinInject<AppState> { parametersOf(this) }
            AppNavigationSuite(appNavController = appNavController) {
                MainNavHost(appNavController = appNavController)
            }
        }
    }
}
