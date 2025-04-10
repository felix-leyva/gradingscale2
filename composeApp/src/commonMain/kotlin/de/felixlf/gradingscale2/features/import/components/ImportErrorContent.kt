package de.felixlf.gradingscale2.features.import.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ImportErrorContent(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.Companion
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.Companion.weight(1f))

        Text(
            text = "Error loading data",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Companion.Center,
        )

        Spacer(modifier = Modifier.Companion.height(8.dp))

        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Companion.Center,
        )

        Spacer(modifier = Modifier.Companion.height(16.dp))

        Button(onClick = onRetry) {
            Text("Retry")
        }

        Spacer(modifier = Modifier.Companion.weight(1f))
    }
}
