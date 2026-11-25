package de.felixlf.gradingscale2.entities.features.list

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.felixlf.gradingscale2.entities.features.list.GradeScaleListUIEvent.SelectGradeScale
import de.felixlf.gradingscale2.entities.features.list.GradeScaleListUIEvent.SetTotalPoints
import de.felixlf.gradingscale2.entities.uimodel.StateProducer
import de.felixlf.gradingscale2.entities.uimodel.UIModel
import de.felixlf.gradingscale2.entities.uimodel.asState
import de.felixlf.gradingscale2.entities.usecases.DeleteGradeScaleUseCase
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import de.felixlf.gradingscale2.entities.usecases.GetLastSelectedGradeScaleIdUseCase
import de.felixlf.gradingscale2.entities.usecases.SetLastSelectedGradeScaleIdUseCase
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * This class is responsible for managing the UI state of the grade scale list screen.
 */
internal class GradeListUIModel(
    override val stateProducer: StateProducer,
    private val allGradeScalesUseCase: GetAllGradeScalesUseCase,
    private val getGradeScaleByIdUseCase: GetGradeScaleByIdUseCase,
    private val getLastSelectedGradeScaleIdUseCase: GetLastSelectedGradeScaleIdUseCase,
    private val setLastSelectedGradeScaleIdUseCase: SetLastSelectedGradeScaleIdUseCase,
    private val deleteGradeScaleUseCase: DeleteGradeScaleUseCase? = null,
) : UIModel<GradeScaleListUIState, GradeScaleListUIEvent> {

    private var gradeScaleId by mutableStateOf<String?>(null)
    private var totalPoints by mutableStateOf(10.0)

    override val uiState: StateFlow<GradeScaleListUIState> by stateProducer {
        LaunchedEffect(Unit) {
            val savedId = getLastSelectedGradeScaleIdUseCase()
            if (gradeScaleId == null && !savedId.isNullOrBlank()) {
                gradeScaleId = savedId
            }
        }
        val allGradeScalesFlow = remember { allGradeScalesUseCase() }
        val selectedGradeScaleFlow = remember(gradeScaleId) { gradeScaleId?.let { getGradeScaleByIdUseCase(it) } }
        val selectedGradeScale = selectedGradeScaleFlow?.asState(null)
        val modifiedGradeScale = selectedGradeScale?.copy(totalPoints = totalPoints)
        val gradeScalesNamesWithId = allGradeScalesFlow.asState(persistentListOf()).map {
            GradeScaleListUIState.GradeScaleNameWithId(
                gradeScaleName = it.gradeScaleName,
                gradeScaleId = it.id,
            )
        }.toImmutableList()

        GradeScaleListUIState(
            selectedGradeScale = modifiedGradeScale,
            gradeScalesNamesWithId = gradeScalesNamesWithId,
        )
    }

    override fun sendCommand(command: GradeScaleListUIEvent) {
        when (command) {
            is SelectGradeScale -> {
                val matchedId = uiState.value.gradeScalesNamesWithId
                    .firstOrNull { it.gradeScaleName == command.gradeScaleName }
                    ?.gradeScaleId
                    ?: return
                gradeScaleId = matchedId
                scope.launch { setLastSelectedGradeScaleIdUseCase.invoke(matchedId) }
            }

            is SetTotalPoints -> {
                if (!command.points.isFinite() || command.points <= 0.0) return
                totalPoints = command.points
            }

            is GradeScaleListUIEvent.SelectGradeScaleById -> {
                gradeScaleId = command.gradeScaleId
                scope.launch { setLastSelectedGradeScaleIdUseCase(command.gradeScaleId) }
            }

            is GradeScaleListUIEvent.DeleteGradeScale -> {
                val deleteUseCase = deleteGradeScaleUseCase ?: return
                scope.launch {
                    deleteUseCase(command.gradeScaleId).onSome {
                        if (gradeScaleId == command.gradeScaleId) {
                            val remaining = uiState.value.gradeScalesNamesWithId
                                .filter { it.gradeScaleId != command.gradeScaleId }
                            val nextScale = remaining.firstOrNull()
                            gradeScaleId = nextScale?.gradeScaleId
                            setLastSelectedGradeScaleIdUseCase(nextScale?.gradeScaleId ?: "")
                        }
                    }
                }
            }
        }
    }
}

internal sealed interface GradeScaleListUIEvent {
    data class SelectGradeScale(val gradeScaleName: String) : GradeScaleListUIEvent
    data class SelectGradeScaleById(val gradeScaleId: String) : GradeScaleListUIEvent
    data class SetTotalPoints(val points: Double) : GradeScaleListUIEvent
    data class DeleteGradeScale(val gradeScaleId: String) : GradeScaleListUIEvent
}
