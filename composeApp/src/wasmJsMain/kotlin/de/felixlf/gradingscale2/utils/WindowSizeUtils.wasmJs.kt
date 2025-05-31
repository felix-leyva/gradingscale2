package de.felixlf.gradingscale2.utils

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi

@ExperimentalMaterial3WindowSizeClassApi
@OptIn(markerClass = [ExperimentalComposeUiApi::class])
@Composable
actual fun getWindowSizeClass(): WindowSizeClass = calculateWindowSizeClass()