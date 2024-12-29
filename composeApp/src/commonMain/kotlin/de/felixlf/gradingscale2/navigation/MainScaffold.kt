package de.felixlf.gradingscale2.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val appNavController: AppNavController = koinInject { parametersOf(navController) }
    val currentDestination = appNavController.controller.currentBackStackEntryAsState()

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            Destinations.entries.forEach {
                item(
                    selected = currentDestination.value?.destination?.route?.substringAfterLast(".") == it.name,
                    onClick = { appNavController.controller.navigate(it.name) },
                    icon = { Icon(imageVector = it.icon, contentDescription = null) },
                    label = { Text(stringResource(it.label)) },
                )
            }
        },
        content = { MainNavHost(appNavController) },
    )
}
