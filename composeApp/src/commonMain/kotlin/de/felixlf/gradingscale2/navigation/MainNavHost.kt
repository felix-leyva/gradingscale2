package de.felixlf.gradingscale2.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.composable
import de.felixlf.gradingscale2.features.calculator.GradeScaleCalculatorScreen
import de.felixlf.gradingscale2.features.import.ImportScreen
import de.felixlf.gradingscale2.features.list.GradeScaleListScreen
import de.felixlf.gradingscale2.features.weightedgradecalculator.WeightedGradeCalculatorScreen
import de.felixlf.gradingscale2.scaffold.PersistentScaffold

@Composable
fun MainNavHost(
    modifier: Modifier = Modifier,
    appNavController: AppNavController,
) {
    PlatformNavHost(
        navController = appNavController.controller,
        startDestination = Destinations.GradeScaleList.name,
        modifier = modifier,
    ) {
        composable(Destinations.GradeScaleList.name) {
            PersistentScaffold { GradeScaleListScreen(modifier = Modifier.padding(it)) }
        }

        composable(Destinations.GradeScaleCalculator.name) {
            PersistentScaffold { GradeScaleCalculatorScreen(modifier = Modifier.padding(it)) }
        }

        composable(Destinations.WeightedGradeCalculator.name) {
            PersistentScaffold { WeightedGradeCalculatorScreen(modifier = Modifier.padding(it)) }
        }

        composable(Destinations.GradeImporter.name) {
            PersistentScaffold { ImportScreen(modifier = Modifier.padding(it)) }
        }
    }
}
