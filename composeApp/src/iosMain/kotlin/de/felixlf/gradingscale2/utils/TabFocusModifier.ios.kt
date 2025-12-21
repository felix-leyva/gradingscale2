package de.felixlf.gradingscale2.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun Modifier.onPreviewTab(): Modifier = this.onPreviewTabInternal()
