package de.felixlf.gradingscale2.entities.features.list.upsertgradescaledialog

import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import app.cash.turbine.test
import arrow.core.None
import arrow.core.Option
import de.felixlf.gradingscale2.entities.features.list.upsertgradescaledialog.UpsertGradeScaleUIState.State.Loaded
import de.felixlf.gradingscale2.entities.features.list.upsertgradescaledialog.UpsertGradeScaleUIState.State.Loading
import de.felixlf.gradingscale2.entities.features.list.upsertgradescaledialog.UpsertGradeScaleUIState.State.Operation.Insert
import de.felixlf.gradingscale2.entities.features.list.upsertgradescaledialog.UpsertGradeScaleUIState.State.Operation.Update
import de.felixlf.gradingscale2.entities.moleculeTest
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.InsertGradeScaleUseCase
import de.felixlf.gradingscale2.entities.usecases.UpdateGradeScaleUseCase
import de.felixlf.gradingscale2.entities.util.MockGradeScalesGenerator
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

@OptIn(ExperimentalCoroutinesApi::class)
class UpsertGradeScaleUIStateFactoryTest {
    private val testDispatcher = StandardTestDispatcher()
    private val mockGradeScales = MockGradeScalesGenerator().gradeScales
    private val getAllGradeScalesUseCase = GetAllGradeScalesUseCase {
        flowOf(mockGradeScales.toImmutableList())
    }
    private val defaultUpsertId = "2"
    private lateinit var savedName: String
    private lateinit var savedDefaultGradeName: String
    private val defaultInsertGradeScaleUseCase = InsertGradeScaleUseCase { gradeScaleName, gradeName ->
        delay(10)
        savedName = gradeScaleName
        savedDefaultGradeName = gradeName

        Option.invoke(defaultUpsertId)
    }

    private val defaultUpdateGradeScaleUseCase = UpdateGradeScaleUseCase { _, _, _ ->
        delay(10)
        Option.invoke(defaultUpsertId)
    }

    private val existingGradeScaleNames = mockGradeScales.map {
        UpsertGradeScaleUIState.GradeScaleNameAndId(
            name = it.gradeScaleName,
            id = it.id,
        )
    }.toImmutableList()

    private fun TestScope.setUseCase(
        insertGradeScaleUseCase: InsertGradeScaleUseCase = defaultInsertGradeScaleUseCase,
        updateGradeScaleUseCase: UpdateGradeScaleUseCase = defaultUpdateGradeScaleUseCase,
    ) = UpsertGradeScaleUIStateFactory(
        getAllGradeScalesUseCase = getAllGradeScalesUseCase,
        insertGradeScaleUseCase = insertGradeScaleUseCase,
        updateGradeScaleUseCase = updateGradeScaleUseCase,
        scope = this,
    )

    private fun TestScope.getUIState(factory: UpsertGradeScaleUIStateFactory) =
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
        val useCase = setUseCase()
        getUIState(useCase).test {
            assertEquals(
                UpsertGradeScaleUIState(
                    existingGradeScaleNames = persistentListOf(),
                    newName = "",
                    state = Loading,
                ),
                awaitItem(),
            )
            useCase.sendCommand(UpserGradeScaleUIEvent.SetOperation(Insert))
            assertEquals(
                UpsertGradeScaleUIState(
                    existingGradeScaleNames = existingGradeScaleNames,
                    newName = "",
                    state = Loaded(Insert),
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
            useCase.sendCommand(UpserGradeScaleUIEvent.SetNewName("newName"))
            assertEquals(
                UpsertGradeScaleUIState(
                    existingGradeScaleNames = existingGradeScaleNames,
                    newName = "newName",
                    state = Loading,
                ),
                awaitItem(),
            )
            useCase.sendCommand(UpserGradeScaleUIEvent.SetOperation(Insert))
            awaitItem()

            useCase.sendCommand(UpserGradeScaleUIEvent.SetNewName("newName2"))
            assertEquals(
                UpsertGradeScaleUIState(
                    existingGradeScaleNames = existingGradeScaleNames,
                    newName = "newName2",
                    state = Loaded(Insert),
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
            useCase.sendCommand(UpserGradeScaleUIEvent.SetNewName(gradeScaleName))
            assertEquals(
                UpsertGradeScaleUIState(
                    existingGradeScaleNames = existingGradeScaleNames,
                    newName = gradeScaleName,
                    state = Loading,
                ),
                awaitItem(),
            )

            useCase.sendCommand(UpserGradeScaleUIEvent.SetOperation(Insert))
            awaitItem()
            useCase.sendCommand(UpserGradeScaleUIEvent.Save(defaultGradeName))

            assertEquals(
                UpsertGradeScaleUIState(
                    existingGradeScaleNames = existingGradeScaleNames,
                    newName = gradeScaleName,
                    state = Loading,
                ),
                awaitItem(),
            )

            assertEquals(
                UpsertGradeScaleUIState(
                    existingGradeScaleNames = existingGradeScaleNames,
                    newName = gradeScaleName,
                    state = UpsertGradeScaleUIState.State.Success(defaultUpsertId),
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
            insertGradeScaleUseCase = InsertGradeScaleUseCase { _, _ ->
                delay(1)
                None
            },
        )
        getUIState(useCase).test {
            useCase.sendCommand(UpserGradeScaleUIEvent.SetOperation(Insert))
            skipItems(2)
            val gradeScaleName = "scale"
            val defaultGradeName = "grade"
            useCase.sendCommand(UpserGradeScaleUIEvent.SetNewName(gradeScaleName))
            assertEquals(
                UpsertGradeScaleUIState(
                    existingGradeScaleNames = existingGradeScaleNames,
                    newName = gradeScaleName,
                    state = Loaded(Insert),
                ),
                awaitItem(),
            )

            useCase.sendCommand(UpserGradeScaleUIEvent.Save(defaultGradeName))

            assertEquals(
                UpsertGradeScaleUIState(
                    existingGradeScaleNames = existingGradeScaleNames,
                    newName = gradeScaleName,
                    state = Loading,
                ),
                awaitItem(),
            )

            assertEquals(
                UpsertGradeScaleUIState(
                    existingGradeScaleNames = existingGradeScaleNames,
                    newName = gradeScaleName,
                    state = UpsertGradeScaleUIState.State.SaveError,
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `should set current grade scale id`() = moleculeTest {
        val useCase = setUseCase()
        getUIState(useCase).test {
            skipItems(2)
            val gradeScaleId = mockGradeScales[2].id
            useCase.sendCommand(UpserGradeScaleUIEvent.SetOperation(Update(gradeScaleId)))
            assertEquals(
                UpsertGradeScaleUIState(
                    existingGradeScaleNames = existingGradeScaleNames,
                    newName = mockGradeScales[2].gradeScaleName,
                    state = Loaded(Update(gradeScaleId)),
                ),
                awaitItem(),
            )
        }
    }
}
