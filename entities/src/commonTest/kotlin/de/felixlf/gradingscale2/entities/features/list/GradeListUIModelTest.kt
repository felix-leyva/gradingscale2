package de.felixlf.gradingscale2.entities.features.list

import app.cash.turbine.test
import arrow.core.None
import arrow.core.Some
import de.felixlf.gradingscale2.entities.TestStateProducer
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.usecases.DeleteGradeScaleUseCase
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import de.felixlf.gradingscale2.entities.usecases.GetLastSelectedGradeScaleIdUseCase
import de.felixlf.gradingscale2.entities.usecases.SetLastSelectedGradeScaleIdUseCase
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GradeListUIModelTest {

    private val scale1 = GradeScale(
        id = "scale-1",
        gradeScaleName = "Standard Scale",
        totalPoints = 100.0,
        grades = persistentListOf(
            Grade(namedGrade = "1", percentage = 0.9, idOfGradeScale = "scale-1", nameOfScale = "Standard Scale", uuid = "u1"),
            Grade(namedGrade = "2", percentage = 0.8, idOfGradeScale = "scale-1", nameOfScale = "Standard Scale", uuid = "u2"),
        ),
    )

    private val scale2 = GradeScale(
        id = "scale-2",
        gradeScaleName = "Gymnasium Scale",
        totalPoints = 50.0,
        grades = persistentListOf(
            Grade(namedGrade = "Sehr Gut", percentage = 0.95, idOfGradeScale = "scale-2", nameOfScale = "Gymnasium Scale", uuid = "u3"),
        ),
    )

    private val scalesFlow = MutableStateFlow<List<GradeScale>>(listOf(scale1, scale2))
    private var lastSavedSelectedId: String? = null
    private var deletedScaleId: String? = null

    private lateinit var getAllGradeScalesUseCase: GetAllGradeScalesUseCase
    private lateinit var getGradeScaleByIdUseCase: GetGradeScaleByIdUseCase
    private lateinit var getLastSelectedGradeScaleIdUseCase: GetLastSelectedGradeScaleIdUseCase
    private lateinit var setLastSelectedGradeScaleIdUseCase: SetLastSelectedGradeScaleIdUseCase
    private lateinit var deleteGradeScaleUseCase: DeleteGradeScaleUseCase

    @BeforeTest
    fun setUp() {
        scalesFlow.value = listOf(scale1, scale2)
        lastSavedSelectedId = null
        deletedScaleId = null

        getAllGradeScalesUseCase = GetAllGradeScalesUseCase {
            scalesFlow.map { it.toImmutableList() }
        }

        getGradeScaleByIdUseCase = GetGradeScaleByIdUseCase { id ->
            scalesFlow.map { list -> list.firstOrNull { it.id == id } }
        }

        getLastSelectedGradeScaleIdUseCase = GetLastSelectedGradeScaleIdUseCase {
            lastSavedSelectedId
        }

        setLastSelectedGradeScaleIdUseCase = SetLastSelectedGradeScaleIdUseCase { id ->
            lastSavedSelectedId = id
            Some(Unit)
        }

        deleteGradeScaleUseCase = DeleteGradeScaleUseCase { id ->
            deletedScaleId = id
            scalesFlow.value = scalesFlow.value.filterNot { it.id == id }
            Some(Unit)
        }
    }

    private fun TestScope.createSUT(initialSavedId: String? = null): GradeListUIModel {
        lastSavedSelectedId = initialSavedId
        return GradeListUIModel(
            stateProducer = TestStateProducer(backgroundScope),
            allGradeScalesUseCase = getAllGradeScalesUseCase,
            getGradeScaleByIdUseCase = getGradeScaleByIdUseCase,
            getLastSelectedGradeScaleIdUseCase = getLastSelectedGradeScaleIdUseCase,
            setLastSelectedGradeScaleIdUseCase = setLastSelectedGradeScaleIdUseCase,
            deleteGradeScaleUseCase = deleteGradeScaleUseCase,
        )
    }

    @Test
    fun initialState_loadsGradeScalesList() = runTest {
        val sut = createSUT()

        sut.uiState.test {
            awaitItem() // initial empty/default state
            val loadedState = awaitItem()

            assertEquals(2, loadedState.gradeScalesNamesWithId.size)
            assertEquals("Standard Scale", loadedState.gradeScalesNamesWithId[0].gradeScaleName)
            assertEquals("scale-1", loadedState.gradeScalesNamesWithId[0].gradeScaleId)
            assertEquals("Gymnasium Scale", loadedState.gradeScalesNamesWithId[1].gradeScaleName)
            assertEquals("scale-2", loadedState.gradeScalesNamesWithId[1].gradeScaleId)
            assertNull(loadedState.selectedGradeScale)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun initialState_withSavedId_restoresSelectedGradeScale() = runTest {
        val sut = createSUT(initialSavedId = "scale-2")

        sut.uiState.test {
            awaitItem() // initial
            var state = awaitItem()
            if (state.selectedGradeScale == null) {
                state = awaitItem()
            }

            assertEquals("scale-2", state.selectedGradeScale?.id)
            assertEquals("Gymnasium Scale", state.selectedGradeScale?.gradeScaleName)
            // Default totalPoints is 10.0 in GradeListUIModel
            assertEquals(10.0, state.selectedGradeScale?.totalPoints)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun selectGradeScale_byName_updatesSelectionAndPersists() = runTest {
        val sut = createSUT()

        sut.uiState.test {
            awaitItem()
            awaitItem() // loaded scales

            sut.sendCommand(GradeScaleListUIEvent.SelectGradeScale("Standard Scale"))
            testScheduler.runCurrent()
            var selectedState = awaitItem()
            if (selectedState.selectedGradeScale == null) {
                selectedState = awaitItem()
            }

            assertEquals("scale-1", selectedState.selectedGradeScale?.id)
            assertEquals("scale-1", lastSavedSelectedId)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun selectGradeScaleById_updatesSelectedScale() = runTest {
        val sut = createSUT()

        sut.uiState.test {
            awaitItem()
            awaitItem()

            sut.sendCommand(GradeScaleListUIEvent.SelectGradeScaleById("scale-2"))
            testScheduler.runCurrent()
            var state = awaitItem()
            if (state.selectedGradeScale == null) {
                state = awaitItem()
            }

            assertEquals("scale-2", state.selectedGradeScale?.id)
            assertEquals("Gymnasium Scale", state.selectedGradeScale?.gradeScaleName)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setTotalPoints_validPoints_updatesTotalPoints() = runTest {
        val sut = createSUT(initialSavedId = "scale-1")

        sut.uiState.test {
            awaitItem()
            var state = awaitItem()
            if (state.selectedGradeScale == null) state = awaitItem()
            assertEquals(10.0, state.selectedGradeScale?.totalPoints)

            sut.sendCommand(GradeScaleListUIEvent.SetTotalPoints(45.5))
            val updatedState = awaitItem()

            assertEquals(45.5, updatedState.selectedGradeScale?.totalPoints)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setTotalPoints_zeroOrNegative_isIgnored() = runTest {
        val sut = createSUT(initialSavedId = "scale-1")

        sut.uiState.test {
            awaitItem()
            var state = awaitItem()
            if (state.selectedGradeScale == null) state = awaitItem()

            sut.sendCommand(GradeScaleListUIEvent.SetTotalPoints(0.0))
            sut.sendCommand(GradeScaleListUIEvent.SetTotalPoints(-5.0))

            // State should remain 10.0
            assertEquals(10.0, state.selectedGradeScale?.totalPoints)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteGradeScale_deletesAndSelectsNextScale() = runTest {
        val sut = createSUT(initialSavedId = "scale-1")

        sut.uiState.test {
            awaitItem()
            var state = awaitItem()
            if (state.selectedGradeScale == null) state = awaitItem()
            assertEquals("scale-1", state.selectedGradeScale?.id)

            sut.sendCommand(GradeScaleListUIEvent.DeleteGradeScale("scale-1"))
            testScheduler.runCurrent()
            var afterDeleteState = awaitItem()
            if (afterDeleteState.selectedGradeScale?.id != "scale-2") {
                afterDeleteState = awaitItem()
            }

            assertEquals("scale-1", deletedScaleId)
            assertEquals("scale-2", afterDeleteState.selectedGradeScale?.id)
            assertEquals("scale-2", lastSavedSelectedId)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun recompositionsOnSetTotalPoints_doNotRecreateOrRecollectFlows() = runTest {
        var allGradeScalesCalls = 0
        var allGradeScalesCollections = 0
        var getScaleByIdCalls = 0
        var getScaleByIdCollections = 0

        val countingAllScalesUseCase = GetAllGradeScalesUseCase {
            allGradeScalesCalls++
            scalesFlow.map { it.toImmutableList() }.onStart { allGradeScalesCollections++ }
        }
        val countingScaleByIdUseCase = GetGradeScaleByIdUseCase { id ->
            getScaleByIdCalls++
            scalesFlow.map { list -> list.firstOrNull { it.id == id } }.onStart { getScaleByIdCollections++ }
        }

        val sut = GradeListUIModel(
            stateProducer = TestStateProducer(backgroundScope),
            allGradeScalesUseCase = countingAllScalesUseCase,
            getGradeScaleByIdUseCase = countingScaleByIdUseCase,
            getLastSelectedGradeScaleIdUseCase = GetLastSelectedGradeScaleIdUseCase { "scale-1" },
            setLastSelectedGradeScaleIdUseCase = setLastSelectedGradeScaleIdUseCase,
            deleteGradeScaleUseCase = deleteGradeScaleUseCase,
        )

        sut.uiState.test {
            var current = awaitItem()
            while (current.selectedGradeScale?.id != "scale-1") {
                current = awaitItem()
            }

            sut.sendCommand(GradeScaleListUIEvent.SetTotalPoints(20.0))
            while (current.selectedGradeScale?.totalPoints != 20.0) {
                current = awaitItem()
            }

            sut.sendCommand(GradeScaleListUIEvent.SetTotalPoints(30.0))
            while (current.selectedGradeScale?.totalPoints != 30.0) {
                current = awaitItem()
            }

            sut.sendCommand(GradeScaleListUIEvent.SetTotalPoints(40.0))
            while (current.selectedGradeScale?.totalPoints != 40.0) {
                current = awaitItem()
            }

            assertEquals(1, allGradeScalesCalls)
            assertEquals(1, allGradeScalesCollections)
            assertEquals(1, getScaleByIdCalls)
            assertEquals(1, getScaleByIdCollections)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun slowPreferenceLoadDoesNotOverwriteEarlierScaleSelection() = runTest {
        val savedIdDeferred = CompletableDeferred<String?>()
        val sut = GradeListUIModel(
            stateProducer = TestStateProducer(backgroundScope),
            allGradeScalesUseCase = getAllGradeScalesUseCase,
            getGradeScaleByIdUseCase = getGradeScaleByIdUseCase,
            getLastSelectedGradeScaleIdUseCase = GetLastSelectedGradeScaleIdUseCase { savedIdDeferred.await() },
            setLastSelectedGradeScaleIdUseCase = setLastSelectedGradeScaleIdUseCase,
            deleteGradeScaleUseCase = deleteGradeScaleUseCase,
        )

        sut.uiState.test {
            awaitItem()
            awaitItem()

            sut.sendCommand(GradeScaleListUIEvent.SelectGradeScaleById("scale-2"))
            runCurrent()
            assertEquals("scale-2", sut.uiState.value.selectedGradeScale?.id)

            savedIdDeferred.complete("scale-1")
            runCurrent()
            assertEquals("scale-2", sut.uiState.value.selectedGradeScale?.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun selectGradeScaleByIdPersistsSelectedId() = runTest {
        val sut = createSUT()
        sut.uiState.test {
            awaitItem()
            awaitItem()

            sut.sendCommand(GradeScaleListUIEvent.SelectGradeScaleById("scale-2"))
            runCurrent()

            assertEquals("scale-2", sut.uiState.value.selectedGradeScale?.id)
            assertEquals("scale-2", lastSavedSelectedId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun failedDeleteGradeScaleLeavesSelectionAndSavedPreferenceUnchanged() = runTest {
        deleteGradeScaleUseCase = DeleteGradeScaleUseCase { None }
        val sut = createSUT(initialSavedId = "scale-1")

        sut.uiState.test {
            var current = awaitItem()
            while (current.selectedGradeScale?.id != "scale-1") {
                current = awaitItem()
            }

            sut.sendCommand(GradeScaleListUIEvent.DeleteGradeScale("scale-1"))
            runCurrent()

            assertEquals("scale-1", sut.uiState.value.selectedGradeScale?.id)
            assertEquals("scale-1", lastSavedSelectedId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deletingLastRemainingScaleClearsSelectionAndSavedPreference() = runTest {
        scalesFlow.value = listOf(scale1)
        val sut = createSUT(initialSavedId = "scale-1")

        sut.uiState.test {
            var current = awaitItem()
            while (current.selectedGradeScale?.id != "scale-1") {
                current = awaitItem()
            }

            sut.sendCommand(GradeScaleListUIEvent.DeleteGradeScale("scale-1"))
            runCurrent()

            assertNull(sut.uiState.value.selectedGradeScale)
            assertEquals("", lastSavedSelectedId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun setTotalPointsIgnoresNonFiniteValues() = runTest {
        val sut = createSUT(initialSavedId = "scale-1")
        sut.uiState.test {
            var current = awaitItem()
            while (current.selectedGradeScale?.id != "scale-1") {
                current = awaitItem()
            }

            sut.sendCommand(GradeScaleListUIEvent.SetTotalPoints(Double.POSITIVE_INFINITY))
            sut.sendCommand(GradeScaleListUIEvent.SetTotalPoints(Double.NaN))
            runCurrent()

            assertEquals(10.0, sut.uiState.value.selectedGradeScale?.totalPoints)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
