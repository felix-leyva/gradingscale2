package de.felixlf.gradingscale2.utils

import androidx.activity.compose.LocalActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi

@ExperimentalMaterial3WindowSizeClassApi
@OptIn(markerClass = [ExperimentalComposeUiApi::class])
@Composable
actual fun getWindowSizeClass(): WindowSizeClass {
    val activity = requireNotNull(LocalActivity.current) { "Activity provided by LocalActivity.current returned null" }
    return calculateWindowSizeClass(activity)
}
