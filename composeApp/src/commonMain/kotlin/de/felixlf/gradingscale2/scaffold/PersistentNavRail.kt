package de.felixlf.gradingscale2.scaffold

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.compose.currentBackStackEntryAsState
import de.felixlf.gradingscale2.AppState
import de.felixlf.gradingscale2.navigation.Destinations
import de.felixlf.gradingscale2.utils.isAtLeastMediumScreenWidth
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/**
 * A shared element key for the side navigation bar
 */
private object SideNavSharedElementKey

/**
 * Z-index for the shared element in overlay
 */
private const val SideNavSharedElementZIndex = 100f

/**
 * A persistent bottom navigation bar that uses shared element transitions
 * to maintain visual continuity across navigation destinations
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ScaffoldState.PersistentNavigationRail(
    modifier: Modifier = Modifier,
    enterTransition: EnterTransition,
    exitTransition: ExitTransition,
    content: @Composable ColumnScope.() -> Unit,
) {
    AnimatedVisibility(
        modifier = modifier
            .sharedElement(
                sharedContentState = rememberSharedContentState(SideNavSharedElementKey),
                animatedVisibilityScope = this,
                zIndexInOverlay = SideNavSharedElementZIndex,
            ),
        visible = isAtLeastMediumScreenWidth().value,
        enter = enterTransition,
        exit = exitTransition,
        content = {
            NavigationRail {
                content()
            }
        },
    )
}

@Composable
fun ScaffoldState.DefaultNavigationRail(
    modifier: Modifier = Modifier,
    enterTransition: EnterTransition = slideInHorizontally(initialOffsetX = { -it }),
    exitTransition: ExitTransition = slideOutHorizontally(targetOffsetX = { -it }),
) {
    val appState: AppState = koinInject()
    val navController = appState.navController.controller
    val currentDestination = navController.currentBackStackEntryAsState()
    PersistentNavigationRail(
        modifier = modifier,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
    ) {
        Destinations.entries.forEach { destination ->
            val selected = rememberUpdatedState(
                currentDestination.value?.destination?.route?.substringAfterLast(".") == destination.name,
            )
            NavigationRailItem(
                selected = selected.value,
                onClick = {
                    if (!selected.value) {
                        navController.navigate(destination.name)
                    }
                },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = null,
                    )
                },
                label = {
                    Text(
                        text = stringResource(destination.label),
                        textAlign = TextAlign.Center,
                    )
                },
            )
        }
    }
}
