package de.felixlf.gradingscale2.entities.features.list.upsertgradescaledialog

import app.cash.turbine.test
import arrow.core.None
import arrow.core.Option
import de.felixlf.gradingscale2.entities.TestStateProducer
import de.felixlf.gradingscale2.entities.features.list.upsertgradescaledialog.UpsertGradeScaleUIState.State.Loaded
import de.felixlf.gradingscale2.entities.features.list.upsertgradescaledialog.UpsertGradeScaleUIState.State.Loading
import de.felixlf.gradingscale2.entities.features.list.upsertgradescaledialog.UpsertGradeScaleUIState.State.Operation.Insert
import de.felixlf.gradingscale2.entities.features.list.upsertgradescaledialog.UpsertGradeScaleUIState.State.Operation.Update
import de.felixlf.gradingscale2.entities.models.GradeScaleNameAndId
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.InsertGradeScaleUseCase
import de.felixlf.gradingscale2.entities.usecases.ShowSnackbarUseCase
import de.felixlf.gradingscale2.entities.usecases.UpdateGradeScaleUseCase
import de.felixlf.gradingscale2.entities.util.MockGradeScalesGenerator
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
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
        GradeScaleNameAndId(
            name = it.gradeScaleName,
            id = it.id,
        )
    }.toImmutableList()

    private val mockShowSnackbarUseCase = ShowSnackbarUseCase { _, _, _ ->
        ShowSnackbarUseCase.SnackbarResult.ActionPerformed
    }

    private fun TestScope.setUseCase(
        insertGradeScaleUseCase: InsertGradeScaleUseCase = defaultInsertGradeScaleUseCase,
        updateGradeScaleUseCase: UpdateGradeScaleUseCase = defaultUpdateGradeScaleUseCase,
    ) = UpsertGradeScaleUIStateFactory(
        getAllGradeScalesUseCase = getAllGradeScalesUseCase,
        insertGradeScaleUseCase = insertGradeScaleUseCase,
        updateGradeScaleUseCase = updateGradeScaleUseCase,
        showSnackbarUseCase = mockShowSnackbarUseCase,
        stateProducer = TestStateProducer(backgroundScope),
    )

    private fun TestScope.getUIState(factory: UpsertGradeScaleUIStateFactory) =
        factory.uiState

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
    fun `should return initial state and then the list of gradeScale names`() = runTest {
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
    fun `should set new name`() = runTest {
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
    fun `should set save state to loading and then to success`() = runTest {
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
    fun `should set save state to loading and then to error`() = runTest {
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
    fun `should set current grade scale id`() = runTest {
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

    @Test
    fun `recompositions on SetNewName do not recreate or re-collect getAllGradeScalesUseCase flow`() = runTest {
        var allGradeScalesCalls = 0
        var allGradeScalesCollections = 0
        val countingUseCase = GetAllGradeScalesUseCase {
            allGradeScalesCalls++
            flowOf(mockGradeScales.toImmutableList()).onStart { allGradeScalesCollections++ }
        }
        val sut = UpsertGradeScaleUIStateFactory(
            getAllGradeScalesUseCase = countingUseCase,
            insertGradeScaleUseCase = defaultInsertGradeScaleUseCase,
            updateGradeScaleUseCase = defaultUpdateGradeScaleUseCase,
            showSnackbarUseCase = mockShowSnackbarUseCase,
            stateProducer = TestStateProducer(backgroundScope),
        )

        sut.uiState.test {
            var current = awaitItem()
            while (current.existingGradeScaleNames.isEmpty()) {
                current = awaitItem()
            }

            sut.sendCommand(UpserGradeScaleUIEvent.SetOperation(Insert))
            while (current.state != Loaded(Insert)) {
                current = awaitItem()
            }

            sut.sendCommand(UpserGradeScaleUIEvent.SetNewName("A"))
            while (current.newName != "A") {
                current = awaitItem()
            }

            sut.sendCommand(UpserGradeScaleUIEvent.SetNewName("AB"))
            while (current.newName != "AB") {
                current = awaitItem()
            }

            sut.sendCommand(UpserGradeScaleUIEvent.SetNewName("ABC"))
            while (current.newName != "ABC") {
                current = awaitItem()
            }

            assertEquals(1, allGradeScalesCalls)
            assertEquals(1, allGradeScalesCollections)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `db emission after save does not overwrite Success or SaveError`() = runTest {
        val scalesFlow = MutableStateFlow(mockGradeScales.toImmutableList())
        val insertSuccessWithEmission = InsertGradeScaleUseCase { name, _ ->
            scalesFlow.value = (
                scalesFlow.value + mockGradeScales.first().copy(id = "new-id", gradeScaleName = name)
                ).toImmutableList()
            Option("new-id")
        }
        val sutSuccess = UpsertGradeScaleUIStateFactory(
            getAllGradeScalesUseCase = GetAllGradeScalesUseCase { scalesFlow },
            insertGradeScaleUseCase = insertSuccessWithEmission,
            updateGradeScaleUseCase = defaultUpdateGradeScaleUseCase,
            showSnackbarUseCase = mockShowSnackbarUseCase,
            stateProducer = TestStateProducer(backgroundScope),
        )

        sutSuccess.uiState.test {
            sutSuccess.sendCommand(UpserGradeScaleUIEvent.SetOperation(Insert))
            runCurrent()
            assertEquals(Loaded(Insert), expectMostRecentItem().state)

            sutSuccess.sendCommand(UpserGradeScaleUIEvent.SetNewName("Persisted Scale"))
            sutSuccess.sendCommand(UpserGradeScaleUIEvent.Save("grade"))
            runCurrent()

            assertEquals(UpsertGradeScaleUIState.State.Success("new-id"), expectMostRecentItem().state)
            cancelAndIgnoreRemainingEvents()
        }

        val failingScalesFlow = MutableStateFlow(mockGradeScales.toImmutableList())
        val insertFailWithEmission = InsertGradeScaleUseCase { _, _ ->
            failingScalesFlow.value = failingScalesFlow.value.drop(1).toImmutableList()
            None
        }
        val sutError = UpsertGradeScaleUIStateFactory(
            getAllGradeScalesUseCase = GetAllGradeScalesUseCase { failingScalesFlow },
            insertGradeScaleUseCase = insertFailWithEmission,
            updateGradeScaleUseCase = defaultUpdateGradeScaleUseCase,
            showSnackbarUseCase = mockShowSnackbarUseCase,
            stateProducer = TestStateProducer(backgroundScope),
        )

        sutError.uiState.test {
            sutError.sendCommand(UpserGradeScaleUIEvent.SetOperation(Insert))
            runCurrent()
            assertEquals(Loaded(Insert), expectMostRecentItem().state)

            sutError.sendCommand(UpserGradeScaleUIEvent.SetNewName("Failing Scale"))
            sutError.sendCommand(UpserGradeScaleUIEvent.Save("grade"))
            runCurrent()

            assertEquals(UpsertGradeScaleUIState.State.SaveError, expectMostRecentItem().state)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `db emission while editing in Update mode does not overwrite user input`() = runTest {
        val scalesFlow = MutableStateFlow(mockGradeScales.toImmutableList())
        val targetScale = mockGradeScales[2]
        val sut = UpsertGradeScaleUIStateFactory(
            getAllGradeScalesUseCase = GetAllGradeScalesUseCase { scalesFlow },
            insertGradeScaleUseCase = defaultInsertGradeScaleUseCase,
            updateGradeScaleUseCase = defaultUpdateGradeScaleUseCase,
            showSnackbarUseCase = mockShowSnackbarUseCase,
            stateProducer = TestStateProducer(backgroundScope),
        )

        sut.uiState.test {
            sut.sendCommand(UpserGradeScaleUIEvent.SetOperation(Update(targetScale.id)))
            runCurrent()
            val initialized = expectMostRecentItem()
            assertEquals(targetScale.gradeScaleName, initialized.newName)

            sut.sendCommand(UpserGradeScaleUIEvent.SetNewName("Custom Edited Name"))
            runCurrent()
            assertEquals("Custom Edited Name", expectMostRecentItem().newName)

            // Unrelated DB emission happens while user is still typing
            scalesFlow.value = (
                scalesFlow.value + mockGradeScales.first().copy(id = "other-new", gradeScaleName = "Other")
                ).toImmutableList()
            runCurrent()

            val afterEmission = expectMostRecentItem()
            assertEquals("Custom Edited Name", afterEmission.newName)
            assertEquals(Loaded(Update(targetScale.id)), afterEmission.state)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `rapid double Save invokes insertGradeScaleUseCase only once`() = runTest {
        var insertCount = 0
        val countingInsert = InsertGradeScaleUseCase { name, defaultGrade ->
            delay(10)
            insertCount++
            savedName = name
            savedDefaultGradeName = defaultGrade
            Option("new-id")
        }
        val sut = setUseCase(insertGradeScaleUseCase = countingInsert)

        sut.uiState.test {
            sut.sendCommand(UpserGradeScaleUIEvent.SetOperation(Insert))
            runCurrent()
            assertEquals(Loaded(Insert), expectMostRecentItem().state)

            sut.sendCommand(UpserGradeScaleUIEvent.SetNewName("New Scale"))
            runCurrent()

            sut.sendCommand(UpserGradeScaleUIEvent.Save("A"))
            sut.sendCommand(UpserGradeScaleUIEvent.Save("A"))
            testScheduler.advanceTimeBy(50)
            testScheduler.runCurrent()

            assertEquals(1, insertCount)
            assertEquals(UpsertGradeScaleUIState.State.Success("new-id"), expectMostRecentItem().state)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
