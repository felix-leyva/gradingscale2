package de.felixlf.gradingscale2.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.MutableWindowInsets
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.onConsumedWindowInsetsChanged
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalLayoutApi::class)
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
        content = {
            val contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            val safeInsets = remember(contentWindowInsets) { MutableWindowInsets(contentWindowInsets) }
            val density = LocalDensity.current
            val insets = contentWindowInsets.asPaddingValues(density)
            val direction = LocalLayoutDirection.current
            val innerPadding = PaddingValues(
                top = insets.calculateTopPadding(),
                bottom = insets.calculateBottomPadding(),
                start = insets.calculateStartPadding(direction),
                end = insets.calculateEndPadding(direction),
            )
            Surface(
                modifier =
                Modifier.onConsumedWindowInsetsChanged { consumedWindowInsets ->
                    // Exclude currently consumed window insets from user provided contentWindowInsets
                    safeInsets.insets = contentWindowInsets.exclude(consumedWindowInsets)
                },
            ) {
                Box(
                    modifier = Modifier.padding(innerPadding),
                ) {
                    MainNavHost(appNavController)
                }
            }
        },
    )
}
