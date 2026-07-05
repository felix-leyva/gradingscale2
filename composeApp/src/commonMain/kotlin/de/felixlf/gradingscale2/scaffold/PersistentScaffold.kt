package de.felixlf.gradingscale2.scaffold

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Scaffold for navigation destinations. The top-level navigation UI (bottom bar / rail) lives outside in
 * [AppNavigationSuite]; this only hosts the per-destination chrome (top bar, FAB, snackbar).
 */
@Composable
fun PersistentScaffold(
    modifier: Modifier = Modifier.fillMaxSize(),
    topBar: @Composable ScaffoldState.() -> Unit = {},
    floatingActionButton: @Composable ScaffoldState.() -> Unit = {},
    snackbarHost: @Composable ScaffoldState.() -> Unit = { PersistentSnackbarHost() },
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable ScaffoldState.(PaddingValues) -> Unit,
) {
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        modifier = modifier,
        topBar = { scaffoldState.topBar() },
        floatingActionButton = { scaffoldState.floatingActionButton() },
        snackbarHost = { scaffoldState.snackbarHost() },
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        contentColor = contentColor,
        contentWindowInsets = contentWindowInsets,
        content = { paddingValues -> scaffoldState.content(paddingValues) },
    )
}
