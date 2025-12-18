package de.felixlf.gradingscale2.features

import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.raise.option
import de.felixlf.gradingscale2.entities.features.list.upsertgradedialog.UpsertGradeUIEvent
import de.felixlf.gradingscale2.entities.features.list.upsertgradedialog.UpsertGradeUIFactory
import de.felixlf.gradingscale2.entities.features.list.upsertgradedialog.UpsertGradeUIState
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.usecases.GetGradeByUUIDUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import de.felixlf.gradingscale2.entities.usecases.InsertGradeUseCase
import de.felixlf.gradingscale2.entities.usecases.InsertGradeUseCaseError
import de.felixlf.gradingscale2.entities.usecases.UpsertGradeUseCase
import de.felixlf.gradingscale2.entities.util.MockGradeScalesGenerator
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class EditGradeViewModelTest {

    private lateinit var gradeScales: MutableStateFlow<List<GradeScale>>

    private val getGradeByUUIDUseCase = GetGradeByUUIDUseCase { uuid ->
        gradeScales.map { gradeScaleList ->
            gradeScaleList.flatMap { it.grades }.firstOrNull { it.uuid == uuid }
        }
    }

    private val insertGradeUseCase = InsertGradeUseCase { gradeScaleId, percentage, namedGrade ->
        either {
            gradeScales.update {
                val selectedGradeScale =
                    ensureNotNull(it.firstOrNull { gradeScale -> gradeScale.id == gradeScaleId }) {
                        InsertGradeUseCaseError.GradeScaleIdNotFound
                    }
                val newGrade = Grade(
                    namedGrade = namedGrade,
                    percentage = percentage,
                    idOfGradeScale = gradeScaleId,
                    nameOfScale = selectedGradeScale.gradeScaleName,
                    uuid = "1234",
                )
                it.map { gradeScale ->
                    if (gradeScale.id == gradeScaleId) {
                        gradeScale.copy(
                            grades = (gradeScale.grades + newGrade).toPersistentList(),
                        )
                    } else {
                        gradeScale
                    }
                }
            }
        }
    }

    private val updateSingleGradeUseCase = UpsertGradeUseCase { grade ->
        option {
            gradeScales.update {
                val modifiedGradeScale =
                    ensureNotNull(it.find { gradeScale -> gradeScale.id == grade.idOfGradeScale })
                val newGradeScale = GradeScale(
                    id = modifiedGradeScale.id,
                    gradeScaleName = modifiedGradeScale.gradeScaleName,
                    totalPoints = modifiedGradeScale.totalPoints,
                    grades = modifiedGradeScale.grades.map { existingGrade ->
                        if (existingGrade.uuid == grade.uuid) grade else existingGrade
                    }.toPersistentList(),
                )
                it.map { gradeScale -> if (gradeScale.gradeScaleName == newGradeScale.gradeScaleName) newGradeScale else gradeScale }
            }
        }
    }

    private val getGradeScaleByIdUseCase = GetGradeScaleByIdUseCase { id ->
        gradeScales.map { gradeScaleList ->
            gradeScaleList.firstOrNull { it.id == id }
        }
    }

    fun TestScope.getSUT(): UpsertGradeUIFactory {
        gradeScales = MutableStateFlow(MockGradeScalesGenerator().gradeScales)
        return UpsertGradeUIFactory(
            getGradeByUUIDUseCase = getGradeByUUIDUseCase,
            upsertGradeUseCase = updateSingleGradeUseCase,
            insertGradeUseCase = insertGradeUseCase,
            getGradeScaleByIdUseCase = getGradeScaleByIdUseCase,
            scope = this,
        )
    }

    @Test
    fun `updateGradeModel loads the grade from the usecase`() = moleculeTest {
        // Given
        val viewModel = getSUT()
        val grade = gradeScales.value[1].grades[3]
        val uuid = grade.uuid

        // When, Then
        testMoleculeFlow(viewModel) {
            assertEquals(null, awaitItem().grade)
            viewModel.sendCommand(UpsertGradeUIEvent.SetGradeUUID(uuid))
            assertEquals(grade, awaitItem().grade)
        }
    }

    @Test
    fun `updateGradeModel name updates the ui grade name`() = moleculeTest {
        // Given
        val viewModel = getSUT()
        val grade = gradeScales.value[1].grades[3]
        val uuid = grade.uuid
        val newNameGrade = "New Grade Name"

        // When, Then
        testMoleculeFlow(viewModel) {
            assertEquals(null, awaitItem().grade)
            viewModel.sendCommand(UpsertGradeUIEvent.SetGradeUUID(uuid))
            assertEquals(grade, awaitItem().grade)
            viewModel.sendCommand(UpsertGradeUIEvent.SetGradeName(newNameGrade))
            val state = awaitItem()
            assertEquals(newNameGrade, state.name)
            assertEquals(persistentSetOf(), state.error)
        }
    }

    @Test
    fun `updateGradeModel name updates the ui grade name but with errors if name is empty`() = moleculeTest {
        // Given
        val viewModel = getSUT()

        val grade = gradeScales.value[1].grades[3]
        val uuid = grade.uuid
        val newNameGrade = ""

        // When, Then
        testMoleculeFlow(viewModel) {
            assertEquals(null, awaitItem().grade)
            viewModel.sendCommand(UpsertGradeUIEvent.SetGradeUUID(uuid))
            awaitItem()
            viewModel.sendCommand(UpsertGradeUIEvent.SetGradeName(newNameGrade))
            val state = awaitItem()
            assertEquals(newNameGrade, state.name)
            assertEquals(UpsertGradeUIState.Error.INVALID_NAME, state.error.first())
        }
    }

    @Test
    fun `update percentage updates the ui percentage but with errors if percentage is not between 0 and 100`() = moleculeTest {
        // Given
        val viewModel = getSUT()

        val grade = gradeScales.value[1].grades[3]
        val uuid = grade.uuid
        val newPercentage = 101.0

        // When, Then
        testMoleculeFlow(viewModel) {
            assertEquals(null, awaitItem().grade)
            viewModel.sendCommand(UpsertGradeUIEvent.SetGradeUUID(uuid))
            awaitItem()
            viewModel.sendCommand(UpsertGradeUIEvent.SetPercentage(newPercentage.toString()))
            val state = awaitItem()
            assertEquals(newPercentage.toString(), state.percentage)
            assertEquals(UpsertGradeUIState.Error.INVALID_PERCENTAGE, state.error.first())
        }
    }

    @Test
    fun `updateGradeModel updates the grade`() = moleculeTest {
        // Given
        val viewModel = getSUT()

        val grade = gradeScales.value[1].grades[3]
        val uuid = grade.uuid
        val newNameGrade = "New Grade Name"

        // When, Then
        testMoleculeFlow(viewModel) {
            assertEquals(null, awaitItem().grade)
            viewModel.sendCommand(UpsertGradeUIEvent.SetGradeUUID(uuid))
            awaitItem()
            viewModel.sendCommand(UpsertGradeUIEvent.SetGradeName(newNameGrade))
            val state = awaitItem()
            assertEquals(newNameGrade, state.name)
            assertEquals(persistentSetOf(), state.error)
            viewModel.sendCommand(UpsertGradeUIEvent.Save)
            awaitItem()
            // Check if the grade was updated
            assertEquals(newNameGrade, gradeScales.value[1].grades[3].namedGrade)
        }
    }

    @Test
    fun `updateGradeModel creates a new grade`() = moleculeTest {
        // Given
        val viewModel = getSUT()

        val gradeScaleId = gradeScales.value[1].id
        val newNameGrade = "New Grade Name"
        val newPercentage = 0.5123

        // When, Then
        testMoleculeFlow(viewModel) {
            awaitItem()
            viewModel.sendCommand(UpsertGradeUIEvent.SetGradeScaleId(gradeScaleId))
            viewModel.sendCommand(UpsertGradeUIEvent.SetGradeName(newNameGrade))
            awaitItem()
            viewModel.sendCommand(UpsertGradeUIEvent.SetPercentage(newPercentage.toString()))
            awaitItem()
            viewModel.sendCommand(UpsertGradeUIEvent.SaveAsNew)
            advanceUntilIdle()
            awaitItem()
            // Check if the grade was created
            assertContains(gradeScales.value[1].grades.map { it.namedGrade }, newNameGrade)
        }
    }
}
