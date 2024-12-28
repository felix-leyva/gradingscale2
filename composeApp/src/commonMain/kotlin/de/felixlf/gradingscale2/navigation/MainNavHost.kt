package de.felixlf.gradingscale2.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.felixlf.gradingscale2.features.gradescalecalculator.GradeScaleListScreen

@Composable
fun MainNavHost(appNavController: AppNavController) {
    NavHost(
        navController = appNavController.controller,
        startDestination = Destinations.GradeScaleList.name,
    ) {
        composable(Destinations.GradeScaleList.name) {
            Column {
                Button(
                    onClick = {
                        appNavController.controller.navigate(Destinations.GradeScaleDetail.name)
                    },
                ) {
                    Text(text = "GOTO")
                }
                GradeScaleListScreen()
            }
        }

        composable(Destinations.GradeScaleDetail.name) {
            Button(
                onClick = {
                    appNavController.controller.navigate(Destinations.GradeScaleList.name)
                },
            ) {
                Text(text = "GOTO")
            }
        }
    }
}
