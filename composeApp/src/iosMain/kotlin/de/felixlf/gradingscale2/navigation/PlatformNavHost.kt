package de.felixlf.gradingscale2.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

/**
 * iOS implementation of PlatformNavHost that uses the regular NavHost
 */
@Composable
actual fun PlatformNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier,
    route: String?,
    enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition),
    exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition),
    popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition),
    popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition),
    sizeTransform: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> SizeTransform?)?,
    builder: NavGraphBuilder.() -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        route = route,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
        sizeTransform = sizeTransform,
        builder = builder,
    )
}
