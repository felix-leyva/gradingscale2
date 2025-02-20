package de.felixlf.gradingscale2.features.list.editgradedialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.usecases.GetGradeByUUIDUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import de.felixlf.gradingscale2.entities.usecases.InsertGradeUseCase
import de.felixlf.gradingscale2.entities.usecases.UpsertGradeUseCase
import de.felixlf.gradingscale2.features.list.editgradedialog.EditGradeUIState.Error.DUPLICATE_NAME
import de.felixlf.gradingscale2.features.list.editgradedialog.EditGradeUIState.Error.DUPLICATE_PERCENTAGE
import de.felixlf.gradingscale2.features.list.editgradedialog.EditGradeUIState.Error.INVALID_NAME
import de.felixlf.gradingscale2.features.list.editgradedialog.EditGradeUIState.Error.INVALID_PERCENTAGE
import de.felixlf.gradingscale2.uimodel.MoleculePresenter
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/**
 * This class is responsible for managing the UI state of the edit grade dialog.
 */
class EditGradeUIFactory(
    private val getGradeByUUIDUseCase: GetGradeByUUIDUseCase,
    private val insertGradeUseCase: InsertGradeUseCase,
    private val upsertGradeUseCase: UpsertGradeUseCase,
    private val getGradeScaleByIdUseCase: GetGradeScaleByIdUseCase,
    private val viewModelScope: CoroutineScope,
) : MoleculePresenter<EditGradeUIState, EditGradeUIEvent> {

    private var gradeScaleId by mutableStateOf<String?>(null)
    private var currentGradeScale by mutableStateOf<GradeScale?>(null)

    private val currentGradeScaleNames by derivedStateOf { currentGradeScale?.grades?.map { it.namedGrade } }
    private val currentGradeScalePercentages by derivedStateOf { currentGradeScale?.grades?.map { it.percentage } }

    private var gradeUUID by mutableStateOf<String?>(null)
    private var selectedGrade by mutableStateOf<Grade?>(null)

    private var gradeName by mutableStateOf<String?>(null)
    private var percentage by mutableStateOf<String?>(null)
    private var percentageFieldErrors by mutableStateOf<EditGradeUIState.Error?>(null)
    private var nameFieldErrors by mutableStateOf<EditGradeUIState.Error?>(null)

    @Composable
    override fun produceUI(): EditGradeUIState {
        initUi()
        return EditGradeUIState(
            grade = selectedGrade,
            percentage = percentage,
            name = gradeName,
            error = setOfNotNull(percentageFieldErrors, nameFieldErrors).toPersistentSet(),
        )
    }

    @Composable
    private inline fun initUi() {
        LaunchedEffect(gradeScaleId) {
            gradeScaleId?.let { getGradeScaleByIdUseCase(it) }?.filterNotNull()?.collect {
                currentGradeScale = it
            }
        }

        LaunchedEffect(gradeUUID) {
            gradeUUID?.let { getGradeByUUIDUseCase(it) }?.filterNotNull()?.collect {
                selectedGrade = it
                gradeName = it.namedGrade
                percentage = it.percentage.times(100).toString()
                gradeScaleId = it.idOfGradeScale
            }
        }
    }

    override fun sendEvent(event: EditGradeUIEvent) {
        when (event) {
            is EditGradeUIEvent.SetGradeScaleId -> gradeScaleId = event.id
            is EditGradeUIEvent.SetGradeUUID -> gradeUUID = event.uuid
            is EditGradeUIEvent.SetGradeName -> updateGradeName(event)
            is EditGradeUIEvent.SetPercentage -> upgradePercentage(event)
            is EditGradeUIEvent.Save -> save()
        }
    }

    private fun save() {
        viewModelScope.launch {
            val gradeName = requireNotNull(gradeName)
            val percentage = requireNotNull(percentage?.toDoubleOrNull()?.div(100.0))
            val gradeScaleId = requireNotNull(gradeScaleId)
            val selectedGrade = selectedGrade
            when {
                nameFieldErrors != null || percentageFieldErrors != null -> return@launch

                selectedGrade != null -> {
                    val updatedGrade = selectedGrade.copy(
                        namedGrade = gradeName,
                        percentage = percentage,
                    )
                    upsertGradeUseCase(updatedGrade)
                }

                else -> insertGradeUseCase(
                    gradeScaleId = gradeScaleId,
                    percentage = percentage,
                    namedGrade = gradeName,
                )
            }
        }
    }

    private fun upgradePercentage(event: EditGradeUIEvent.SetPercentage) = event.percentage.let { updatedPercentage ->
        percentage = updatedPercentage
        val newPercentage = updatedPercentage.toDoubleOrNull()?.div(100)
        percentageFieldErrors = when {
            newPercentage == null || newPercentage !in 0.0..1.0 -> INVALID_PERCENTAGE
            selectedGrade?.percentage != newPercentage && currentGradeScalePercentages?.contains(newPercentage) == true -> DUPLICATE_PERCENTAGE
            else -> null
        }
    }

    private fun updateGradeName(
        event: EditGradeUIEvent.SetGradeName
    ) = with(event) {
        gradeName = name
        nameFieldErrors = when {
            name.isBlank() -> INVALID_NAME
            selectedGrade?.namedGrade != name && currentGradeScaleNames?.contains(name) == true -> DUPLICATE_NAME
            else -> null
        }
    }

}

sealed interface EditGradeUIEvent {
    data class SetGradeScaleId(val id: String) : EditGradeUIEvent
    data class SetGradeUUID(val uuid: String) : EditGradeUIEvent
    data class SetGradeName(val name: String) : EditGradeUIEvent
    data class SetPercentage(val percentage: String) : EditGradeUIEvent
    data object Save : EditGradeUIEvent
}
