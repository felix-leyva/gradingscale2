package de.felixlf.gradingscale2.entities.features.list.upsertgradedialog

import app.cash.turbine.test
import arrow.core.Either
import arrow.core.Some
import de.felixlf.gradingscale2.entities.TestStateProducer
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.usecases.GetGradeByUUIDUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import de.felixlf.gradingscale2.entities.usecases.InsertGradeUseCase
import de.felixlf.gradingscale2.entities.usecases.UpsertGradeUseCase
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UpsertGradeUIFactoryTest {

    private val testScale = GradeScale(
        id = "scale-1",
        gradeScaleName = "Test Scale",
        totalPoints = 100.0,
        grades = persistentListOf(
            Grade(namedGrade = "A", percentage = 0.9, idOfGradeScale = "scale-1", nameOfScale = "Test Scale", uuid = "g-1"),
            Grade(namedGrade = "B", percentage = 0.8, idOfGradeScale = "scale-1", nameOfScale = "Test Scale", uuid = "g-2"),
        ),
    )

    private val sampleGrade = Grade(
        namedGrade = "A",
        percentage = 0.9,
        idOfGradeScale = "scale-1",
        nameOfScale = "Test Scale",
        uuid = "g-1",
    )

    private val gradeFlow = MutableStateFlow<Grade?>(sampleGrade)
    private val scaleFlow = MutableStateFlow<GradeScale?>(testScale)

    private var upsertedGrade: Grade? = null
    private var insertedGradeScaleId: String? = null
    private var insertedPercentage: Double? = null
    private var insertedNamedGrade: String? = null

    private lateinit var getGradeByUUIDUseCase: GetGradeByUUIDUseCase
    private lateinit var getGradeScaleByIdUseCase: GetGradeScaleByIdUseCase
    private lateinit var insertGradeUseCase: InsertGradeUseCase
    private lateinit var upsertGradeUseCase: UpsertGradeUseCase

    @BeforeTest
    fun setUp() {
        gradeFlow.value = sampleGrade
        scaleFlow.value = testScale
        upsertedGrade = null
        insertedGradeScaleId = null
        insertedPercentage = null
        insertedNamedGrade = null

        getGradeByUUIDUseCase = GetGradeByUUIDUseCase { uuid ->
            if (uuid == "g-1") gradeFlow else flowOf(null)
        }

        getGradeScaleByIdUseCase = GetGradeScaleByIdUseCase { id ->
            if (id == "scale-1") scaleFlow else flowOf(null)
        }

        insertGradeUseCase = InsertGradeUseCase { gradeScaleId, percentage, namedGrade ->
            insertedGradeScaleId = gradeScaleId
            insertedPercentage = percentage
            insertedNamedGrade = namedGrade
            Either.Right(Unit)
        }

        upsertGradeUseCase = UpsertGradeUseCase { grade ->
            upsertedGrade = grade
            Some(Unit)
        }
    }

    private fun TestScope.createSUT(): UpsertGradeUIFactory {
        return UpsertGradeUIFactory(
            getGradeByUUIDUseCase = getGradeByUUIDUseCase,
            insertGradeUseCase = insertGradeUseCase,
            upsertGradeUseCase = upsertGradeUseCase,
            getGradeScaleByIdUseCase = getGradeScaleByIdUseCase,
            stateProducer = TestStateProducer(backgroundScope),
        )
    }

    @Test
    fun initialState_hasNullFields() = runTest {
        val sut = createSUT()

        sut.uiState.test {
            val state = awaitItem()
            assertNull(state.grade)
            assertNull(state.selectedGrade)
            assertNull(state.name)
            assertNull(state.percentage)
            assertNull(state.gradeScale)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setGradeScaleId_loadsGradeScale() = runTest {
        val sut = createSUT()

        sut.uiState.test {
            awaitItem() // initial

            sut.sendCommand(UpsertGradeUIEvent.SetGradeScaleId("scale-1"))
            testScheduler.runCurrent()
            val state = awaitItem()

            assertEquals("scale-1", state.gradeScale?.id)
            assertEquals("Test Scale", state.gradeScale?.gradeScaleName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setGradeUUID_loadsGradeAndPopulatesFields() = runTest {
        val sut = createSUT()

        sut.uiState.test {
            awaitItem() // initial

            sut.sendCommand(UpsertGradeUIEvent.SetGradeUUID("g-1"))
            testScheduler.runCurrent()
            var loadedState = awaitItem()
            while (loadedState.gradeScale == null) {
                loadedState = awaitItem()
            }

            assertEquals("g-1", loadedState.selectedGrade?.uuid)
            assertEquals("A", loadedState.name)
            assertEquals("90.0", loadedState.percentage)
            assertEquals("scale-1", loadedState.gradeScale.id)
            assertTrue(loadedState.isSaveButtonEnabled)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setGradeNameAndPercentage_updatesState() = runTest {
        val sut = createSUT()

        sut.uiState.test {
            awaitItem()

            sut.sendCommand(UpsertGradeUIEvent.SetGradeName("New Grade"))
            testScheduler.runCurrent()
            val nameState = awaitItem()
            assertEquals("New Grade", nameState.name)

            sut.sendCommand(UpsertGradeUIEvent.SetPercentage("75"))
            testScheduler.runCurrent()
            val pctState = awaitItem()
            assertEquals("75", pctState.percentage)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun save_withLoadedGrade_updatesExistingGrade() = runTest {
        val sut = createSUT()

        sut.uiState.test {
            awaitItem()
            sut.sendCommand(UpsertGradeUIEvent.SetGradeUUID("g-1"))
            testScheduler.runCurrent()
            var state = awaitItem()
            while (!state.isSaveButtonEnabled) {
                state = awaitItem()
            }
            assertTrue(state.isSaveButtonEnabled)

            sut.sendCommand(UpsertGradeUIEvent.SetGradeName("A+"))
            testScheduler.runCurrent()
            awaitItem()

            sut.sendCommand(UpsertGradeUIEvent.SetPercentage("95"))
            testScheduler.runCurrent()
            val stateBeforeSave = awaitItem()
            assertTrue(stateBeforeSave.isSaveButtonEnabled)

            sut.sendCommand(UpsertGradeUIEvent.Save)
            testScheduler.runCurrent()

            assertEquals("A+", upsertedGrade?.namedGrade)
            assertEquals(0.95, upsertedGrade?.percentage)
            assertEquals("g-1", upsertedGrade?.uuid)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun saveAsNew_savesNewGradeWithScaleId() = runTest {
        val sut = createSUT()

        sut.uiState.test {
            awaitItem()
            sut.sendCommand(UpsertGradeUIEvent.SetGradeScaleId("scale-1"))
            testScheduler.runCurrent()
            val scaleState = awaitItem()
            assertEquals("scale-1", scaleState.gradeScale?.id)

            sut.sendCommand(UpsertGradeUIEvent.SetGradeName("C"))
            testScheduler.runCurrent()
            val nameState = awaitItem()
            assertEquals("C", nameState.name)

            sut.sendCommand(UpsertGradeUIEvent.SetPercentage("70"))
            testScheduler.runCurrent()
            val readyState = awaitItem()
            assertTrue(readyState.isSaveNewButtonEnabled)

            sut.sendCommand(UpsertGradeUIEvent.SaveAsNew)
            testScheduler.runCurrent()

            assertEquals("scale-1", insertedGradeScaleId)
            assertEquals("C", insertedNamedGrade)
            assertEquals(0.70, insertedPercentage)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun save_withValidationError_doesNotInvokeUseCase() = runTest {
        val sut = createSUT()

        sut.uiState.test {
            awaitItem()
            sut.sendCommand(UpsertGradeUIEvent.SetGradeUUID("g-1"))
            awaitItem()
            awaitItem()

            // Set percentage to invalid value > 100
            sut.sendCommand(UpsertGradeUIEvent.SetPercentage("150"))
            val errorState = awaitItem()
            assertFalse(errorState.isSaveButtonEnabled)
            assertTrue(errorState.error.isNotEmpty())

            sut.sendCommand(UpsertGradeUIEvent.Save)

            assertNull(upsertedGrade)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
