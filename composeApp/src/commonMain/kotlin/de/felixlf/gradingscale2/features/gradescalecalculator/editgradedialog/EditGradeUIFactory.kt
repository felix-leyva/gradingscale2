package de.felixlf.gradingscale2.features.gradescalecalculator.editgradedialog

import androidx.compose.runtime.Composable
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

        val gradeNameField = gradeNameFlowAsState ?: grade?.namedGrade
        val gradePercentageField = percentageFlowAsState ?: grade?.percentage?.times(100)?.toString()

        val errors = buildSet {
            if (gradeNameField?.isBlank() == true) add(EditGradeUIState.Error.INVALID_NAME)
            if ((gradePercentageField?.toDoubleOrNull() ?: -1.0) !in 0.0..100.0) add(EditGradeUIState.Error.INVALID_PERCENTAGE)
        }

        return EditGradeUIState(
            grade = grade,
            percentage = gradePercentageField,
            name = gradeNameField,
            error = errors.toImmutableSet(),
        )
    }
}
