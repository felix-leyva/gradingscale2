package de.felixlf.gradingscale2.features.list.creategradescaledialog

import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import app.cash.turbine.test
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.UpsertGradeScaleUseCase
import de.felixlf.gradingscale2.entities.util.MockGradeScalesGenerator
import de.felixlf.gradingscale2.moleculeTest
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CreateGradeScaleUIStateFactoryTest {
    private val testDispatcher = StandardTestDispatcher()
    private val mockGradeScales = MockGradeScalesGenerator().gradeScales
    private val getAllGradeScalesUseCase = GetAllGradeScalesUseCase {
        flowOf(mockGradeScales.toImmutableList())
    }
    private val defaultUpsertId = "2"
    private lateinit var savedName: String
    private lateinit var savedDefaultGradeName: String
    private val defaultUpsertGradeScaleUseCase = UpsertGradeScaleUseCase { gradeScaleName, gradeName ->
        delay(1)
        savedName = gradeScaleName
        savedDefaultGradeName = gradeName

        Result.success(defaultUpsertId)
    }

    private val existingGradeScaleNames = mockGradeScales.map {
        CreateGradeScaleUIState.GradeScaleNameAndId(
            name = it.gradeScaleName,
            id = it.id,
        )
    }.toImmutableList()

    private fun TestScope.setUseCase(
        upsertGradeScaleUseCase: UpsertGradeScaleUseCase = defaultUpsertGradeScaleUseCase,
    ) = CreateGradeScaleUIStateFactory(
        getAllGradeScalesUseCase = getAllGradeScalesUseCase,
        upsertGradeScaleUseCase = upsertGradeScaleUseCase,
        scope = this,
    )

    private fun TestScope.getUIState(factory: CreateGradeScaleUIStateFactory) =
        launchMolecule(mode = RecompositionMode.Immediate) {
            factory.produceUI()
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        savedName = ""
        savedDefaultGradeName = ""
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cancelChildren()
    }

    @Test
    fun `should return initial state and then the list of gradeScale names`() = moleculeTest {
        getUIState(setUseCase()).test {
            assertEquals(
                CreateGradeScaleUIState(
                    existingGradeScaleNames = persistentListOf(),
                    newName = "",
                    saveState = null,
                ),
                awaitItem(),
            )

            assertEquals(
                CreateGradeScaleUIState(
                    existingGradeScaleNames = existingGradeScaleNames,
                    newName = "",
                    saveState = null,
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `should set new name`() = moleculeTest {
        val useCase = setUseCase()
        getUIState(useCase).test {
            skipItems(2)
            useCase.sendEvent(CreateGradeScaleUIEvent.SetNewName("newName"))
            assertEquals(
                CreateGradeScaleUIState(
                    existingGradeScaleNames = existingGradeScaleNames,
                    newName = "newName",
                    saveState = null,
                ),
                awaitItem(),
            )

            useCase.sendEvent(CreateGradeScaleUIEvent.SetNewName("newName2"))
            assertEquals(
                CreateGradeScaleUIState(
                    existingGradeScaleNames = existingGradeScaleNames,
                    newName = "newName2",
                    saveState = null,
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `should set save state to loading and then to success`() = moleculeTest {
        val useCase = setUseCase()
        getUIState(useCase).test {
            skipItems(2)
            val gradeScaleName = "scale"
            val defaultGradeName = "grade"
            useCase.sendEvent(CreateGradeScaleUIEvent.SetNewName(gradeScaleName))
            assertEquals(
                CreateGradeScaleUIState(
                    existingGradeScaleNames = existingGradeScaleNames,
                    newName = gradeScaleName,
                    saveState = null,
                ),
                awaitItem(),
            )

            useCase.sendEvent(CreateGradeScaleUIEvent.Save(defaultGradeName))

            assertEquals(
                CreateGradeScaleUIState(
                    existingGradeScaleNames = existingGradeScaleNames,
                    newName = gradeScaleName,
                    saveState = CreateGradeScaleUIState.State.Loading,
                ),
                awaitItem(),
            )

            assertEquals(
                CreateGradeScaleUIState(
                    existingGradeScaleNames = existingGradeScaleNames,
                    newName = gradeScaleName,
                    saveState = CreateGradeScaleUIState.State.Success(defaultUpsertId),
                ),
                awaitItem(),
            )
            assertEquals(gradeScaleName, savedName)
            assertEquals(defaultGradeName, savedDefaultGradeName)
        }
    }

    @Test
    fun `should set save state to loading and then to error`() = moleculeTest {
        val useCase = setUseCase(
            upsertGradeScaleUseCase = UpsertGradeScaleUseCase { _, _ ->
                delay(1)
                Result.failure(Exception())
            },
        )
        getUIState(useCase).test {
            skipItems(2)
            val gradeScaleName = "scale"
            val defaultGradeName = "grade"
            useCase.sendEvent(CreateGradeScaleUIEvent.SetNewName(gradeScaleName))
            assertEquals(
                CreateGradeScaleUIState(
                    existingGradeScaleNames = existingGradeScaleNames,
                    newName = gradeScaleName,
                    saveState = null,
                ),
                awaitItem(),
            )

            useCase.sendEvent(CreateGradeScaleUIEvent.Save(defaultGradeName))

            assertEquals(
                CreateGradeScaleUIState(
                    existingGradeScaleNames = existingGradeScaleNames,
                    newName = gradeScaleName,
                    saveState = CreateGradeScaleUIState.State.Loading,
                ),
                awaitItem(),
            )

            assertEquals(
                CreateGradeScaleUIState(
                    existingGradeScaleNames = existingGradeScaleNames,
                    newName = gradeScaleName,
                    saveState = CreateGradeScaleUIState.State.Error,
                ),
                awaitItem(),
            )
        }
    }
}
