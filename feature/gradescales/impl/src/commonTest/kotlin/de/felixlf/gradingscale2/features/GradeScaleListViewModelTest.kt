package de.felixlf.gradingscale2.features

import app.cash.turbine.test
import arrow.core.Option
import de.felixlf.gradingscale2.entities.features.list.GradeListUIModel
import de.felixlf.gradingscale2.entities.features.list.GradeScaleListUIEvent
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import de.felixlf.gradingscale2.entities.usecases.GetLastSelectedGradeScaleIdUseCase
import de.felixlf.gradingscale2.entities.usecases.SetLastSelectedGradeScaleIdUseCase
import de.felixlf.gradingscale2.entities.util.MockGradeScalesGenerator
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class GradeScaleListViewModelTest {

    private val mockGradeScales = MockGradeScalesGenerator().gradeScales
    private val gradeScaleIds = mockGradeScales.map { it.id }
    private val gradeScaleById = GetGradeScaleByIdUseCase { gradeScaleId ->
        flowOf(mockGradeScales.find { it.id == gradeScaleId })
    }
    private val getAllGradeScalesUseCase = GetAllGradeScalesUseCase {
        flowOf(mockGradeScales.toImmutableList())
    }

    private fun TestScope.setupSUT(
        getLastSelectedGradeScaleIdUseCase: GetLastSelectedGradeScaleIdUseCase = GetLastSelectedGradeScaleIdUseCase { null },
        setLastSelectedGradeScaleIdUseCase: SetLastSelectedGradeScaleIdUseCase = SetLastSelectedGradeScaleIdUseCase { Option(Unit) },
    ): GradeListUIModel {
        return GradeListUIModel(
            allGradeScalesUseCase = getAllGradeScalesUseCase,
            getGradeScaleByIdUseCase = gradeScaleById,
            getLastSelectedGradeScaleIdUseCase = getLastSelectedGradeScaleIdUseCase,
            setLastSelectedGradeScaleIdUseCase = setLastSelectedGradeScaleIdUseCase,
            stateProducer = TestStateProducer(backgroundScope),
        )
    }

    @Test
    fun `gradeScales are initialized from the usecases`() = runTest {
        val viewModel = setupSUT()
        viewModel.uiState.test {
            awaitItem()
            val state = awaitItem()
            assertEquals(null, state.selectedGradeScale)
            assertEquals(gradeScaleIds, state.gradeScalesNamesWithId.map { it.gradeScaleId })
        }
    }

    @Test
    fun `selectGradeScale sets selectedGradeScaleId`() = runTest {
        val viewModel = setupSUT()
        viewModel.uiState.test {
            skipItems(2)
            viewModel.sendCommand(GradeScaleListUIEvent.SelectGradeScale(mockGradeScales[0].gradeScaleName))
            val state = awaitItem()
            assertEquals(mockGradeScales[0], state.selectedGradeScale)
            assertEquals(gradeScaleIds, state.gradeScalesNamesWithId.map { it.gradeScaleId })
        }
    }

    @Test
    fun `setTotalPoints modifies the totalPoints of the selected gradescale`() = runTest {
        val viewModel = setupSUT()
        viewModel.uiState.test {
            awaitItem()
            awaitItem()

            viewModel.sendCommand(GradeScaleListUIEvent.SelectGradeScale(mockGradeScales[0].gradeScaleName))
            awaitItem()
            viewModel.sendCommand(GradeScaleListUIEvent.SetTotalPoints(20.0))
            val state = awaitItem()
            assertEquals(20.0, state.selectedGradeScale?.totalPoints)
            assertEquals(gradeScaleIds, state.gradeScalesNamesWithId.map { it.gradeScaleId })
        }
    }
}
