package de.felixlf.gradingscale2.utils

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Companion.Compact
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo

/**
 * Returns the current window size class based on Material Design 3 guidelines
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
expect fun getWindowSizeClass(): WindowSizeClass

/**
 * Utility function to determine if the current window width is at least medium size
 * This follows Material Design 3 adaptive layout guidelines
 */
@Composable
fun isAtLeastMediumScreenWidth(): State<Boolean> {
    val windowSizeClass = getWindowSizeClass()
    val isMediumOrLarger = windowSizeClass.widthSizeClass > Compact

    return remember(isMediumOrLarger) {
        mutableStateOf(isMediumOrLarger)
    }
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
