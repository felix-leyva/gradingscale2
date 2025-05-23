package de.felixlf.gradingscale2

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import de.felixlf.gradingscale2.navigation.AppNavController
import de.felixlf.gradingscale2.navigation.MainNavHost
import de.felixlf.gradingscale2.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
@Preview
internal fun App() {
    KoinContext {
        val navController = rememberNavController()
        val appNavController = koinInject<AppNavController> { parametersOf(navController) }

        AppTheme {
            SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
                koinInject<AppState> { parametersOf(this) }
                MainNavHost(appNavController = appNavController)
            }
        }
    }
}
