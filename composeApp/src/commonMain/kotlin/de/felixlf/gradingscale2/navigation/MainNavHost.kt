package de.felixlf.gradingscale2.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.felixlf.gradingscale2.features.calculator.GradeScaleCalculatorScreen
import de.felixlf.gradingscale2.features.list.GradeScaleListScreen

@Composable
fun MainNavHost(appNavController: AppNavController) {
    NavHost(
        navController = appNavController.controller,
        startDestination = Destinations.GradeScaleList.name,
    ) {
        composable(Destinations.GradeScaleList.name) {
            GradeScaleListScreen()
        }
        composable(Destinations.GradeScaleCalculator.name) {
            GradeScaleCalculatorScreen()
        }
        composable(Destinations.WeightedGradeCalculator.name) {
            Text(text = "${it.destination.route}")
        }
        composable(Destinations.GradeImporter.name) {
            Text(text = "${it.destination.route}")
        }
    }
}
