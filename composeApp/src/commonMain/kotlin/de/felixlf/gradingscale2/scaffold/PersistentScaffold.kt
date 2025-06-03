package de.felixlf.gradingscale2.scaffold

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateBounds
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

/**
 * A scaffold that supports persistent UI elements across navigation destinations.
 */
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalLayoutApi::class)
@Composable
fun AnimatedContentScope.PersistentScaffold(
    modifier: Modifier = Modifier.fillMaxSize(),
    topBar: @Composable ScaffoldState.() -> Unit = {},
    floatingActionButton: @Composable ScaffoldState.() -> Unit = {},
    bottomBar: @Composable ScaffoldState.() -> Unit = { DefaultNavigationBar() },
    navigationRail: @Composable ScaffoldState.() -> Unit = { DefaultNavigationRail() },
    snackbarHost: @Composable ScaffoldState.() -> Unit = { PersistentSnackbarHost() },
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable ScaffoldState.(PaddingValues) -> Unit,
) {
    val scaffoldState = rememberScaffoldState(this)
    with(scaffoldState) {
        NavigationRailScaffold(
            modifier = modifier,
            navigationRail = navigationRail,
            content = {
                Scaffold(
                    modifier = modifier
                        .animateBounds(lookaheadScope = this),
                    topBar = {
                        topBar()
                    },
                    floatingActionButton = {
                        floatingActionButton()
                    },
                    bottomBar = {
                        bottomBar()
                    },
                    snackbarHost = {
                        snackbarHost()
                    },
                    floatingActionButtonPosition = floatingActionButtonPosition,
                    contentColor = contentColor,
                    contentWindowInsets = contentWindowInsets,
                    content = { paddingValues ->
                        content(paddingValues)
                    },
                )
            },
        )
    }
}

/**
 * Helper composable to render a navigation rail alongside the main content
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private inline fun ScaffoldState.NavigationRailScaffold(
    modifier: Modifier = Modifier,
    navigationRail: @Composable ScaffoldState.() -> Unit,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier,
        content = {
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .zIndex(2f),
            ) {
                navigationRail()
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f),
            ) {
                content()
            }
        },
    )
}
