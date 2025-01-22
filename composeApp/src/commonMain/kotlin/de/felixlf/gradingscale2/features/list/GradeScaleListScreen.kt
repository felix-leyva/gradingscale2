package de.felixlf.gradingscale2.features.list

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.felixlf.gradingscale2.entities.util.MockGradeScalesGenerator
import de.felixlf.gradingscale2.features.list.components.GradeScaleListItem
import de.felixlf.gradingscale2.features.list.editgradedialog.EditGradeDialog
import de.felixlf.gradingscale2.uicomponents.DropboxSelector
import de.felixlf.gradingscale2.utils.stringWithDecimals
import de.felixlf.gradingscale2.utils.textFieldManager
import gradingscale2.composeapp.generated.resources.Res
import gradingscale2.composeapp.generated.resources.gradescale_list_no_grade_scale_selected
import gradingscale2.composeapp.generated.resources.gradescale_list_select_grade_scale
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * The Grade Scale List Screen is the main screen of the grade scale list feature. It allows the user to view and edit the grades of a grade scale.
 */
@Composable
fun GradeScaleListScreen() {
    val viewModel: GradeScaleListViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    var activeEditGradeUUID by remember { mutableStateOf<String?>(null) }

    GradeScaleListScreen(
        uiState = uiState.value,
        onSelectGradeScale = { viewModel.onEvent(GradeScaleListUIEvent.SelectGradeScale(it)) },
        onSetTotalPoints = { viewModel.onEvent(GradeScaleListUIEvent.SetTotalPoints(it)) },
        onEditGrade = { activeEditGradeUUID = it },
    )

    activeEditGradeUUID?.let {
        EditGradeDialog(
            uuid = it,
            onDismiss = { activeEditGradeUUID = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GradeScaleListScreen(
    uiState: GradeScaleListUIState,
    onSelectGradeScale: (String) -> Unit = {},
    onSetTotalPoints: (Double) -> Unit = {},
    onEditGrade: (String) -> Unit = {},
) {
    val gradeScale = remember(uiState.selectedGradeScale) { uiState.selectedGradeScale }
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Row {
            val textFieldValue = textFieldManager(uiState.selectedGradeScale?.totalPoints?.stringWithDecimals() ?: "") {
                onSetTotalPoints(it.toDoubleOrNull() ?: 1.0)
            }
            DropboxSelector(
                elements = uiState.gradeScalesNamesWithId.map { it.gradeScaleName }.toImmutableList(),
                selectedElement = uiState.selectedGradeScale?.gradeScaleName,
                onSelectElement = onSelectGradeScale,
                modifier = Modifier.weight(1f),
                defaultText = stringResource(Res.string.gradescale_list_select_grade_scale),
            )

            BasicTextField(
                modifier = Modifier.weight(1f).padding(16.dp),
                state = textFieldValue,
                textStyle = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurface),

            )
        }
        HorizontalDivider()
        if (gradeScale == null) {
            Text(text = stringResource(Res.string.gradescale_list_no_grade_scale_selected))
            return
        }
        // Using column, due a bug in CMP where on overscrolling the list, the list is not more clickable
        val listState = rememberLazyListState()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
        ) {
            item {
                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .fillMaxWidth(),

                )
            }
            itemsIndexed(gradeScale.sortedPointedGrades) { _, grade ->
                Column(
                    modifier = Modifier.clickable(
                        onClick = { onEditGrade(grade.uuid) },
                    ),
                ) {
                    GradeScaleListItem(grade = grade)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .fillMaxWidth(),
                )
            }
        }
    }
}

@Preview
@Composable
private fun CalculatorScreenPreview() {
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
