package de.felixlf.gradingscale2

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.felixlf.gradingscale2.features.gradescalecalculator.GradeScaleListScreen
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

@Composable
@Preview
internal fun App() {
    KoinContext { }
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            val analyticsProvider: AnalyticsProvider = koinInject()
            Button(onClick = { showContent = !showContent }) {
                Text("Click me!")
                analyticsProvider.logEvent("ButtonPress", mapOf("state" to showContent))
            }
            val authTokenProvider: AuthTokenProvider = koinInject()
            LaunchedEffect(Unit) {
                println("LaunchedEffect")
                authTokenProvider
                    .getTokenFlow()
                    .filterNotNull()
                    .first()
                    .let { println(it) }
                analyticsProvider.logEvent("TestEvent", mapOf("text" to "text2"))
            }
            AnimatedVisibility(showContent) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Compose: ")
                    GradeScaleListScreen()
                }
            }
        }
    }
}
