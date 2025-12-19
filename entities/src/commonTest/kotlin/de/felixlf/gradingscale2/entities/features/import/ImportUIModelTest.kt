package de.felixlf.gradingscale2.entities.features.import

import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import arrow.core.some
import de.felixlf.gradingscale2.entities.models.remote.CountryAndName
import de.felixlf.gradingscale2.entities.models.remote.CountryGradingScales
import de.felixlf.gradingscale2.entities.models.remote.GradeDTO
import de.felixlf.gradingscale2.entities.models.remote.GradeScaleDTO
import de.felixlf.gradingscale2.entities.models.remote.RemoteError
import de.felixlf.gradingscale2.entities.testMoleculeFlow
import de.felixlf.gradingscale2.entities.usecases.FakeTrackerUseCase
import de.felixlf.gradingscale2.entities.usecases.GetRemoteGradeScaleUseCase
import de.felixlf.gradingscale2.entities.usecases.GetRemoteGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.ImportRemoteGradeScaleIntoDbUseCase
import de.felixlf.gradingscale2.entities.usecases.ShowSnackbarUseCase
import gradingscale2.entities.generated.resources.Res
import gradingscale2.entities.generated.resources.import_grade_get_remote_grades_error
import gradingscale2.entities.generated.resources.import_grade_open_import_dialog_error
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ImportUIModelTest {

    // Test data
    private val testCountry = "Germany"
    private val testGradeScaleName = "German Grading Scale"
    private val testGrades = listOf(
        GradeDTO("A", 90.0),
        GradeDTO("B", 80.0),
        GradeDTO("C", 70.0),
    )
    private val testGradeScaleDTO = GradeScaleDTO(
        gradeScaleName = testGradeScaleName,
        country = testCountry,
        grades = testGrades,
    )
    private val testCountryGradingScales = CountryGradingScales(
        country = testCountry,
        gradesScalesNames = listOf(testGradeScaleName),
    )
    private val testCountryAndName = CountryAndName(
        country = testCountry,
        name = testGradeScaleName,
    )
    private val testRemoteError = RemoteError(404, "Not Found")

    // Track method calls
    private var getRemoteGradeScalesUseCaseCalled = false
    private var getRemoteGradeScaleUseCaseCalled = false
    private var importRemoteGradeScaleIntoDbUseCaseCalled = false
    private var lastCountryAndNameParam: CountryAndName? = null
    private var lastGradeScaleDTOParam: GradeScaleDTO? = null

    // Define functional interfaces
    private lateinit var getRemoteGradeScalesUseCase: GetRemoteGradeScalesUseCase
    private lateinit var getRemoteGradeScaleUseCase: GetRemoteGradeScaleUseCase
    private lateinit var importRemoteGradeScaleIntoDbUseCase: ImportRemoteGradeScaleIntoDbUseCase

    private lateinit var showSnackbarUseCase: ShowSnackbarUseCase

    private lateinit var fakeTrackerUseCase: FakeTrackerUseCase

    // Subject under test
    private lateinit var importUIModel: ImportUIModelWithEvents

    @BeforeTest
    fun setup() {
        // Reset tracking variables
        getRemoteGradeScalesUseCaseCalled = false
        getRemoteGradeScaleUseCaseCalled = false
        importRemoteGradeScaleIntoDbUseCaseCalled = false
        lastCountryAndNameParam = null
        lastGradeScaleDTOParam = null

        // Create functional interface implementations
        getRemoteGradeScalesUseCase = GetRemoteGradeScalesUseCase {
            getRemoteGradeScalesUseCaseCalled = true
            persistentListOf(testCountryGradingScales).right()
        }

        getRemoteGradeScaleUseCase = GetRemoteGradeScaleUseCase { countryAndName ->
            getRemoteGradeScaleUseCaseCalled = true
            lastCountryAndNameParam = countryAndName
            testGradeScaleDTO.right()
        }

        importRemoteGradeScaleIntoDbUseCase = ImportRemoteGradeScaleIntoDbUseCase { gradeScaleDTO ->
            importRemoteGradeScaleIntoDbUseCaseCalled = true
            lastGradeScaleDTOParam = gradeScaleDTO
            "Success".some()
        }

        showSnackbarUseCase = ShowSnackbarUseCase { _, _, _ ->
            ShowSnackbarUseCase.SnackbarResult.Dismissed
        }

        fakeTrackerUseCase = FakeTrackerUseCase()
    }

    private fun TestScope.initSUT() {
        // Create the model with test scope from TestDispatcherProvider
        importUIModel = ImportUIModelWithEvents(
            scope = this,
            getRemoteGradeScalesUseCase = getRemoteGradeScalesUseCase,
            getRemoteGradeScaleUseCase = getRemoteGradeScaleUseCase,
            importRemoteGradeScaleIntoDbUseCase = importRemoteGradeScaleIntoDbUseCase,
            showSnackbarUseCase = showSnackbarUseCase,
            trackErrorUseCase = fakeTrackerUseCase,
        )
    }

    @Test
    fun initialStateShouldLoadCountriesAndGrades() = runTest {
        initSUT()
        // We first need to observe the state to trigger molecule evaluation
        testMoleculeFlow(importUIModel) {
            // First emission might be the default state with isLoading=true
            awaitItem()
            // Advance until the loading is done
            // testDispatcherProvider.testDispatcher.scheduler.advanceUntilIdle()

            // If there's another emission, get it
            val finalState = awaitItem()

            // Verify
            assertTrue(getRemoteGradeScalesUseCaseCalled)
            assertEquals(persistentListOf(testCountryGradingScales), finalState.countryGradingScales)
            assertFalse(finalState.isLoading)
            assertNull(finalState.error)

            // Clean up any remaining emissions
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldHandleErrorWhenLoadingGradesFails() = runTest {
        // Create custom error case
        val errorUseCase = GetRemoteGradeScalesUseCase {
            getRemoteGradeScalesUseCaseCalled = true
            testRemoteError.left()
        }

        // Create new model with error case
        val errorModel = ImportUIModelWithEvents(
            scope = this,
            getRemoteGradeScalesUseCase = errorUseCase,
            getRemoteGradeScaleUseCase = getRemoteGradeScaleUseCase,
            importRemoteGradeScaleIntoDbUseCase = importRemoteGradeScaleIntoDbUseCase,
            showSnackbarUseCase = showSnackbarUseCase,
            trackErrorUseCase = fakeTrackerUseCase,
        )

        // Observe states
        testMoleculeFlow(errorModel) {
            // First emission might be the default state with isLoading=true
            val loadingState = awaitItem()

            // Get the error state if it's not the first emission
            val errorState = if (loadingState.error == null) awaitItem() else loadingState

            // Verify error is captured
            assertEquals(testRemoteError.message, fakeTrackerUseCase.reportedErrors.value?.message)
            assertEquals(Res.string.import_grade_get_remote_grades_error, errorState.error)

            // Clean up
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldSelectCountryWhenSelectCountryCommandIsSent() = runTest {
        initSUT()
        backgroundScope.launchMolecule(mode = RecompositionMode.Immediate, body = { importUIModel.produceUI() }).test {
            // First get to stable state
            val initialState = awaitItem()

            // Skip loading state if necessary
            if (initialState.isLoading) {
                awaitItem()
            }

            // Act: select country
            importUIModel.sendCommand(ImportCommand.SelectCountry(testCountry))

            // Get state after selection
            val state = awaitItem()

            // Verify
            assertEquals(testCountry, state.selectedCountry)
            val selectedGradingScales = state.shownCountryGradingScales
            assertEquals(1, selectedGradingScales.size)
            assertEquals(testCountry, selectedGradingScales[0]?.country)

            // Clean up
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldLoadGradeScaleWhenOpenImportDialogCommandIsSent() = runTest {
        initSUT()
        testMoleculeFlow(importUIModel) {
            // First get to stable state
            val initialState = awaitItem()

            // Skip loading state if necessary
            if (initialState.isLoading) {
                awaitItem()
            }

            // Reset tracking for this test
            getRemoteGradeScaleUseCaseCalled = false
            lastCountryAndNameParam = null

            // Act: open import dialog
            importUIModel.sendCommand(ImportCommand.OpenImportDialog(testCountryAndName))

            // Loading state might be emitted
            val loadingState = awaitItem()

            // Get final state if needed
            val finalState = if (loadingState.isLoading) awaitItem() else loadingState

            // Verify
            assertTrue(getRemoteGradeScaleUseCaseCalled)
            assertEquals(testCountryAndName, lastCountryAndNameParam)
            assertEquals(testGradeScaleDTO, finalState.displayedGradeScaleDTO)
            assertFalse(finalState.isLoading)

            // Clean up
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldHandleErrorWhenLoadingGradeScaleFails() = runTest {
        // Create custom error case
        val errorUseCase = GetRemoteGradeScaleUseCase { countryAndName ->
            lastCountryAndNameParam = countryAndName
            testRemoteError.left()
        }

        // Create new model with error case
        val errorModel = ImportUIModelWithEvents(
            scope = this,
            getRemoteGradeScalesUseCase = getRemoteGradeScalesUseCase,
            getRemoteGradeScaleUseCase = errorUseCase,
            importRemoteGradeScaleIntoDbUseCase = importRemoteGradeScaleIntoDbUseCase,
            showSnackbarUseCase = showSnackbarUseCase,
            trackErrorUseCase = fakeTrackerUseCase,
        )

        testMoleculeFlow(errorModel) {
            // Get to stable state
            val initialState = awaitItem()

            // Skip loading state if necessary
            if (initialState.isLoading) {
                awaitItem()
            }

            // Act: open import dialog
            errorModel.sendCommand(ImportCommand.OpenImportDialog(testCountryAndName))

            // May get loading state
            val nextState = awaitItem()

            // Get error state if not already received
            val errorState = if (nextState.error == null) awaitItem() else nextState

            // Verify
            assertEquals(testRemoteError.message, fakeTrackerUseCase.reportedErrors.value?.message)
            assertEquals(Res.string.import_grade_open_import_dialog_error, errorState.error)
            assertFalse(errorState.isLoading)

            // Clean up
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldImportGradeScaleWhenImportGradeScaleCommandIsSent() = runTest {
        initSUT()
        testMoleculeFlow(importUIModel) {
            // Get to stable state
            awaitItem()

            // Given a displayed grade scale
            importUIModel.displayedGradeScaleDTO = testGradeScaleDTO

            // Act: import grade scale
            importUIModel.sendCommand(ImportCommand.ImportGradeScale)
            advanceUntilIdle()

            // Verify
            assertTrue(importRemoteGradeScaleIntoDbUseCaseCalled)
            assertEquals(testGradeScaleDTO, lastGradeScaleDTOParam)

            // Check event was dispatched - this might need to be a separate check

            // Final state should have isLoading=false
            val finalState = awaitItem()
            assertFalse(finalState.isLoading)

            // Clean up
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun shouldNotAttemptImportWhenNoGradeScaleIsDisplayed() = runTest {
        initSUT()
        testMoleculeFlow(importUIModel) {
            // Get to stable state
            val initialState = awaitItem()

            // Skip loading state if necessary
            if (initialState.isLoading) {
                awaitItem()
            }

            // Given no displayed grade scale
            importUIModel.displayedGradeScaleDTO = null

            // Act: import grade scale
            importUIModel.sendCommand(ImportCommand.ImportGradeScale)

            // Verify
            assertFalse(importRemoteGradeScaleIntoDbUseCaseCalled)
            assertNull(lastGradeScaleDTOParam)

            // Check no event was dispatched
            assertTrue(importUIModel.events.isEmpty)

            // Clean up
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shownCountryGradingScalesShouldReturnFilteredListWhenCountryIsSelected() = runTest {
        // Given multiple countries
        val anotherCountry = "France"
        val anotherCountryScales = CountryGradingScales(
            country = anotherCountry,
            gradesScalesNames = listOf("French Grading Scale"),
        )

        // Custom implementation for multiple countries
        val multipleCountriesUseCase = GetRemoteGradeScalesUseCase {
            persistentListOf(testCountryGradingScales, anotherCountryScales).right()
        }

        // Create model with multiple countries
        val multiCountryModel = ImportUIModelWithEvents(
            scope = this,
            getRemoteGradeScalesUseCase = multipleCountriesUseCase,
            getRemoteGradeScaleUseCase = getRemoteGradeScaleUseCase,
            importRemoteGradeScaleIntoDbUseCase = importRemoteGradeScaleIntoDbUseCase,
            showSnackbarUseCase = showSnackbarUseCase,
            trackErrorUseCase = fakeTrackerUseCase,
        )

        testMoleculeFlow(multiCountryModel) {
            // Get initial state
            val initialState = awaitItem()

            // Skip loading state if necessary
            val stableState = if (initialState.isLoading) awaitItem() else initialState

            // Initially, all countries should be shown
            assertEquals(2, stableState.countryGradingScales.size)
            assertEquals(2, stableState.shownCountryGradingScales.size)

            // Act: select a country
            multiCountryModel.sendCommand(ImportCommand.SelectCountry(testCountry))

            // Get updated state
            val filteredState = awaitItem()

            // Verify filtered list
            assertEquals(testCountry, filteredState.selectedCountry)
            assertEquals(1, filteredState.shownCountryGradingScales.size)
            assertEquals(testCountry, filteredState.shownCountryGradingScales[0]?.country)

            // Clean up
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldSetLoadingStateDuringOperations() = runTest {
        initSUT()
        // Track loading state changes

        testMoleculeFlow(importUIModel) {
            // Get initial state
            val initialState = awaitItem()

            // Skip initial loading state if necessary
            assertTrue(initialState.isLoading)
            val loaded = awaitItem()
            assertFalse { loaded.isLoading }
            importUIModel.sendCommand(ImportCommand.OpenImportDialog(testCountryAndName))
            // Should have final state with loading=false
            val finalState = awaitItem()
            assertFalse(finalState.isLoading)

            // Clean up
            cancelAndIgnoreRemainingEvents()
        }
    }
}
