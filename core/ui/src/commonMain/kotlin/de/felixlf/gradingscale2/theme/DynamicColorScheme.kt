package de.felixlf.gradingscale2.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
expect fun getDynamicColorScheme(darkTheme: Boolean): ColorScheme?
