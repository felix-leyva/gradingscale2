package de.felixlf.gradingscale2

import androidx.compose.runtime.Composable
import de.felixlf.gradingscale2.navigation.MainScaffold
import de.felixlf.gradingscale2.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext

@Composable
@Preview
internal fun App() {
    KoinContext {
        AppTheme {
            MainScaffold()
        }
    }
}
