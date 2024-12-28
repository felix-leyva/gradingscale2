package de.felixlf.gradingscale2.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.aakira.napier.Napier
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val appNavController: AppNavController = koinInject { parametersOf(navController) }
    val currentDestination = appNavController.controller.currentBackStackEntryAsState()
    val navigationBar = remember(appNavController.controller.currentDestination) {
        movableContentOf {
            Text("Something")
        }
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            Destinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = null,
                        )
                    },
                    label = { Text(stringResource(it.label)) },
                    selected = currentDestination.value?.destination?.route?.substringAfterLast(".") == it.name,
                    onClick = {
                        Napier.d("Navigate to ${it.name}")
                        appNavController.controller.navigate(it.name)
                    },
                )
            }
        },
    ) {
        MainNavHost(appNavController)
    }
}
