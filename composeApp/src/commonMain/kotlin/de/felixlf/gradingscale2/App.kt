package de.felixlf.gradingscale2

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.felixlf.gradingscale2.entities.repositories.GradeScaleRepository
import de.felixlf.gradingscale2.entities.util.GradeScaleGenerator
import gradingscale2.composeapp.generated.resources.Res
import gradingscale2.composeapp.generated.resources.compose_multiplatform
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    KoinContext { }
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        var text by remember { mutableStateOf("Hello, World!") }
        val repo = koinInject<GradeScaleRepository>()
        val grades by repo.getGradeScales().collectAsStateWithLifecycle(persistentListOf())
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
            LaunchedEffect(Unit) {
                val gradeScale = GradeScaleGenerator().gradeScales
                gradeScale.forEach { repo.upsertGradeScale(it) }
            }
            AnimatedVisibility(showContent) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Compose: $grades")
                }
            }
            Text(text)
        }
    }
}
