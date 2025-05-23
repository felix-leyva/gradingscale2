package de.felixlf.gradingscale2.scaffold

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject

@Composable
fun PersistentSnackbarHost() {
    val snackbarHostState = koinInject<SnackbarHostState>()
    SnackbarHost(hostState = snackbarHostState, modifier = Modifier)
}
