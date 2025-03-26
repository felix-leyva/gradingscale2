package de.felixlf.gradingscale2.entities.features.import

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.felixlf.gradingscale2.entities.models.remote.Country
import de.felixlf.gradingscale2.entities.models.remote.CountryGradingScales
import de.felixlf.gradingscale2.entities.models.remote.GradeScaleDTO
import de.felixlf.gradingscale2.entities.uimodel.MoleculePresenter
import de.felixlf.gradingscale2.entities.usecases.GetRemoteGradeScaleUseCase
import de.felixlf.gradingscale2.entities.usecases.GetRemoteGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.ImportRemoteGradeScaleIntoDbUseCase
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ImportUIStateFactory(
    private val getRemoteGradeScalesUseCase: GetRemoteGradeScalesUseCase,
    private val getRemoteGradeScaleUseCase: GetRemoteGradeScaleUseCase,
    private val importRemoteGradeScaleIntoDbUseCase: ImportRemoteGradeScaleIntoDbUseCase,
    private val coroutineScope: CoroutineScope
) : MoleculePresenter<ImportUIState, ImportCommand> {
    var countriesAndGrades: ImmutableList<CountryGradingScales> by mutableStateOf(persistentListOf())
    var displayedGradeScaleDTO: GradeScaleDTO? by mutableStateOf(null)
    private var selectedCountry: Country? by mutableStateOf(null)
    private var error: String? by mutableStateOf(null)
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
            getRemoteGradeScalesUseCase().onRight { countriesAndGrades = it }
                .onLeft { error = it.message } // TODO: handle error with string resources
        }
    }

    override fun sendEvent(event: ImportCommand) {
        when (event) {
            is ImportCommand.ImportGradeScale -> doLoadingOperation {
                displayedGradeScaleDTO?.let {
                    importRemoteGradeScaleIntoDbUseCase(it).onSome { /* TODO handle success msg */ }
                }
            }

            is ImportCommand.OpenImportDialog -> doLoadingOperation {
                getRemoteGradeScaleUseCase(event.countryAndName).onRight { displayedGradeScaleDTO = it }
                    .onLeft { error = it.message } // TODO: handle error with string resources
            }

            is ImportCommand.SelectCountry -> selectedCountry = event.country
        }
    }

    private fun doLoadingOperation(operation: suspend () -> Unit) = coroutineScope.launch {
        isLoading = true
        operation()
        isLoading = false
    }

}
