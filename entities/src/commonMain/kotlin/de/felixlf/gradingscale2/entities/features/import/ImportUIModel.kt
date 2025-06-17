package de.felixlf.gradingscale2.entities.features.import

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.felixlf.gradingscale2.entities.models.remote.Country
import de.felixlf.gradingscale2.entities.models.remote.CountryGradingScales
import de.felixlf.gradingscale2.entities.models.remote.GradeScaleDTO
import de.felixlf.gradingscale2.entities.uimodel.UIModel
import de.felixlf.gradingscale2.entities.uimodel.UIModelScope
import de.felixlf.gradingscale2.entities.usecases.GetRemoteGradeScaleUseCase
import de.felixlf.gradingscale2.entities.usecases.GetRemoteGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.ImportRemoteGradeScaleIntoDbUseCase
import de.felixlf.gradingscale2.entities.usecases.ShowSnackbarUseCase
import de.felixlf.gradingscale2.entities.usecases.TrackErrorUseCase
import gradingscale2.entities.generated.resources.Res
import gradingscale2.entities.generated.resources.import_grade_get_remote_grades_error
import gradingscale2.entities.generated.resources.import_grade_open_import_dialog_error
import gradingscale2.entities.generated.resources.import_grade_save_success_message
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

class ImportUIModel(
    override val scope: UIModelScope,
    private val getRemoteGradeScalesUseCase: GetRemoteGradeScalesUseCase,
    private val getRemoteGradeScaleUseCase: GetRemoteGradeScaleUseCase,
    private val importRemoteGradeScaleIntoDbUseCase: ImportRemoteGradeScaleIntoDbUseCase,
    private val showSnackbarUseCase: ShowSnackbarUseCase,
    private val trackErrorUseCase: TrackErrorUseCase,
) : UIModel<ImportUIState, ImportCommand, ImportUIEvent> {

    override val events: Channel<ImportUIEvent> = Channel()
    override val uiState: StateFlow<ImportUIState> by moleculeUIState()
    internal var countriesAndGrades: ImmutableList<CountryGradingScales> by mutableStateOf(persistentListOf())
    internal var displayedGradeScaleDTO: GradeScaleDTO? by mutableStateOf(null)
    private var selectedCountry: Country? by mutableStateOf(null)
    private var error: StringResource? by mutableStateOf(null)
    private var isLoading by mutableStateOf(true)

    @Composable
    override fun produceUI(): ImportUIState {
        LaunchedEffect(error) { loadGrades() }
        return ImportUIState(
            countryGradingScales = countriesAndGrades,
            displayedGradeScaleDTO = displayedGradeScaleDTO,
            selectedCountry = selectedCountry,
            isLoading = isLoading,
            error = error,
        )
    }

    private suspend fun loadGrades() {
        if (error == null) {
            isLoading = true
            getRemoteGradeScalesUseCase()
                .onRight { countriesAndGrades = it }
                .onLeft {
                    trackErrorUseCase("$TAG#loadGrades", it.message, it.code)
                    error = Res.string.import_grade_get_remote_grades_error
                }
            isLoading = false
        }
    }

    override fun sendCommand(command: ImportCommand) {
        when (command) {
            is ImportCommand.ImportGradeScale -> doLoadingOperation {
                displayedGradeScaleDTO?.let {
                    importRemoteGradeScaleIntoDbUseCase(it)
                        .onSome {
                            scope.launch {
                                displayedGradeScaleDTO = null
                                showSnackbarUseCase(
                                    message = Res.string.import_grade_save_success_message,
                                    actionLabel = null,
                                    duration = ShowSnackbarUseCase.SnackbarDuration.Short,
                                )
                            }
                        }
                }
            }

            is ImportCommand.OpenImportDialog -> doLoadingOperation {
                getRemoteGradeScaleUseCase(command.countryAndName)
                    .onRight { displayedGradeScaleDTO = it }
                    .onLeft {
                        trackErrorUseCase("$TAG#loadGrades", it.message, it.code)
                        error = Res.string.import_grade_open_import_dialog_error
                    }
            }

            is ImportCommand.SelectCountry -> selectedCountry = command.country
            ImportCommand.DismissImportDialog -> {
                displayedGradeScaleDTO = null
                error = null
            }

            ImportCommand.Refresh -> scope.launch { loadGrades() }
        }
    }

    private fun doLoadingOperation(operation: suspend () -> Unit) {
        scope.launch {
            isLoading = true
            operation()
            isLoading = false
        }
    }

    private companion object {
        const val TAG = "ImportUIModel"
    }
}
