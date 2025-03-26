package de.felixlf.gradingscale2.features.import

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.felixlf.gradingscale2.entities.repositories.RemoteSyncRepository
import org.koin.compose.koinInject

@Composable
internal fun ImportScreen() {
    val repo: RemoteSyncRepository = koinInject<RemoteSyncRepository>()
    var grades by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val gradesRemote = repo.countriesAndGrades().getOrNull() ?: return@LaunchedEffect
        grades = gradesRemote.toString()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            grades,
        )
    }
}
