package de.felixlf.gradingscale2.scaffold

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex

private const val NAV_RAIL_Z_INDEX = 2f // Navigation rail above content
private const val CONTENT_Z_INDEX = 1f // Main content below navigation

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalLayoutApi::class)
@Composable
fun AnimatedContentScope.PersistentScaffold(
    modifier: Modifier = Modifier.fillMaxSize(),
    topBar: @Composable ScaffoldState.() -> Unit = {},
    floatingActionButton: @Composable ScaffoldState.() -> Unit = {},
    bottomBar: @Composable () -> Unit = { DefaultNavigationBar() },
    navigationRail: @Composable () -> Unit = { DefaultNavigationRail() },
    snackbarHost: @Composable ScaffoldState.() -> Unit = { PersistentSnackbarHost() },
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable ScaffoldState.(PaddingValues) -> Unit,
) {
    val scaffoldState = rememberScaffoldState(this)

    NavigationRailScaffold(
        modifier = modifier,
        navigationRail = navigationRail,
        content = {
            Scaffold(
                modifier = modifier,
                topBar = { scaffoldState.topBar() },
                floatingActionButton = { scaffoldState.floatingActionButton() },
                bottomBar = { bottomBar() },
                snackbarHost = { scaffoldState.snackbarHost() },
                floatingActionButtonPosition = floatingActionButtonPosition,
                contentColor = contentColor,
                contentWindowInsets = contentWindowInsets,
                content = { paddingValues -> scaffoldState.content(paddingValues) },
            )
        },
    )
}

@Composable
private fun NavigationRailScaffold(
    modifier: Modifier = Modifier,
    navigationRail: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Row(modifier = modifier) {
        Box(
            modifier = Modifier
                .wrapContentWidth()
                .zIndex(NAV_RAIL_Z_INDEX),
        ) {
            navigationRail()
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(CONTENT_Z_INDEX),
        ) {
            content()
        }
    }
}
