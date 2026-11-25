package de.felixlf.gradingscale2

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import de.felixlf.gradingscale2.navigation.AppNavController

/**
 * Application-level state holder that provides access to app navigation
 * and can be accessed by all navigation destinations.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Stable
class AppState(
    val navController: AppNavController,
    val sharedTransitionScope: SharedTransitionScope,
)

@OptIn(ExperimentalSharedTransitionApi::class)
internal val LocalAppState = staticCompositionLocalOf<AppState> {
    error("No AppState provided. Ensure rememberScaffoldState is called inside App() or a LocalAppState provider.")
}
