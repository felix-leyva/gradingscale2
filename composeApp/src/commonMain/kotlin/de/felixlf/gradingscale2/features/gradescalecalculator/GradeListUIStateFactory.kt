package de.felixlf.gradingscale2.features.gradescalecalculator

import androidx.compose.runtime.Composable
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import de.felixlf.gradingscale2.uimodel.UIStateFactory
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow

internal class GradeListUIStateFactory(
    private val allGradeScalesUseCase: GetAllGradeScalesUseCase,
    private val getGradeScaleByIdUseCase: GetGradeScaleByIdUseCase,
    private val selectedGradeScaleId: MutableStateFlow<String?>,
    private val totalPoints: MutableStateFlow<Double>,
) : UIStateFactory<GradeScaleListUIState> {
    @Composable
    override fun produceUI(): GradeScaleListUIState {
        val selectedGradeScale = selectedGradeScaleId.asState()?.let(getGradeScaleByIdUseCase::invoke)?.asState(null)
        val modifiedGradeScale = selectedGradeScale?.copy(totalPoints = totalPoints.asState())

        val gradeScalesNamesWithId = allGradeScalesUseCase().asState(persistentListOf()).map {
            GradeScaleListUIState.GradeScaleNameWithId(
                gradeScaleName = it.gradeScaleName,
                gradeScaleId = it.id,
            )
        }.toImmutableList()

        return GradeScaleListUIState(
            selectedGradeScale = modifiedGradeScale,
            gradeScalesNamesWithId = gradeScalesNamesWithId,
        )
    }
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
