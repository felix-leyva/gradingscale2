package de.felixlf.gradingscale2

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.felixlf.gradingscale2.features.gradescalecalculator.GradeScaleListScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext

@Composable
@Preview
internal fun App() {
    KoinContext {
        MaterialTheme {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Compose: ")
                Scaffold {
                    GradeScaleListScreen()
                }
            }
        }
    }
}
