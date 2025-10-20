package de.felixlf.gradingscale2

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import de.felixlf.gradingscale2.entities.network.DiagnosticsProvider
import de.felixlf.gradingscale2.navigation.AppNavController
import de.felixlf.gradingscale2.navigation.MainNavHost
import de.felixlf.gradingscale2.theme.AppTheme
import de.felixlf.gradingscale2.utils.ProvideWindowSizeClass
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
@Preview
internal fun App() {
    val diagnosticsProvider = koinInject<DiagnosticsProvider>()
    LaunchedEffect(Unit) { diagnosticsProvider.initDiagnostics() }
    LaunchedEffect(Unit) { Napier.base(DebugAntilog()) }
    val navController = rememberNavController()
    val appNavController = koinInject<AppNavController> { parametersOf(navController) }

    AppTheme {
        ProvideWindowSizeClass {
            SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
                koinInject<AppState> { parametersOf(this) }
                MainNavHost(appNavController = appNavController)
            }
        }
    }
}
