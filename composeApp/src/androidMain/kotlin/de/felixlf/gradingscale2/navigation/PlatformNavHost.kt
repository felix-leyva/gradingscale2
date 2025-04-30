package de.felixlf.gradingscale2.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

/**
 * Android implementation of PlatformNavHost that uses the regular NavHost
 */
@Composable
actual fun PlatformNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier,
    route: String?,
    builder: NavGraphBuilder.() -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        route = route,
        builder = builder,
    )
}
