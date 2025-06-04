package de.felixlf.gradingscale2.scaffold

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape // Added
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme // Added
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults // Added
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation // Added
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip // Added
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp // Added
import androidx.navigation.compose.currentBackStackEntryAsState
import de.felixlf.gradingscale2.AppState
import de.felixlf.gradingscale2.navigation.Destinations
import de.felixlf.gradingscale2.utils.isAtLeastMediumScreenWidth
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/**
 * A shared element key for the bottom navigation bar
 */
private object BottomNavSharedElementKey

/**
 * Z-index for the shared element in overlay
 */
private const val BottomNavSharedElementZIndex = 100f

/**
 * A persistent bottom navigation bar that uses shared element transitions
 * to maintain visual continuity across navigation destinations
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ScaffoldState.PersistentNavigationBar(
    modifier: Modifier = Modifier,
    enterTransition: EnterTransition,
    exitTransition: ExitTransition,
    content: @Composable RowScope.() -> Unit,
) {
    AnimatedVisibility(
        modifier = modifier
            .sharedElement(
                sharedContentState = rememberSharedContentState(BottomNavSharedElementKey),
                animatedVisibilityScope = this,
                zIndexInOverlay = BottomNavSharedElementZIndex,
            ),
        visible = !isAtLeastMediumScreenWidth().value,
        enter = enterTransition,
        exit = exitTransition,
        content = {
            NavigationBar(
                // Added for a subtle elevation effect and shape
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                modifier = Modifier.clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                content()
            }
        },
    )
}

@Composable
fun ScaffoldState.DefaultNavigationBar(
    modifier: Modifier = Modifier,
    enterTransition: EnterTransition = slideInVertically(initialOffsetY = { it }),
    exitTransition: ExitTransition = slideOutVertically(targetOffsetY = { it }),
) {
    val appState: AppState = koinInject()
    val navController = appState.navController.controller
    val currentDestination = navController.currentBackStackEntryAsState()
    PersistentNavigationBar(
        modifier = modifier,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
    ) {
        Destinations.entries.forEach { destination ->
            val selected = rememberUpdatedState(
                currentDestination.value?.destination?.route?.substringAfterLast(".") == destination.name,
            )
            NavigationBarItem(
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
                // Added for custom item colors
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), // Slightly transparent indicator
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}
