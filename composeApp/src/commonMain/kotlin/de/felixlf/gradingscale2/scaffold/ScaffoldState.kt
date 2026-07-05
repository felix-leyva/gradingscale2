package de.felixlf.gradingscale2.scaffold

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import de.felixlf.gradingscale2.AppState
import org.koin.compose.koinInject

/**
 * UI logic state holder for handling persistent UI elements across navigation destinations.
 * Implements both AnimatedVisibilityScope and SharedTransitionScope to enable animations.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
class ScaffoldState
internal constructor(
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope,
) : AnimatedVisibilityScope by animatedVisibilityScope,
    SharedTransitionScope by sharedTransitionScope

/**
 * Remembers a [ScaffoldState] for the current navigation entry. The animated scope comes from the NavDisplay entry
 * transition ([LocalNavAnimatedContentScope]), so it must be called inside a navigation entry.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun rememberScaffoldState(
    animatedVisibilityScope: AnimatedVisibilityScope = LocalNavAnimatedContentScope.current,
): ScaffoldState {
    val appState = koinInject<AppState>()
    return remember(animatedVisibilityScope, appState) {
        ScaffoldState(
            animatedVisibilityScope = animatedVisibilityScope,
            sharedTransitionScope = appState.sharedTransitionScope,
        )
    }
}
