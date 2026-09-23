package de.felixlf.gradingscale2.features.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.felixlf.gradingscale2.entities.features.list.GradeScaleListUIEvent
import de.felixlf.gradingscale2.entities.features.list.GradeScaleListUIState
import de.felixlf.gradingscale2.entities.features.list.upsertgradescaledialog.UpsertGradeScaleUIState
import de.felixlf.gradingscale2.entities.models.GradeScaleNameAndId
import de.felixlf.gradingscale2.entities.util.MockGradeScalesGenerator
import de.felixlf.gradingscale2.features.list.components.DeleteGradeScaleDialog
import de.felixlf.gradingscale2.features.list.components.GradeScaleListContent
import de.felixlf.gradingscale2.features.list.components.GradeScaleListHeader
import de.felixlf.gradingscale2.features.list.upsertgradedialog.EditGradeDialog
import de.felixlf.gradingscale2.features.list.upsertgradedialog.InsertGradeDialog
import de.felixlf.gradingscale2.features.list.upsertgradescaledialog.UpsertGradeScaleDialog
import de.felixlf.gradingscale2.theme.AppTheme
import de.felixlf.gradingscale2.theme.LocalHazeState
import de.felixlf.gradingscale2.uicomponents.AdaptiveGradeScaleSelector
import de.felixlf.gradingscale2.utils.textFieldManager
import dev.chrisbanes.haze.hazeSource
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

/**
 * The Grade Scale List Screen is the main screen of the grade scale list feature.
 * It allows the user to view, customize, and edit the grades and scales.
 */
@Composable
internal fun GradeScaleListScreen(
    modifier: Modifier = Modifier,
    viewModel: GradeScaleListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var activeDialogCommand by remember { mutableStateOf<GradeScaleListDialogCommand?>(null) }

    GradeScaleListScreen(
        modifier = modifier,
        uiState = uiState,
        onSelectGradeScale = { viewModel.sendCommand(GradeScaleListUIEvent.SelectGradeScale(it)) },
        onSetTotalPoints = { viewModel.sendCommand(GradeScaleListUIEvent.SetTotalPoints(it)) },
        onOpenDialog = { activeDialogCommand = it },
    )

    activeDialogCommand?.let { command ->
        when (command) {
            is GradeScaleListDialogCommand.EditCurrentGrade -> EditGradeDialog(
                uuid = command.gradeId,
                onDismiss = { activeDialogCommand = null },
            )

            is GradeScaleListDialogCommand.AddNewGradeInCurrentGradeScale -> InsertGradeDialog(
                gradeScaleId = command.gradeScaleId,
                onDismiss = { activeDialogCommand = null },
            )

            GradeScaleListDialogCommand.AddNewGradeScale -> UpsertGradeScaleDialog(
                onDismiss = {
                    activeDialogCommand = null
                    it?.let { viewModel.sendCommand(GradeScaleListUIEvent.SelectGradeScaleById(it)) }
                },
                operation = UpsertGradeScaleUIState.State.Operation.Insert,
            )

            is GradeScaleListDialogCommand.EditGradeScale -> UpsertGradeScaleDialog(
                onDismiss = { activeDialogCommand = null },
                operation = UpsertGradeScaleUIState.State.Operation.Update(command.gradeScaleId),
            )

            is GradeScaleListDialogCommand.DeleteGradeScale -> DeleteGradeScaleDialog(
                gradeScaleName = uiState.selectedGradeScaleName,
                onConfirm = {
                    viewModel.sendCommand(GradeScaleListUIEvent.DeleteGradeScale(command.gradeScaleId))
                    activeDialogCommand = null
                },
                onDismiss = { activeDialogCommand = null },
            )
        }
    }
}

@Composable
private fun GradeScaleListScreen(
    uiState: GradeScaleListUIState,
    modifier: Modifier = Modifier,
    onSelectGradeScale: (String) -> Unit = {},
    onSetTotalPoints: (Double) -> Unit = {},
    onOpenDialog: (GradeScaleListDialogCommand) -> Unit = {},
) {
    var isGridView by rememberSaveable { mutableStateOf(false) }

    // Convert UI state to GradeScaleNameAndId list for the adaptive selector
    val gradeScaleItems = remember(uiState.gradeScalesNamesWithId) {
        uiState.gradeScalesNamesWithId.map {
            GradeScaleNameAndId(id = it.gradeScaleId, name = it.gradeScaleName)
        }.toPersistentList()
    }

    AdaptiveGradeScaleSelector(
        items = gradeScaleItems,
        selectedItemId = uiState.selectedGradeScaleId,
        onSelectionChange = { id ->
            id?.let {
                val selectedName = uiState.gradeScalesNamesWithId.find { it.gradeScaleId == id }?.gradeScaleName
                selectedName?.let { onSelectGradeScale(it) }
            }
        },
        modifier = modifier.fillMaxSize(),
    ) { isListPaneVisible ->
        val totalPointsState = textFieldManager(uiState.totalPointsString) {
            onSetTotalPoints(it.toDoubleOrNull() ?: 1.0)
        }

        Box(
            modifier = Modifier
                .hazeSource(LocalHazeState.current)
                .fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .fillMaxSize(),
            ) {
                // Header with Dropdown/Title, Total Points input, View Switcher & Scale Options Menu
                GradeScaleListHeader(
                    isListPaneVisible = isListPaneVisible,
                    gradeScaleItems = gradeScaleItems,
                    selectedGradeScaleId = uiState.selectedGradeScaleId,
                    selectedGradeScaleName = uiState.selectedGradeScaleName,
                    totalPointsState = totalPointsState,
                    isGridView = isGridView,
                    onToggleViewMode = { isGridView = !isGridView },
                    onSelectGradeScaleId = { id ->
                        id?.let {
                            val selectedName = uiState.gradeScalesNamesWithId.find { it.gradeScaleId == id }?.gradeScaleName
                            selectedName?.let { onSelectGradeScale(it) }
                        }
                    },
                    onOpenDialog = onOpenDialog,
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Content: List or Grid view
                GradeScaleListContent(
                    gradeItems = uiState.gradeItems,
                    isGridView = isGridView,
                    isListPaneVisible = isListPaneVisible,
                    onEditGrade = { gradeUuid ->
                        onOpenDialog(GradeScaleListDialogCommand.EditCurrentGrade(gradeUuid))
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            // Floating Action Button to add grade to current scale
            uiState.selectedGradeScaleId?.let { scaleId ->
                FloatingActionButton(
                    onClick = {
                        onOpenDialog(GradeScaleListDialogCommand.AddNewGradeInCurrentGradeScale(scaleId))
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add grade",
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun GradeScaleListScreenPreview() = AppTheme {
    GradeScaleListScreen(
        GradeScaleListUIState(
            selectedGradeScale = MockGradeScalesGenerator().gradeScales.first(),
            gradeScalesNamesWithId = MockGradeScalesGenerator()
                .gradeScales
                .map {
                    GradeScaleListUIState.GradeScaleNameWithId(
                        gradeScaleName = it.gradeScaleName,
                        gradeScaleId = it.id,
                    )
                }.toImmutableList(),
        ),
    )
}
