package de.felixlf.gradingscale2.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

/**
 * Platform-specific implementation of NavHost.
 * This allows us to handle platform-specific navigation setup.
 */
@Composable
expect fun PlatformNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    route: String? = null,
    builder: NavGraphBuilder.() -> Unit,
)
