package de.felixlf.gradingscale2.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

/**
 * JS implementation of PlatformNavHost
 */
@Composable
actual fun PlatformNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier,
    route: String?,
    builder: NavGraphBuilder.() -> Unit,
) {
    // Create a ViewModelStore for this NavHost
    val viewModelStore = remember { ViewModelStore() }

    // Set up ViewModelStoreOwner for composition
    val viewModelStoreOwner = remember(viewModelStore) {
        object : ViewModelStoreOwner {
            override val viewModelStore = viewModelStore
        }
    }

    // First, try to set ViewModelStore using JavaScript interop
    try {
        js("navController.setViewModelStore(viewModelStore)")
    } catch (e: Throwable) {
        console.warn("Using direct JS approach failed, continuing with composition provider")
    }

    // Provide the ViewModelStoreOwner to the composition
    CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
        // Use standard NavHost
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier,
            route = route,
            builder = builder,
        )
    }
}
