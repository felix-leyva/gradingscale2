package de.felixlf.gradingscale2.scaffold

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItemColors
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.felixlf.gradingscale2.navigation.AppNavController
import de.felixlf.gradingscale2.navigation.Destinations
import org.jetbrains.compose.resources.stringResource

private const val SURFACE_ELEVATION_DP = 3

/**
 * Hosts the app content and the top-level navigation UI. The navigation suite automatically switches between a bottom
 * bar and a navigation rail based on the current window size class.
 */
@Composable
fun AppNavigationSuite(
    appNavController: AppNavController,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(SURFACE_ELEVATION_DP.dp)
    val itemColors = appNavigationItemColors()
    NavigationSuiteScaffold(
        modifier = modifier,
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = containerColor,
            navigationRailContainerColor = containerColor,
        ),
        navigationSuiteItems = {
            Destinations.entries.forEach { destination ->
                item(
                    selected = appNavController.current == destination,
                    onClick = { appNavController.navigateTopLevel(destination) },
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
                    colors = itemColors,
                )
            }
        },
        content = content,
    )
}

@Composable
private fun appNavigationItemColors(): NavigationSuiteItemColors {
    val selectedColor = MaterialTheme.colorScheme.onPrimaryContainer
    val indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
    return NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = selectedColor,
            selectedTextColor = selectedColor,
            indicatorColor = indicatorColor,
            unselectedIconColor = unselectedColor,
            unselectedTextColor = unselectedColor,
        ),
        navigationRailItemColors = NavigationRailItemDefaults.colors(
            selectedIconColor = selectedColor,
            selectedTextColor = selectedColor,
            indicatorColor = indicatorColor,
            unselectedIconColor = unselectedColor,
            unselectedTextColor = unselectedColor,
        ),
    )
}
