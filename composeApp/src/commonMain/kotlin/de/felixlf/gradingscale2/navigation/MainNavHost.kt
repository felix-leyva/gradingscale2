package de.felixlf.gradingscale2.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.composable
import de.felixlf.gradingscale2.features.calculator.GradeScaleCalculatorScreen
import de.felixlf.gradingscale2.features.import.ImportScreen
import de.felixlf.gradingscale2.features.list.GradeScaleListScreen
import de.felixlf.gradingscale2.features.weightedgradecalculator.WeightedGradeCalculatorScreen

@Composable
fun MainNavHost(
    modifier: Modifier = Modifier,
    appNavController: AppNavController,
) {
    // Use our platform-specific NavHost implementation
    PlatformNavHost(
        navController = appNavController.controller,
        startDestination = Destinations.GradeScaleList.name,
        modifier = modifier,
    ) {
        // We need to wrap each screen in a composable block
        composable(Destinations.GradeScaleList.name) {
            GradeScaleListScreen()
        }

        composable(Destinations.GradeScaleCalculator.name) {
            GradeScaleCalculatorScreen()
        }

        composable(Destinations.WeightedGradeCalculator.name) {
            WeightedGradeCalculatorScreen()
        }

        composable(Destinations.GradeImporter.name) {
            ImportScreen()
        }
    }
}
