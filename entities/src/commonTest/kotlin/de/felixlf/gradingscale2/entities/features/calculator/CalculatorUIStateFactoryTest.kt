package de.felixlf.gradingscale2.entities.features.calculator

import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import app.cash.turbine.test
import arrow.core.Option
import de.felixlf.gradingscale2.entities.moleculeTest
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import de.felixlf.gradingscale2.entities.usecases.GetLastSelectedGradeScaleId
import de.felixlf.gradingscale2.entities.usecases.SetLastSelectedGradeScaleId
import de.felixlf.gradingscale2.entities.util.MockGradeScalesGenerator
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
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
        getLastSelectedGradeScaleId: GetLastSelectedGradeScaleId = GetLastSelectedGradeScaleId { null },
        setLastSelectedGradeScaleId: SetLastSelectedGradeScaleId = SetLastSelectedGradeScaleId { Option(Unit) },
    ) {
        factory = CalculatorUIModel(
            scope = this,
            allGradeScalesUseCase = getAllGradeScalesUseCase,
            getGradeScaleByIdUseCase = gradeScaleByIdUseCase,
            getLastSelectedGradeScaleIdUseCase = getLastSelectedGradeScaleId,
            setLastSelectedGradeScaleIdUseCase = setLastSelectedGradeScaleId,
        )
    }

    @Test
    fun `gradeScales are initialized from the usecases`() = moleculeTest {
        setupSUT()
        launchMolecule(RecompositionMode.Immediate) { factory.produceUI() }.test {
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
    fun `selectGradeScale sets selectedGradeScaleId`() = moleculeTest {
        setupSUT()
        launchMolecule(RecompositionMode.Immediate) { factory.produceUI() }.test {
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
    fun `setTotalPoints sets totalPoints`() = moleculeTest {
        setupSUT()
        launchMolecule(RecompositionMode.Immediate) { factory.produceUI() }.test {
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
    fun `setPercentage sets percentage`() = moleculeTest {
        setupSUT()
        launchMolecule(RecompositionMode.Immediate) { factory.produceUI() }.test {
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
    fun `setPoints sets points`() = moleculeTest {
        setupSUT()
        launchMolecule(RecompositionMode.Immediate) { factory.produceUI() }.test {
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
    fun `setGradeName sets grade name`() = moleculeTest {
        setupSUT()
        launchMolecule(RecompositionMode.Immediate) { factory.produceUI() }.test {
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
}
