package de.felixlf.gradingscale2.scaffold

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import de.felixlf.gradingscale2.AppState
import de.felixlf.gradingscale2.navigation.Destinations
import de.felixlf.gradingscale2.utils.currentWindowSizeClass
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

private const val NAV_RAIL_WIDTH_DP = 120f // Increased width for better touch targets
private const val ANIMATION_DURATION_MS = 300 // Smooth transition duration
private const val SURFACE_ELEVATION_DP = 3

@Composable
fun PersistentNavigationRail(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val windowSizeClass = currentWindowSizeClass()
    val shouldShow by remember(windowSizeClass) {
        derivedStateOf { windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact }
    }

    val animatedWidth by animateFloatAsState(
        targetValue = if (shouldShow) NAV_RAIL_WIDTH_DP else 0f,
        animationSpec = tween(durationMillis = ANIMATION_DURATION_MS),
        label = "NavRailWidth",
    )

    if (animatedWidth > 0f) {
        NavigationRail(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(SURFACE_ELEVATION_DP.dp),
            modifier = modifier
                .width(animatedWidth.dp)
                .fillMaxHeight(),
        ) {
            content()
        }
    }
}

@Composable
fun DefaultNavigationRail(
    modifier: Modifier = Modifier,
) {
    val appState: AppState = koinInject()
    val navController = appState.navController.controller
    val currentDestination = navController.currentBackStackEntryAsState()
    PersistentNavigationRail(modifier = modifier) {
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
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}
