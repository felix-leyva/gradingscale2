package de.felixlf.gradingscale2.entities.features.list.upsertgradedialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.uimodel.StateProducer
import de.felixlf.gradingscale2.entities.uimodel.UIModel
import de.felixlf.gradingscale2.entities.usecases.GetGradeByUUIDUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import de.felixlf.gradingscale2.entities.usecases.InsertGradeUseCase
import de.felixlf.gradingscale2.entities.usecases.UpsertGradeUseCase
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/**
 * This class is responsible for managing the UI state of the edit grade dialog.
 */
class UpsertGradeUIFactory(
    private val getGradeByUUIDUseCase: GetGradeByUUIDUseCase,
    private val insertGradeUseCase: InsertGradeUseCase,
    private val upsertGradeUseCase: UpsertGradeUseCase,
    private val getGradeScaleByIdUseCase: GetGradeScaleByIdUseCase,
    override val stateProducer: StateProducer,
) : UIModel<UpsertGradeUIState, UpsertGradeUIEvent> {

    private var gradeScaleId by mutableStateOf<String?>(null)
    private var currentGradeScale by mutableStateOf<GradeScale?>(null)

    private var gradeUUID by mutableStateOf<String?>(null)
    private var selectedGrade by mutableStateOf<Grade?>(null)

    private var gradeName by mutableStateOf<String?>(null)
    private var percentage by mutableStateOf<String?>(null)
    private var currentState by mutableStateOf<UpsertGradeUIState?>(null)

    override val uiState: StateFlow<UpsertGradeUIState> by stateProducer {
        initUi()
        UpsertGradeUIState(
            grade = selectedGrade,
            percentage = percentage,
            name = gradeName,
            gradeScale = currentGradeScale,
            selectedGrade = selectedGrade,
        ).also {
            LaunchedEffect(it) { currentState = it }
        }
    }

    @Composable
    private fun initUi() {
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

    override fun sendCommand(command: UpsertGradeUIEvent) {
        when (command) {
            is UpsertGradeUIEvent.SetGradeScaleId -> gradeScaleId = command.id
            is UpsertGradeUIEvent.SetGradeUUID -> gradeUUID = command.uuid
            is UpsertGradeUIEvent.SetGradeName -> updateGradeName(command)
            is UpsertGradeUIEvent.SetPercentage -> upgradePercentage(command)
            is UpsertGradeUIEvent.Save, is UpsertGradeUIEvent.SaveAsNew -> save(command)
            is UpsertGradeUIEvent.Delete -> TODO()
        }
    }

    private fun save(event: UpsertGradeUIEvent) {
        scope.launch {
            val name = gradeName?.takeIf { it.isNotBlank() } ?: return@launch
            val parsedPercentage = percentage?.trim()?.toDoubleOrNull()
                ?.takeIf { it.isFinite() }
                ?.div(100.0)
                ?.takeIf { it in 0.0..1.0 }
                ?: return@launch
            val scaleId = gradeScaleId ?: return@launch
            val currentSelectedGrade = selectedGrade
            val validationState = UpsertGradeUIState(
                name = gradeName,
                percentage = percentage,
                grade = currentSelectedGrade,
                gradeScale = currentGradeScale,
                selectedGrade = currentSelectedGrade,
            )
            when {
                event is UpsertGradeUIEvent.Save && currentSelectedGrade != null && validationState.isSaveButtonEnabled -> {
                    val updatedGrade = currentSelectedGrade.copy(
                        namedGrade = name,
                        percentage = parsedPercentage,
                    )
                    upsertGradeUseCase(updatedGrade)
                }

                event is UpsertGradeUIEvent.SaveAsNew && validationState.isSaveNewButtonEnabled -> insertGradeUseCase(
                    gradeScaleId = scaleId,
                    percentage = parsedPercentage,
                    namedGrade = name,
                )
            }
        }
    }

    private fun upgradePercentage(event: UpsertGradeUIEvent.SetPercentage) = event.percentage.let { updatedPercentage ->
        percentage = updatedPercentage
    }

    private fun updateGradeName(event: UpsertGradeUIEvent.SetGradeName) {
        gradeName = event.name
    }
}

sealed interface UpsertGradeUIEvent {
    data class SetGradeScaleId(val id: String) : UpsertGradeUIEvent
    data class SetGradeUUID(val uuid: String) : UpsertGradeUIEvent
    data class SetGradeName(val name: String) : UpsertGradeUIEvent
    data class SetPercentage(val percentage: String) : UpsertGradeUIEvent
    data object Save : UpsertGradeUIEvent
    data object SaveAsNew : UpsertGradeUIEvent
    data object Delete : UpsertGradeUIEvent
}
