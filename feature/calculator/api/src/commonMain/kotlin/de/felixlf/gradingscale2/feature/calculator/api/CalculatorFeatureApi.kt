package de.felixlf.gradingscale2.feature.calculator.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

fun interface CalculatorScreenEntry {
    @Composable
    operator fun invoke(modifier: Modifier)
}
