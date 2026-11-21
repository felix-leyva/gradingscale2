package de.felixlf.gradingscale2.feature.gradescales.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

fun interface GradeScalesScreenEntry {
    @Composable
    operator fun invoke(modifier: Modifier)
}
