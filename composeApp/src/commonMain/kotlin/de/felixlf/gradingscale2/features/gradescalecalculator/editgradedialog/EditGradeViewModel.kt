package de.felixlf.gradingscale2.features.gradescalecalculator.editgradedialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.felixlf.gradingscale2.entities.usecases.GetGradeByUUIDUseCase
import de.felixlf.gradingscale2.entities.usecases.UpdateSingleGradeUseCase
import kotlinx.coroutines.flow.MutableStateFlow

class EditGradeViewModel(
    getGradeByUUIDUseCase: GetGradeByUUIDUseCase,
    private val updateSingleGradeUseCase: UpdateSingleGradeUseCase,
) : ViewModel() {
    private val gradeUUID = MutableStateFlow<String?>(null)
    private val gradeName = MutableStateFlow<String?>(null)
    private val percentage = MutableStateFlow<Double?>(null)

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

    fun setPercentage(percentage: Double) {
        this.percentage.value = percentage
    }
    
    fun updateGrade() {
        if(!uiState.value.isSaveButtonEnabled) return
        updateSingleGradeUseCase()
    }
}
