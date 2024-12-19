package de.felixlf.gradingscale2.features.gradescalecalculator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import de.felixlf.gradingscale2.features.gradescalecalculator.GradeScaleListUIEvent.SelectGradeScale
import de.felixlf.gradingscale2.features.gradescalecalculator.GradeScaleListUIEvent.SetTotalPoints
import de.felixlf.gradingscale2.uimodel.MoleculePresenter
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

internal class GradeListUIStateFactory(
    private val allGradeScalesUseCase: GetAllGradeScalesUseCase,
    private val getGradeScaleByIdUseCase: GetGradeScaleByIdUseCase,
) : MoleculePresenter<GradeScaleListUIState, GradeScaleListUIEvent> {

    // MutableStateOf causes inside the produceUI function recomposition which is helpful to update the State. If we wish to "observe" this
    // value in other places, we need to do this inside @Composable functions.
    private var gradeScaleId by mutableStateOf<String?>(null)
    private var totalPoints by mutableStateOf(10.0)

    // Important exception: do not use this state value in the produceUI function, to avoid recomposition loops.
    private var state by mutableStateOf<GradeScaleListUIState?>(null)

    @Composable
    override fun produceUI(): GradeScaleListUIState {
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

    override fun sendEvent(event: GradeScaleListUIEvent) {
        when (event) {
            is SelectGradeScale ->
                gradeScaleId =
                    state?.gradeScalesNamesWithId?.firstOrNull { it.gradeScaleName == event.gradeScaleName }?.gradeScaleId

            is SetTotalPoints -> {
                if (event.points <= 0) return
                totalPoints = event.points
            }
        }
    }
}

sealed interface GradeScaleListUIEvent {
    data class SelectGradeScale(val gradeScaleName: String) : GradeScaleListUIEvent
    data class SetTotalPoints(val points: Double) : GradeScaleListUIEvent
}

// The following code is the original code from the project, which was optimized using Molecule.
//    @OptIn(ExperimentalCoroutinesApi::class)
//    val uiState =
//        selectedGradeScaleId
//            .flatMapLatest { id ->
//                id?.let { getGradeScaleByIdUseCase(it) } ?: flowOf(null)
//            }.combine(totalPoints) { gradeScale, totalPoints ->
//                gradeScale?.copy(totalPoints = totalPoints)
//            }.combine(getAllGradeScalesUseCase()) { selectedGradeScale, gradeScales ->
//                GradeScaleListUIState(
//                    selectedGradeScale = selectedGradeScale,
//                    gradeScalesNamesWithId =
//                        gradeScales
//                            .map {
//                                GradeScaleNameWithId(
//                                    gradeScaleName = it.gradeScaleName,
//                                    gradeScaleId = it.id,
//                                )
//                            }.toImmutableList(),
//                )
//            }.stateIn(
//                scope = viewModelScope,
//                started = SharingStarted.WhileSubscribed(5000),
//                initialValue = GradeScaleListUIState.Initial,
//            )
