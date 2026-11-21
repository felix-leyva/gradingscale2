package de.felixlf.gradingscale2.feature.importer.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

fun interface ImporterScreenEntry {
    @Composable
    operator fun invoke(modifier: Modifier)
}
