package de.felixlf.gradingscale2.features.gradescalecalculator

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.felixlf.gradingscale2.entities.models.PointedGrade
import de.felixlf.gradingscale2.entities.util.MockGradeScalesGenerator
import de.felixlf.gradingscale2.features.gradescalecalculator.editgradedialog.EditGradeDialog
import de.felixlf.gradingscale2.uicomponents.VerticalDivider
import de.felixlf.gradingscale2.utils.stringWithDecimals
import de.felixlf.gradingscale2.utils.textFieldManager
import gradingscale2.composeapp.generated.resources.Res
import gradingscale2.composeapp.generated.resources.gradescale_list_select_grade_scale
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

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
            GradeScaleDropboxSelector(uiState = uiState, onSelectGradeScale = onSelectGradeScale)
            BasicTextField(
                modifier = Modifier.weight(1f).padding(16.dp),
                state = textFieldValue,
            )
        }
        HorizontalDivider()
        if (gradeScale == null) {
            Text(text = "No grade scale selected")
            return
        }
        println("gradeScale: ${gradeScale.gradeScaleName}")
        // TODO: Change to LazyColumn when bug is fixed
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
                    ListItem(grade = grade)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RowScope.GradeScaleDropboxSelector(
    uiState: GradeScaleListUIState,
    onSelectGradeScale: (String) -> Unit,
) {
    var expandedDropdown by remember { mutableStateOf(false) }
    val textFieldState = rememberTextFieldState()

    LaunchedEffect(uiState.selectedGradeScale) {
        uiState.selectedGradeScale?.gradeScaleName?.let(textFieldState::setTextAndPlaceCursorAtEnd)
    }

    ExposedDropdownMenuBox(
        modifier = Modifier.weight(1f),
        expanded = expandedDropdown,
        onExpandedChange = { expandedDropdown = !expandedDropdown },
    ) {
        TextField(
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            state = textFieldState,
            readOnly = true,
            lineLimits = TextFieldLineLimits.SingleLine,
            label = { Text(stringResource(Res.string.gradescale_list_select_grade_scale)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expandedDropdown,
            onDismissRequest = { expandedDropdown = false },
        ) {
            uiState.gradeScalesNames.forEach { string ->
                DropdownMenuItem(
                    onClick = {
                        expandedDropdown = false
                        onSelectGradeScale(string)
                    },
                    text = {
                        Text(text = string)
                    },
                    colors = MenuDefaults.itemColors(),
                )
            }
        }
    }
}

@Composable
private fun ListItem(
    grade: PointedGrade,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f).padding(8.dp),
            text = grade.namedGrade,
        )
        VerticalDivider()
        Text(
            modifier = Modifier.weight(1f).padding(8.dp),
            text = "${(grade.percentage * 100).stringWithDecimals()} %",
        )
        VerticalDivider()
        Text(
            modifier = Modifier.weight(1f).padding(8.dp),
            text = grade.points.stringWithDecimals(),
        )
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
