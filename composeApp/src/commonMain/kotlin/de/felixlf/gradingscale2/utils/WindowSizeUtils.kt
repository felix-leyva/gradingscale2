package de.felixlf.gradingscale2.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo

/**
 * Window size classes following Material Design 3 adaptive layout guidelines
 * https://m3.material.io/foundations/layout/applying-layout/window-size-classes
 */
enum class WindowSizeClass {
    COMPACT, // width < 600dp (phones in portrait)
    MEDIUM, // 600dp <= width < 840dp (tablets in portrait, phones in landscape)
    EXPANDED, // width >= 840dp (tablets in landscape, desktops)
}

/**
 * Material Design 3 standard breakpoints for window width
 */
object WindowSizeBreakpoints {
    const val COMPACT_MAX_WIDTH = 600
    const val MEDIUM_MAX_WIDTH = 840
}

/**
 * Returns the current window size class based on Material Design 3 guidelines
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun getWindowSizeClass(): WindowSizeClass {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current

    val screenWidthDp = with(density) {
        windowInfo.containerSize.width.toDp().value
    }

    return when {
        screenWidthDp < WindowSizeBreakpoints.COMPACT_MAX_WIDTH -> WindowSizeClass.COMPACT
        screenWidthDp < WindowSizeBreakpoints.MEDIUM_MAX_WIDTH -> WindowSizeClass.MEDIUM
        else -> WindowSizeClass.EXPANDED
    }
}

/**
 * Utility function to determine if the current window width is at least medium size
 * This follows Material Design 3 adaptive layout guidelines
 */
@Composable
fun isAtLeastMediumScreenWidth(): State<Boolean> {
    val windowSizeClass = getWindowSizeClass()
    val isMediumOrLarger = windowSizeClass != WindowSizeClass.COMPACT

    return remember(isMediumOrLarger) {
        mutableStateOf(isMediumOrLarger)
    }
}

/**
 * Utility function to check if the screen is compact (typically phones in portrait)
 */
@Composable
fun isCompactWidth(): Boolean {
    return getWindowSizeClass() == WindowSizeClass.COMPACT
}

/**
 * Utility function to check if the screen is medium width
 */
@Composable
fun isMediumWidth(): Boolean {
    return getWindowSizeClass() == WindowSizeClass.MEDIUM
}

/**
 * Utility function to check if the screen is expanded (typically tablets in landscape or desktops)
 */
@Composable
fun isExpandedWidth(): Boolean {
    return getWindowSizeClass() == WindowSizeClass.EXPANDED
}

/**
 * Returns the current screen width in dp
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun getScreenWidthDp(): Float {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current

    return with(density) {
        windowInfo.containerSize.width.toDp().value
    }
}
