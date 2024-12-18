package de.felixlf.gradingscale2.navigation

import androidx.navigation.NavHostController

interface AppNavController {
    fun setNavController(navHostController: NavHostController)
    fun getNavController(): NavHostController
}

internal class AppNavControllerImpl : AppNavController {
    private lateinit var navController: NavHostController

    override fun setNavController(navHostController: NavHostController) {
        if (::navController.isInitialized) return
        navController = navHostController
    }

    override fun getNavController(): NavHostController {
        return navController
    }
}
