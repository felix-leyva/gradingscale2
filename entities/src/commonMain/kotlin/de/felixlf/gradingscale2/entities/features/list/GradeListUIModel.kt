package de.felixlf.gradingscale2.entities.features.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.felixlf.gradingscale2.entities.features.list.GradeScaleListUIEvent.SelectGradeScale
import de.felixlf.gradingscale2.entities.features.list.GradeScaleListUIEvent.SetTotalPoints
import de.felixlf.gradingscale2.entities.uimodel.MoleculePresenter
import de.felixlf.gradingscale2.entities.uimodel.UIModelScope
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import de.felixlf.gradingscale2.entities.usecases.GetLastSelectedGradeScaleIdUseCase
import de.felixlf.gradingscale2.entities.usecases.SetLastSelectedGradeScaleIdUseCase
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

/**
 * This class is responsible for managing the UI state of the grade scale list screen.
 */
class GradeListUIModel(
    private val scope: UIModelScope,
    private val allGradeScalesUseCase: GetAllGradeScalesUseCase,
    private val getGradeScaleByIdUseCase: GetGradeScaleByIdUseCase,
    private val getLastSelectedGradeScaleIdUseCase: GetLastSelectedGradeScaleIdUseCase,
    private val setLastSelectedGradeScaleIdUseCase: SetLastSelectedGradeScaleIdUseCase,
) : MoleculePresenter<GradeScaleListUIState, GradeScaleListUIEvent> {

    // MutableStateOf causes inside the produceUI function recomposition which is helpful to update the State. If we wish to "observe" this
    // value in other places, we need to do this inside @Composable functions.
    private var gradeScaleId by mutableStateOf<String?>(null)
    private var totalPoints by mutableStateOf(10.0)

    // Important exception: do not use this state value in the produceUI function, to avoid recomposition loops.
    private var state by mutableStateOf<GradeScaleListUIState?>(null)

    @Composable
    override fun produceUI(): GradeScaleListUIState {
        LaunchedEffect(Unit) {
            try {
                println("[DEBUG] GradeListUIModel: Entered LaunchedEffect for fetching last selected ID.")
                val useCaseInstance = getLastSelectedGradeScaleIdUseCase

                // Log the useCaseInstance to the JS console for inspection
                @Suppress("UNUSED_VARIABLE") // To avoid warnings if only used in js()
                val instanceForJsLog = useCaseInstance
                println("console.log('[DEBUG] getLastSelectedGradeScaleIdUseCase object:', instanceForJsLog);")

                if (useCaseInstance == null) {
                    println("[DEBUG] GetLastSelectedGradeScaleIdUseCase IS NULL!")
                    // Handle null case if it's possible and not an error
                    return@LaunchedEffect
                }

                // You can also try to check if invoke is present, though this is tricky with mangled names
                // js("console.log('[DEBUG] Does it have an invoke-like method?', typeof instanceForJsLog?.get_lastSelectedGradeScaleId_5wlpnc_k$);")
                // More generically:
                // js("console.log('[DEBUG] Keys on useCaseInstance:', Object.keys(instanceForJsLog));")

                println("[DEBUG] Attempting to call invoke() on useCaseInstance.")
                val idFromUseCase = useCaseInstance.invoke() // This is the line that likely fails
                println("[DEBUG] Successfully called invoke(), result: $idFromUseCase")

                idFromUseCase?.let {
                    gradeScaleId = it
                    println("[DEBUG] Set gradeScaleId to: $it")
                } ?: println("[DEBUG] Result from useCaseInstance.invoke() was null.")
            } catch (e: Exception) {
                println("[ERROR] GradeListUIModel - Error in LaunchedEffect: ${e.message}")
                // Log the full JS error object to the console
                @Suppress("UNUSED_VARIABLE")
                val errorForJsLog = e
                println("console.error('[DEBUG] Full JS error object in LaunchedEffect catch block:', errorForJsLog);")
                println("console.error('[DEBUG] Stack trace:', errorForJsLog.stack);")
            }
        }
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
                scope.launch { gradeScaleId?.let { setLastSelectedGradeScaleIdUseCase.invoke(it) } }
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
