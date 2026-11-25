package de.felixlf.gradingscale2.entities.features.calculator

import app.cash.turbine.test
import arrow.core.Option
import de.felixlf.gradingscale2.entities.TestStateProducer
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import de.felixlf.gradingscale2.entities.usecases.GetLastSelectedGradeScaleIdUseCase
import de.felixlf.gradingscale2.entities.usecases.SetLastSelectedGradeScaleIdUseCase
import de.felixlf.gradingscale2.entities.util.MockGradeScalesGenerator
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CalculatorUIStateFactoryTest {

    private val mockGradeScales = MockGradeScalesGenerator().gradeScales
    private val gradeScaleIds = mockGradeScales.map { it.id }
    private val gradeScaleByIdUseCase = GetGradeScaleByIdUseCase { gradeScaleId ->
        flowOf(mockGradeScales.find { it.id == gradeScaleId })
    }
    private val getAllGradeScalesUseCase = GetAllGradeScalesUseCase {
        flowOf(mockGradeScales.toImmutableList())
    }

    private lateinit var factory: CalculatorUIModel

    private fun TestScope.setupSUT(
        getLastSelectedGradeScaleIdUseCase: GetLastSelectedGradeScaleIdUseCase = GetLastSelectedGradeScaleIdUseCase { null },
        setLastSelectedGradeScaleIdUseCase: SetLastSelectedGradeScaleIdUseCase = SetLastSelectedGradeScaleIdUseCase { Option(Unit) },
    ) {
        factory = CalculatorUIModel(
            stateProducer = TestStateProducer(backgroundScope),
            allGradeScalesUseCase = getAllGradeScalesUseCase,
            getGradeScaleByIdUseCase = gradeScaleByIdUseCase,
            getLastSelectedGradeScaleIdUseCase = getLastSelectedGradeScaleIdUseCase,
            setLastSelectedGradeScaleIdUseCase = setLastSelectedGradeScaleIdUseCase,
        )
    }

    @Test
    fun `gradeScales are initialized from the usecases`() = runTest {
        setupSUT()
        factory.uiState.test {
            val emptyState = awaitItem()
            assertEquals(null, emptyState.selectedGradeScale)
            assertTrue(emptyState.gradeScalesNamesWithId.isEmpty())

            // When
            val state = awaitItem()

            // Then
            assertEquals(null, state.selectedGradeScale)
            assertEquals(gradeScaleIds, state.gradeScalesNamesWithId.map { it.gradeScaleId })
        }
    }

    @Test
    fun `selectGradeScale sets selectedGradeScaleId`() = runTest {
        setupSUT()
        factory.uiState.test {
            awaitItem()
            val state = awaitItem()
            assertEquals(null, state.selectedGradeScale)

            // When
            factory.sendCommand(CalculatorUIEvent.SelectGradeScale(state.gradeScalesNames.first()))

            // Then
            val newState = awaitItem()
            assertEquals(mockGradeScales.first(), newState.selectedGradeScale)

            // When
            factory.sendCommand(CalculatorUIEvent.SelectGradeScale(newState.gradeScalesNames.last()))
            assertEquals(mockGradeScales.last(), awaitItem().selectedGradeScale)
        }
    }

    @Test
    fun `setTotalPoints sets totalPoints`() = runTest {
        setupSUT()
        factory.uiState.test {
            awaitItem()
            val state = awaitItem()
            factory.sendCommand(CalculatorUIEvent.SelectGradeScale(state.gradeScalesNames.first()))

            // When
            factory.sendCommand(CalculatorUIEvent.SetTotalPoints(20.0))

            // Then
            assertEquals(20.0, awaitItem().totalPoints)
        }
    }

    @Test
    fun `setPercentage sets percentage`() = runTest {
        setupSUT()
        factory.uiState.test {
            awaitItem()
            val state = awaitItem()
            factory.sendCommand(CalculatorUIEvent.SelectGradeScale(state.gradeScalesNames.first()))
            awaitItem()
            // When
            factory.sendCommand(CalculatorUIEvent.SetPercentage(0.51))

            // Then
            with(awaitItem()) {
                assertEquals(0.51, currentPercentage)
                assertEquals(5.1, currentGrade?.points)
                assertEquals(0.51, currentGrade?.percentage)
                assertEquals(mockGradeScales.first().gradeByPercentage(0.51).namedGrade, currentGrade?.grade?.namedGrade)
            }
        }
    }

    @Test
    fun `setPoints sets points`() = runTest {
        setupSUT()
        factory.uiState.test {
            awaitItem()
            val state = awaitItem()
            factory.sendCommand(CalculatorUIEvent.SelectGradeScale(state.gradeScalesNames.first()))
            awaitItem()
            // When
            factory.sendCommand(CalculatorUIEvent.SetPoints(5.1))

            // Then
            with(awaitItem()) {
                assertEquals(0.51, currentPercentage)
                assertEquals(5.1, currentGrade?.points)
                assertEquals(0.51, currentGrade?.percentage)
                assertEquals(mockGradeScales.first().gradeByPoints(5.1).namedGrade, currentGrade?.grade?.namedGrade)
            }
        }
    }

    @Test
    fun `setGradeName sets grade name`() = runTest {
        setupSUT()
        factory.uiState.test {
            awaitItem()
            val state = awaitItem()
            factory.sendCommand(CalculatorUIEvent.SelectGradeScale(state.gradeScalesNames.first()))
            awaitItem()
            // When
            factory.sendCommand(CalculatorUIEvent.SetGradeName(mockGradeScales.first().gradeByPercentage(0.9).namedGrade))

            // Then
            with(awaitItem()) {
                assertEquals(0.9, currentPercentage)
                assertEquals(9.0, currentGrade?.points)
                assertEquals(0.9, currentGrade?.percentage)
                assertEquals(mockGradeScales.first().gradeByPercentage(0.9).namedGrade, currentGrade?.grade?.namedGrade)
            }
        }
    }

    @Test
    fun `recompositions on local state updates do not recreate or re-collect flows`() = runTest {
        var allGradeScalesCalls = 0
        var allGradeScalesCollections = 0
        var getScaleByIdCalls = 0
        var getScaleByIdCollections = 0

        val countingAllScalesUseCase = GetAllGradeScalesUseCase {
            allGradeScalesCalls++
            flowOf(mockGradeScales.toImmutableList()).onStart { allGradeScalesCollections++ }
        }
        val countingScaleByIdUseCase = GetGradeScaleByIdUseCase { id ->
            getScaleByIdCalls++
            flowOf(mockGradeScales.find { it.id == id }).onStart { getScaleByIdCollections++ }
        }

        val sut = CalculatorUIModel(
            stateProducer = TestStateProducer(backgroundScope),
            allGradeScalesUseCase = countingAllScalesUseCase,
            getGradeScaleByIdUseCase = countingScaleByIdUseCase,
            getLastSelectedGradeScaleIdUseCase = GetLastSelectedGradeScaleIdUseCase { null },
            setLastSelectedGradeScaleIdUseCase = SetLastSelectedGradeScaleIdUseCase { Option(Unit) },
        )

        sut.uiState.test {
            var current = awaitItem()
            while (current.gradeScalesNamesWithId.isEmpty()) {
                current = awaitItem()
            }

            sut.sendCommand(CalculatorUIEvent.SelectGradeScale(current.gradeScalesNames.first()))
            while (current.selectedGradeScale == null) {
                current = awaitItem()
            }

            sut.sendCommand(CalculatorUIEvent.SetTotalPoints(20.0))
            while (current.totalPoints != 20.0) {
                current = awaitItem()
            }

            sut.sendCommand(CalculatorUIEvent.SetPercentage(0.75))
            while (current.currentPercentage != 0.75) {
                current = awaitItem()
            }

            sut.sendCommand(CalculatorUIEvent.SetPoints(10.0))
            while (current.currentPercentage != 0.5) {
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
    fun `slow preference load finishing after explicit scale selection does not overwrite selection`() = runTest {
        val savedIdDeferred = CompletableDeferred<String?>()
        setupSUT(getLastSelectedGradeScaleIdUseCase = { savedIdDeferred.await() })

        factory.uiState.test {
            var current = awaitItem()
            while (current.gradeScalesNamesWithId.isEmpty()) {
                current = awaitItem()
            }

            val chosenScale = mockGradeScales.last()
            factory.sendCommand(CalculatorUIEvent.SelectGradeScale(chosenScale.gradeScaleName))
            runCurrent()
            assertEquals(chosenScale.id, factory.uiState.value.selectedGradeScale?.id)

            // Late preference completion (both stale ID or null) must not clobber explicit choice.
            savedIdDeferred.complete(mockGradeScales.first().id)
            runCurrent()
            assertEquals(chosenScale.id, factory.uiState.value.selectedGradeScale?.id)
            cancelAndIgnoreRemainingEvents()
        }

        val nullIdDeferred = CompletableDeferred<String?>()
        setupSUT(getLastSelectedGradeScaleIdUseCase = { nullIdDeferred.await() })
        factory.uiState.test {
            var current = awaitItem()
            while (current.gradeScalesNamesWithId.isEmpty()) {
                current = awaitItem()
            }

            val chosenScale = mockGradeScales.last()
            factory.sendCommand(CalculatorUIEvent.SelectGradeScale(chosenScale.gradeScaleName))
            runCurrent()
            assertEquals(chosenScale.id, factory.uiState.value.selectedGradeScale?.id)

            nullIdDeferred.complete(null)
            runCurrent()
            assertEquals(chosenScale.id, factory.uiState.value.selectedGradeScale?.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `non-finite inputs and unknown grade name do not corrupt calculator state`() = runTest {
        setupSUT()
        factory.uiState.test {
            var current = awaitItem()
            while (current.gradeScalesNamesWithId.isEmpty()) {
                current = awaitItem()
            }
            factory.sendCommand(CalculatorUIEvent.SelectGradeScale(current.gradeScalesNames.first()))
            factory.sendCommand(CalculatorUIEvent.SetTotalPoints(20.0))
            factory.sendCommand(CalculatorUIEvent.SetPercentage(0.75))
            runCurrent()

            assertEquals(20.0, factory.uiState.value.totalPoints)
            assertEquals(0.75, factory.uiState.value.currentPercentage)

            factory.sendCommand(CalculatorUIEvent.SetTotalPoints(Double.POSITIVE_INFINITY))
            factory.sendCommand(CalculatorUIEvent.SetTotalPoints(Double.NaN))
            factory.sendCommand(CalculatorUIEvent.SetPercentage(Double.NaN))
            factory.sendCommand(CalculatorUIEvent.SetPoints(Double.NaN))
            factory.sendCommand(CalculatorUIEvent.SetGradeName("NonExistentGrade"))
            runCurrent()

            assertEquals(20.0, factory.uiState.value.totalPoints)
            assertEquals(0.75, factory.uiState.value.currentPercentage)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
