package de.felixlf.gradingscale2.features.calculator

import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import app.cash.turbine.test
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import de.felixlf.gradingscale2.entities.util.MockGradeScalesGenerator
import de.felixlf.gradingscale2.moleculeTest
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.flowOf
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CalculatorUIStateFactoryTest {

    private val mockGradeScales = MockGradeScalesGenerator().gradeScales
    private val gradeScaleIds = mockGradeScales.map { it.id }
    private val gradeScaleByIdUseCase = GetGradeScaleByIdUseCase { gradeScaleId ->
        flowOf(mockGradeScales.find { it.id == gradeScaleId })
    }
    private val getAllGradeScalesUseCase = GetAllGradeScalesUseCase {
        flowOf(mockGradeScales.toImmutableList())
    }

    private lateinit var factory: CalculatorUIStateFactory

    @BeforeTest
    fun setup() {
        factory = CalculatorUIStateFactory(getAllGradeScalesUseCase, gradeScaleByIdUseCase)
    }

    @Test
    fun `gradeScales are initialized from the usecases`() = moleculeTest {
        launchMolecule(RecompositionMode.Immediate) { factory.produceUI() }.test {
            awaitItem()
            // When
            val state = awaitItem()

            // Then
            assertEquals(null, state.selectedGradeScale)
            assertEquals(gradeScaleIds, state.gradeScalesNamesWithId.map { it.gradeScaleId })
        }
    }
}
