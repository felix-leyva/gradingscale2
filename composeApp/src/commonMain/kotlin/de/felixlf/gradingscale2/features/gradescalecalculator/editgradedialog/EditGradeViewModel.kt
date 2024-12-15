package de.felixlf.gradingscale2.features.gradescalecalculator.editgradedialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.felixlf.gradingscale2.entities.usecases.GetGradeByUUIDUseCase
import de.felixlf.gradingscale2.entities.usecases.UpsertGradeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class EditGradeViewModel(
    getGradeByUUIDUseCase: GetGradeByUUIDUseCase,
    private val upsertGradeUseCase: UpsertGradeUseCase,
) : ViewModel() {
    private val gradeUUID = MutableStateFlow<String?>(null)
    private val gradeName = MutableStateFlow<String?>(null)
    private val percentage = MutableStateFlow<String?>(null)

    val uiState = EditGradeUIFactory(
        getGradeByUUIDUseCase = getGradeByUUIDUseCase,
        gradeUUID = gradeUUID,
        gradeName = gradeName,
        percentage = percentage,
    ).moleculeUIState(viewModelScope)

    fun setGradeUUID(uuid: String) {
        gradeUUID.value = uuid
    }

    fun setGradeName(name: String) {
        gradeName.value = name
    }

    fun setPercentage(percentage: String) {
        this.percentage.value = percentage
    }

    fun updateGrade() {
        if (!uiState.value.isSaveButtonEnabled) return

        val modifiedGrade = uiState.value.grade?.copy(
            namedGrade = uiState.value.name ?: throw IllegalArgumentException("Grade name cannot be null before saving"),
            percentage = (uiState.value.percentage?.toDoubleOrNull()?.div(100.0))
                ?: throw IllegalArgumentException("Grade percentage cannot be null before saving"),
        ) ?: throw IllegalArgumentException("Grade cannot be null before saving")

        viewModelScope.launch { upsertGradeUseCase(modifiedGrade) }
    }
}
