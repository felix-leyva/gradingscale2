package de.felixlf.gradingscale2.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.felixlf.gradingscale2.features.gradescalecalculator.GradeScaleListScreen
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Composable
fun MainNavHost() {
    val navController = rememberNavController()
    val appNavController: AppNavController = koinInject()
    appNavController.setNavController(navController)

    NavHost(
        navController = navController,
        startDestination = Destinations.GradeScaleList,
    ) {
        composable<Destinations.GradeScaleList> {
            GradeScaleListScreen()
        }
    }
}

sealed interface Destinations {
    @Serializable
    data object GradeScaleList : Destinations

    @Serializable
    data object GradeScaleDetail : Destinations

    @Serializable
    data object WeightedGradeCalculator : Destinations

    @Serializable
    data object GradeImporter : Destinations
}
