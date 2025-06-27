package de.felixlf.gradingscale2.scaffold

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import de.felixlf.gradingscale2.AppState
import de.felixlf.gradingscale2.navigation.Destinations
import de.felixlf.gradingscale2.utils.currentWindowSizeClass
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

private const val NAV_BAR_HEIGHT_DP = 80f // Standard bottom navigation height
private const val ANIMATION_DURATION_MS = 300 // Smooth transition duration
private const val SURFACE_ELEVATION_DP = 3
private const val CORNER_RADIUS_DP = 16

@Composable
fun PersistentNavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val windowSizeClass = currentWindowSizeClass()
    var shouldShow by remember { mutableStateOf(windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) }

    LaunchedEffect(windowSizeClass.widthSizeClass) {
        val newShouldShow = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
        if (shouldShow != newShouldShow) {
            shouldShow = newShouldShow
        }
    }

    val animatedHeight by animateFloatAsState(
        targetValue = if (shouldShow) NAV_BAR_HEIGHT_DP else 0f,
        animationSpec = tween(durationMillis = ANIMATION_DURATION_MS),
        label = "BottomNavHeight",
    )

    if (animatedHeight > 0f) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(SURFACE_ELEVATION_DP.dp),
            modifier = modifier
                .height(animatedHeight.dp)
                .clip(RoundedCornerShape(topStart = CORNER_RADIUS_DP.dp, topEnd = CORNER_RADIUS_DP.dp)),
        ) {
            content()
        }
    }
}

@Composable
fun DefaultNavigationBar(
    modifier: Modifier = Modifier,
) {
    val appState: AppState = koinInject()
    val navController = appState.navController.controller
    val currentDestination = navController.currentBackStackEntryAsState()
    PersistentNavigationBar(modifier = modifier) {
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
                colors = NavigationBarItemDefaults.colors(
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
