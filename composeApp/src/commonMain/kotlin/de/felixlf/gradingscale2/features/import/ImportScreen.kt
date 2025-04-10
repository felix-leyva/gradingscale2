package de.felixlf.gradingscale2.features.import

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.felixlf.gradingscale2.entities.features.import.ImportCommand
import de.felixlf.gradingscale2.entities.features.import.ImportUIState
import de.felixlf.gradingscale2.entities.models.remote.CountryAndName
import de.felixlf.gradingscale2.entities.models.remote.CountryGradingScales
import de.felixlf.gradingscale2.entities.models.remote.GradeDTO
import de.felixlf.gradingscale2.entities.models.remote.GradeScaleDTO
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ImportScreen() {
    val viewModel: ImportViewModel = koinViewModel<ImportViewModel>()
    val uiState by viewModel.uiState.collectAsState()

    ImportScreen(
        uiState = uiState,
        onSendCommand = viewModel::sendCommand,
    )
}

@Composable
fun ImportScreen(
    uiState: ImportUIState,
    onSendCommand: (ImportCommand) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading && uiState.countryGradingScales.isEmpty() -> {
                    // Show loading indicator when initially loading
                    LoadingContent()
                }

                uiState.error != null -> {
                    // Show error screen
                    ErrorContent(error = uiState.error!!) {
                        // onSendCommand(ImportCommand.Refresh)
                    }
                }

                else -> {
                    // Main content
                    MainContent(
                        uiState = uiState,
                        onSendCommand = onSendCommand,
                    )
                }
            }
        }

        // Show import dialog if needed
        uiState.displayedGradeScaleDTO?.let { loadedGradeScaleDTO ->
            ImportDialog(
                gradeScale = loadedGradeScaleDTO,
                onConfirm = { onSendCommand(ImportCommand.ImportGradeScale) },
                onDismiss = { onSendCommand(ImportCommand.DismissImportDialog) },
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Error loading data",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text("Retry")
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun MainContent(
    uiState: ImportUIState,
    onSendCommand: (ImportCommand) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Country filter dropdown
        CountryFilterDropdown(
            selectedCountry = uiState.selectedCountry,
            availableCountries = uiState.uniqueCountryNames,
            onCountrySelected = { onSendCommand(ImportCommand.SelectCountry(it)) },
        )

        // Header
        Text(
            text = "Country and grade scale name",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        HorizontalDivider(Modifier)

        // Grade scales list
        GradeScalesList(
            countryGradingScales = uiState.shownCountryGradingScales,
            onGradeScaleClick = { country, name ->
                onSendCommand(ImportCommand.OpenImportDialog(CountryAndName(country, name)))
            },
        )

        // Loading indicator overlay if needed
        if (uiState.isLoading && uiState.countryGradingScales.isNotEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun CountryFilterDropdown(
    selectedCountry: String?,
    availableCountries: List<String>,
    onCountrySelected: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shadowElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selectedCountry ?: "Filter by country",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown Arrow",
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.9f),
            ) {
                // Add "All" option to reset filter
                DropdownMenuItem(
                    onClick = {
                        onCountrySelected(null)
                        expanded = false
                    },
                    text = {
                        Text("All Countries")
                    },
                )

                // List all available countries
                availableCountries.forEach { country ->
                    DropdownMenuItem(
                        onClick = {
                            onCountrySelected(country)
                            expanded = false
                        },
                        text = {
                            Text(country)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun GradeScalesList(
    countryGradingScales: List<CountryGradingScales>,
    onGradeScaleClick: (String, String) -> Unit,
) {
    LazyColumn {
        // Flatten all country grading scales into pairs of country and grade scale name
        countryGradingScales.forEach { countryScale ->
            val country = countryScale.country

            // Display each grade scale for the country
            items(countryScale.gradesScalesNames) { gradeName ->
                GradeScaleItem(
                    country = country,
                    gradeName = gradeName,
                    onClick = { onGradeScaleClick(country, gradeName) },
                )
            }
        }
    }
}

@Composable
private fun GradeScaleItem(
    country: String,
    gradeName: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Text(
            text = "$country - $gradeName",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
    HorizontalDivider()
}

@Composable
private fun ImportDialog(
    gradeScale: GradeScaleDTO,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import the selected grade scale?") },
        text = {
            Column {
                Text(
                    text = "${gradeScale.country}: ${gradeScale.gradeScaleName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Display all grades in the scale
                gradeScale.grades.forEach { grade ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    ) {
                        Text(
                            text = grade.gradeName,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "${(grade.percentage * 100).toInt()}%",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("YES")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("NO")
            }
        },
    )
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
