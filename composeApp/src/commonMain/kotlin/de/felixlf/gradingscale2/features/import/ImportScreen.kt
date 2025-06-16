package de.felixlf.gradingscale2.features.import

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.felixlf.gradingscale2.entities.features.import.ImportCommand
import de.felixlf.gradingscale2.entities.features.import.ImportUIState
import de.felixlf.gradingscale2.entities.models.remote.CountryAndName
import de.felixlf.gradingscale2.entities.models.remote.CountryGradingScales
import de.felixlf.gradingscale2.entities.models.remote.GradeDTO
import de.felixlf.gradingscale2.entities.models.remote.GradeScaleDTO
import de.felixlf.gradingscale2.features.import.components.ImportErrorContent
import de.felixlf.gradingscale2.features.import.components.ImportGradeScalesList
import de.felixlf.gradingscale2.features.import.dialogs.ImportDialog
import de.felixlf.gradingscale2.theme.LocalHazeState
import de.felixlf.gradingscale2.uicomponents.DropboxSelector
import de.felixlf.gradingscale2.uicomponents.LoadingContent
import dev.chrisbanes.haze.hazeSource
import gradingscale2.entities.generated.resources.Res
import gradingscale2.entities.generated.resources.import_country_and_scale_name
import gradingscale2.entities.generated.resources.import_filter_by_country
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ImportScreen(
    modifier: Modifier = Modifier,
    viewModel: ImportViewModel = koinViewModel<ImportViewModel>(),
) {
    val uiState by viewModel.uiState.collectAsState()

    ImportScreen(
        modifier = modifier,
        uiState = uiState,
        onSendCommand = viewModel::sendCommand,
    )
}

@Composable
fun ImportScreen(
    modifier: Modifier = Modifier,
    uiState: ImportUIState,
    onSendCommand: (ImportCommand) -> Unit,
) {
    Box(modifier = modifier.hazeSource(LocalHazeState.current).fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            uiState.isLoading && uiState.countryGradingScales.isEmpty() -> LoadingContent()

            uiState.error != null -> ImportErrorContent(error = uiState.error!!) {
                onSendCommand(ImportCommand.Refresh)
            }

            else -> MainContent(
                uiState = uiState,
                onSendCommand = onSendCommand,
            )
        }
    }

    uiState.displayedGradeScaleDTO?.let { loadedGradeScaleDTO ->
        ImportDialog(
            gradeScale = loadedGradeScaleDTO,
            onConfirm = { onSendCommand(ImportCommand.ImportGradeScale) },
            onDismiss = { onSendCommand(ImportCommand.DismissImportDialog) },
        )
    }
}

@Composable
private fun MainContent(
    uiState: ImportUIState,
    onSendCommand: (ImportCommand) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DropboxSelector(
                elements = uiState.uniqueCountryNames,
                selectedElement = uiState.selectedCountry,
                onSelectElement = { onSendCommand(ImportCommand.SelectCountry(it)) },
                label = stringResource(Res.string.import_filter_by_country),
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            )

            Text(
                text = stringResource(Res.string.import_country_and_scale_name),
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                style = MaterialTheme.typography.titleMedium,
            )

            HorizontalDivider(Modifier)

            ImportGradeScalesList(
                countryGradingScales = uiState.shownCountryGradingScales,
                onGradeScaleClick = { country, name ->
                    onSendCommand(ImportCommand.OpenImportDialog(CountryAndName(country, name)))
                },
            )
        }
        if (uiState.isLoading && uiState.countryGradingScales.isNotEmpty()) {
            LoadingContent()
        }
    }
}

@Preview
@Composable
fun ImportScreenPreviewLoadingInitial() {
    MaterialTheme {
        Surface {
            ImportScreen(
                uiState = ImportUIState(
                    error = null,
                    isLoading = true,
                    countryGradingScales = persistentListOf(),
                    displayedGradeScaleDTO = null,
                    selectedCountry = null,
                ),
                onSendCommand = {},
            )
        }
    }
}

@Preview
@Composable
fun ImportScreenPreviewError() {
    MaterialTheme {
        Surface {
            ImportScreen(
                uiState = ImportUIState(
                    error = "Network error",
                    isLoading = false,
                    countryGradingScales = persistentListOf(),
                    displayedGradeScaleDTO = null,
                    selectedCountry = null,
                ),
                onSendCommand = {},
            )
        }
    }
}

@Preview
@Composable
fun ImportScreenPreviewMainContent() {
    MaterialTheme {
        Surface {
            ImportScreen(
                uiState = ImportUIState(
                    selectedCountry = null,
                    error = null,
                    isLoading = false,
                    countryGradingScales = persistentListOf(
                        CountryGradingScales("Germany", listOf("German Scale 1", "German Scale 2")),
                        CountryGradingScales("Spain", listOf("Spanish Scale A", "Spanish Scale B")),
                    ),
                    displayedGradeScaleDTO = null,
                ),
                onSendCommand = {},
            )
        }
    }
}

@Preview
@Composable
fun ImportScreenPreviewMainContentLoading() {
    MaterialTheme {
        Surface {
            ImportScreen(
                uiState = ImportUIState(
                    isLoading = true,
                    selectedCountry = "Germany",
                    countryGradingScales = persistentListOf(),
                    error = null,
                    displayedGradeScaleDTO = null,
                ),
                onSendCommand = {},
            )
        }
    }
}

@Preview
@Composable
fun ImportScreenPreviewImportDialog() {
    MaterialTheme {
        Surface {
            ImportScreen(
                uiState = ImportUIState(
                    selectedCountry = "Germany",
                    countryGradingScales = persistentListOf(
                        CountryGradingScales("Germany", listOf("German Scale 1", "German Scale 2")),
                        CountryGradingScales("Spain", listOf("Spanish Scale A", "Spanish Scale B")),
                    ),
                    error = null,
                    isLoading = false,
                    displayedGradeScaleDTO = GradeScaleDTO(
                        country = "Germany",
                        gradeScaleName = "German Scale 1",
                        grades = listOf(
                            GradeDTO("1", 1.0),
                            GradeDTO("2", 0.8),
                            GradeDTO("3", 0.6),
                            GradeDTO("4", 0.4),
                            GradeDTO("5", 0.2),
                            GradeDTO("6", 0.0),
                        ),
                    ),
                ),
                onSendCommand = {},
            )
        }
    }
}
