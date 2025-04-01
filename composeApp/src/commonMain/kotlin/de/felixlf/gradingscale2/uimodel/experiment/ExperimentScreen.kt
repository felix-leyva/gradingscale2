package de.felixlf.gradingscale2.uimodel.experiment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import de.felixlf.gradingscale2.entities.util.DispatcherProvider
import de.felixlf.gradingscale2.uimodel.ObserveEvents
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperimentScreen() {
    val dispatcherProvider = koinInject<DispatcherProvider>()
    val viewModel: ExperimentViewModel = viewModel(factory = experimentViewModelFactory(dispatcherProvider.newUIScope()))
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    ObserveEvents(viewModel.eventFlow()) { event ->
        println("event: $event")
        showDialog = !showDialog
    }

    Box(
        modifier = Modifier.Companion.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column {
            Text(text = uiState.text)
            Button(onClick = { viewModel.sendCommand(ExperimentUICommand.ShowDialog) }) {
                Text(text = "Show Dialog")
            }
        }
    }
    if (showDialog) {
        BasicAlertDialog(
            onDismissRequest = {
                showDialog = false
            },
        ) {
            Card(
                modifier = Modifier.Companion,
            ) {
                Text(text = uiState.text)
            }
        }
    }
}
