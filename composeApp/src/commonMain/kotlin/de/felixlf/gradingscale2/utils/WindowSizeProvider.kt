package de.felixlf.gradingscale2.utils

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import io.github.aakira.napier.Napier
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Composition local for window size class
 */
val LocalWindowSizeClass = compositionLocalOf<WindowSizeClass> {
    error("No WindowSizeClass provided")
}

/**
 * Composition local for tracking if window is being resized
 */
val LocalIsResizing = compositionLocalOf { false }

/**
 * Provides window size class to the composition with debouncing
 */
@OptIn(FlowPreview::class)
@Composable
fun ProvideWindowSizeClass(
    content: @Composable () -> Unit,
) {
    val currentWindowSizeClass = getWindowSizeClass()
    val debouncedWindowSizeClass = remember { mutableStateOf(currentWindowSizeClass) }


    LaunchedEffect(currentWindowSizeClass) {
        snapshotFlow { currentWindowSizeClass }
            .distinctUntilChanged()
            .debounce(100)
            .collect { newSize ->
                Napier.d(tag = "WindowSizeProvider", message = "Window size changed to: ${newSize.widthSizeClass}")
                debouncedWindowSizeClass.value = newSize
            }
    }

    CompositionLocalProvider(
        LocalWindowSizeClass provides debouncedWindowSizeClass.value,
    ) {
        content()
    }
}

/**
 * Gets the current window size class from composition local
 */
@Composable
fun currentWindowSizeClass(): WindowSizeClass = LocalWindowSizeClass.current

/**
 * Checks if the current window is at least medium width
 * Uses composition local to avoid repeated calculations
 */
@Composable
fun isAtLeastMediumScreenWidthLocal(): State<Boolean> {
    val windowSizeClass = currentWindowSizeClass()
    return remember(windowSizeClass) {
        derivedStateOf {
            windowSizeClass.widthSizeClass > WindowWidthSizeClass.Compact
        }
    }
}

/**
 * Checks if the current window is large (expanded) width
 * Uses composition local to avoid repeated calculations
 */
@Composable
fun isLargeScreenWidthLocal(): State<Boolean> {
    val windowSizeClass = currentWindowSizeClass()
    return remember(windowSizeClass) {
        derivedStateOf {
            windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
        }
    }
}
