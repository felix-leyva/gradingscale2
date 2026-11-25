package de.felixlf.gradingscale2

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.rememberNavBackStack
import de.felixlf.gradingscale2.entities.network.DiagnosticsProvider
import de.felixlf.gradingscale2.navigation.Destinations
import de.felixlf.gradingscale2.navigation.MainNavHost
import de.felixlf.gradingscale2.navigation.navSavedStateConfiguration
import de.felixlf.gradingscale2.navigation.rememberAppNavController
import de.felixlf.gradingscale2.scaffold.AppNavigationSuite
import de.felixlf.gradingscale2.theme.AppTheme
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.compose.koinInject

// Public because :androidApp calls this from its MainActivity since the AGP 9 module split
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun App() {
    val diagnosticsProvider = koinInject<DiagnosticsProvider>()
    LaunchedEffect(Unit) { diagnosticsProvider.initDiagnostics() }
    LaunchedEffect(Unit) { Napier.base(DebugAntilog()) }
    val backStack = rememberNavBackStack(navSavedStateConfiguration, Destinations.GradeScaleList)
    val appNavController = rememberAppNavController(backStack)

    AppTheme {
        SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
            val appState = remember(appNavController, this) {
                AppState(
                    navController = appNavController,
                    sharedTransitionScope = this,
                )
            }
            CompositionLocalProvider(LocalAppState provides appState) {
                AppNavigationSuite(appNavController = appNavController) {
                    MainNavHost(appNavController = appNavController)
                }
            }
        }
    }
}
