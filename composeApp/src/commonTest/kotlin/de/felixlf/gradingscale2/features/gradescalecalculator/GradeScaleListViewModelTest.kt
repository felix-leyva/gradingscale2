import app.cash.turbine.test
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import de.felixlf.gradingscale2.entities.util.MockGradeScalesGenerator
import de.felixlf.gradingscale2.features.gradescalecalculator.GradeScaleListViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
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

    private lateinit var viewModel: GradeScaleListViewModel
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `gradeScales are initialized from the usecases`() = runTest {
        viewModel = GradeScaleListViewModel(getAllGradeScalesUseCase, gradeScaleById)
        viewModel.uiState.test {
            awaitItem()
            val state = awaitItem()
            assertEquals(null, state.selectedGradeScale)
            assertEquals(gradeScaleIds, state.gradeScalesNamesWithId.map { it.gradeScaleId })
        }
    }

    @Test
    fun `selectGradeScale sets selectedGradeScaleId`() = runTest {
        viewModel = GradeScaleListViewModel(getAllGradeScalesUseCase, gradeScaleById)

        viewModel.uiState.test {
            skipItems(2)
            viewModel.selectGradeScale(mockGradeScales[0].gradeScaleName)
            val state = awaitItem()
            assertEquals(mockGradeScales[0], state.selectedGradeScale)
            assertEquals(gradeScaleIds, state.gradeScalesNamesWithId.map { it.gradeScaleId })
        }
    }

    @Test
    fun `setTotalPoints modifies the totalPoints of the selected gradescale`() = runTest {
        viewModel = GradeScaleListViewModel(getAllGradeScalesUseCase, gradeScaleById)

        viewModel.uiState.test {
            skipItems(2)
            viewModel.selectGradeScale(mockGradeScales[0].gradeScaleName)
            awaitItem()
            viewModel.setTotalPoints(20.0)
            val state = awaitItem()
            assertEquals(20.0, state.selectedGradeScale?.totalPoints)
            assertEquals(gradeScaleIds, state.gradeScalesNamesWithId.map { it.gradeScaleId })
        }
    }
}
