package de.felixlf.gradingscale2.navigation

import androidx.navigation.NavHostController

/**
 * Container of the NavHostController.
 */
interface AppNavController {
    val controller: NavHostController
}

internal class AppNavControllerImpl(override val controller: NavHostController) : AppNavController
