package de.felixlf.gradingscale2.entities.features.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.felixlf.gradingscale2.entities.features.list.GradeScaleListUIEvent.SelectGradeScale
import de.felixlf.gradingscale2.entities.features.list.GradeScaleListUIEvent.SetTotalPoints
import de.felixlf.gradingscale2.entities.uimodel.MoleculePresenter
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import de.felixlf.gradingscale2.entities.usecases.GetLastSelectedGradeScaleId
import de.felixlf.gradingscale2.entities.usecases.SetLastSelectedGradeScaleId
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * This class is responsible for managing the UI state of the grade scale list screen.
 */
class GradeListUIModel(
    private val scope: CoroutineScope,
    private val allGradeScalesUseCase: GetAllGradeScalesUseCase,
    private val getGradeScaleByIdUseCase: GetGradeScaleByIdUseCase,
    private val getLastSelectedGradeScaleIdUseCase: GetLastSelectedGradeScaleId,
    private val setLastSelectedGradeScaleIdUseCase: SetLastSelectedGradeScaleId,
) : MoleculePresenter<GradeScaleListUIState, GradeScaleListUIEvent> {

    // MutableStateOf causes inside the produceUI function recomposition which is helpful to update the State. If we wish to "observe" this
    // value in other places, we need to do this inside @Composable functions.
    private var gradeScaleId by mutableStateOf<String?>(null)
    private var totalPoints by mutableStateOf(10.0)

    // Important exception: do not use this state value in the produceUI function, to avoid recomposition loops.
    private var state by mutableStateOf<GradeScaleListUIState?>(null)

    @Composable
    override fun produceUI(): GradeScaleListUIState {
        LaunchedEffect(Unit) { getLastSelectedGradeScaleIdUseCase()?.let { gradeScaleId = it } }
        val selectedGradeScale = gradeScaleId?.let { getGradeScaleByIdUseCase(it).asState(null) }
        val modifiedGradeScale = selectedGradeScale?.copy(totalPoints = totalPoints)
        val gradeScalesNamesWithId = allGradeScalesUseCase().asState(persistentListOf()).map {
            GradeScaleListUIState.GradeScaleNameWithId(
                gradeScaleName = it.gradeScaleName,
                gradeScaleId = it.id,
            )
        }.toImmutableList()

        return GradeScaleListUIState(
            selectedGradeScale = modifiedGradeScale,
            gradeScalesNamesWithId = gradeScalesNamesWithId,
        ).also { state = it }
    }

    override fun sendCommand(command: GradeScaleListUIEvent) {
        when (command) {
            is SelectGradeScale -> {
                gradeScaleId = state?.gradeScalesNamesWithId?.firstOrNull { it.gradeScaleName == command.gradeScaleName }?.gradeScaleId
                scope.launch { gradeScaleId?.let { setLastSelectedGradeScaleIdUseCase(it) } }
            }

            is SetTotalPoints -> {
                if (command.points <= 0) return
                totalPoints = command.points
            }

            is GradeScaleListUIEvent.SelectGradeScaleById -> gradeScaleId = command.gradeScaleId
        }
    }
}

sealed interface GradeScaleListUIEvent {
    data class SelectGradeScale(val gradeScaleName: String) : GradeScaleListUIEvent
    data class SelectGradeScaleById(val gradeScaleId: String) : GradeScaleListUIEvent
    data class SetTotalPoints(val points: Double) : GradeScaleListUIEvent
}
