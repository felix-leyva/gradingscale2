package de.felixlf.gradingscale2.feature.weightedcalculator.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

fun interface WeightedCalculatorScreenEntry {
    @Composable
    operator fun invoke(modifier: Modifier)
}
