package de.felixlf.gradingscale2.features.gradescalecalculator.editgradedialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.felixlf.gradingscale2.entities.usecases.GetGradeByUUIDUseCase
import de.felixlf.gradingscale2.uimodel.UIStateFactory
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.StateFlow

class EditGradeUIFactory(
    private val getGradeByUUIDUseCase: GetGradeByUUIDUseCase,
    private val gradeUUID: StateFlow<String?>,
    private val gradeName: StateFlow<String?>,
    private val percentage: StateFlow<String?>,
) : UIStateFactory<EditGradeUIState> {

    @Composable
    override fun produceUI(): EditGradeUIState {
        val gradeUUID = gradeUUID.asState()
        val grade = gradeUUID?.let { getGradeByUUIDUseCase(it).asState(null) }
        val gradeNameFlowAsState = gradeName.asState()
        val percentageFlowAsState = percentage.asState()

        var gradeNameState by remember(grade) { mutableStateOf(grade?.namedGrade ?: "") }
        var percentageState by remember(grade) { mutableStateOf(grade?.percentage?.times(100)?.toString() ?: "") }

        LaunchedEffect(gradeNameFlowAsState) { gradeNameFlowAsState?.let { gradeNameState = it } }
        LaunchedEffect(percentageFlowAsState) { percentageFlowAsState?.let { percentageState = it } }

        val errors by remember(gradeNameState, percentageState) {
            derivedStateOf {
                buildSet {
                    if (gradeNameState.isBlank()) add(EditGradeUIState.Error.INVALID_NAME)
                    if ((percentageState.toDoubleOrNull() ?: -1.0) !in 0.0..100.0) add(EditGradeUIState.Error.INVALID_PERCENTAGE)
                }
            }
        }

        return EditGradeUIState(
            grade = grade,
            percentage = percentageState,
            name = gradeNameState,
            error = errors.toImmutableSet(),
        )
    }
}
