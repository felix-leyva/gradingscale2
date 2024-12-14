package de.felixlf.gradingscale2.features.gradescalecalculator.editgradedialog

import androidx.compose.runtime.Composable
import de.felixlf.gradingscale2.entities.usecases.GetGradeByUUIDUseCase
import de.felixlf.gradingscale2.uimodel.UIStateFactory
import kotlinx.coroutines.flow.StateFlow

class EditGradeUIFactory(
    private val getGradeByUUIDUseCase: GetGradeByUUIDUseCase,
    private val gradeUUID: StateFlow<String?>,
    private val gradeName: StateFlow<String?>,
    private val percentage: StateFlow<Double?>,
): UIStateFactory<EditGradeUIState> {

    @Composable
    override fun produceUI(): EditGradeUIState {
        val gradeUUID = gradeUUID.asState()
        val grade = gradeUUID?.let { getGradeByUUIDUseCase(it).asState(null) }
        if (grade == null) return EditGradeUIState(null)
        return EditGradeUIState(
            grade.copy(
                namedGrade = gradeName.asState() ?: grade.namedGrade,
                percentage = percentage.asState() ?: grade.percentage,
            )
        )
    }
}
