package de.felixlf.gradingscale2.features.list.editgradedialog

import app.cash.turbine.test
import arrow.core.raise.option
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.usecases.GetGradeByUUIDUseCase
import de.felixlf.gradingscale2.entities.usecases.UpsertGradeUseCase
import de.felixlf.gradingscale2.entities.util.MockGradeScalesGenerator
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class EditGradeViewModelTest {

    private lateinit var gradeScales: MutableStateFlow<List<GradeScale>>

    private val getGradeByUUIDUseCase = GetGradeByUUIDUseCase { uuid ->
        gradeScales.map { gradeScaleList ->
            gradeScaleList.flatMap { it.grades }.firstOrNull { it.uuid == uuid }
        }
    }

    private val updateSingleGradeUseCase = UpsertGradeUseCase { grade ->
        option {
            gradeScales.update {
                val modifiedGradeScale = it.find { gradeScale -> gradeScale.id == grade.idOfGradeScale }
                    ?: throw IllegalArgumentException("GradeScale not found")
                val newGradeScale = GradeScale(
                    id = modifiedGradeScale.id,
                    gradeScaleName = modifiedGradeScale.gradeScaleName,
                    totalPoints = modifiedGradeScale.totalPoints,
                    grades = modifiedGradeScale.grades.map { existingGrade ->
                        if (existingGrade.uuid == grade.uuid) grade else existingGrade
                    }.toImmutableList(),
                )
                it.map { gradeScale -> if (gradeScale.gradeScaleName == newGradeScale.gradeScaleName) newGradeScale else gradeScale }
            }
        }
    }

    private lateinit var viewModel: EditGradeViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        gradeScales = MutableStateFlow(MockGradeScalesGenerator().gradeScales)
        viewModel = EditGradeViewModel(getGradeByUUIDUseCase, updateSingleGradeUseCase)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `updateGradeModel loads the grade from the usecase`() = runTest {
        // Given
        val grade = gradeScales.value[1].grades[3]
        val uuid = grade.uuid

        // When, Then
        viewModel.uiState.test {
            assertEquals(null, awaitItem().grade)
            viewModel.setGradeUUID(uuid)
            assertEquals(grade, awaitItem().grade)
        }
    }

    @Test
    fun `updateGradeModel name updates the ui grade name`() = runTest {
        // Given
        val grade = gradeScales.value[1].grades[3]
        val uuid = grade.uuid
        val newNameGrade = "New Grade Name"

        // When, Then
        viewModel.uiState.test {
            assertEquals(null, awaitItem().grade)
            viewModel.setGradeUUID(uuid)
            assertEquals(grade, awaitItem().grade)
            viewModel.setGradeName(newNameGrade)
            val state = awaitItem()
            assertEquals(newNameGrade, state.name)
            assertEquals(persistentSetOf(), state.error)
        }
    }

    @Test
    fun `updateGradeModel name updates the ui grade name but with errors if name is empty`() = runTest {
        // Given
        val grade = gradeScales.value[1].grades[3]
        val uuid = grade.uuid
        val newNameGrade = ""

        // When, Then
        viewModel.uiState.test {
            assertEquals(null, awaitItem().grade)
            viewModel.setGradeUUID(uuid)
            awaitItem()
            viewModel.setGradeName(newNameGrade)
            val state = awaitItem()
            assertEquals(newNameGrade, state.name)
            assertEquals(EditGradeUIState.Error.INVALID_NAME, state.error.first())
        }
    }

    @Test
    fun `update percentage updates the ui percentage but with errors if percentage is not between 0 and 100`() = runTest {
        // Given
        val grade = gradeScales.value[1].grades[3]
        val uuid = grade.uuid
        val newPercentage = 101.0

        // When, Then
        viewModel.uiState.test {
            assertEquals(null, awaitItem().grade)
            viewModel.setGradeUUID(uuid)
            awaitItem()
            viewModel.setPercentage(newPercentage.toString())
            val state = awaitItem()
            assertEquals(newPercentage.toString(), state.percentage)
            assertEquals(EditGradeUIState.Error.INVALID_PERCENTAGE, state.error.first())
        }
    }

    @Test
    fun `updateGradeModel updates the grade`() = runTest {
        // Given
        val grade = gradeScales.value[1].grades[3]
        val uuid = grade.uuid
        val newNameGrade = "New Grade Name"

        // When, Then
        viewModel.uiState.test {
            assertEquals(null, awaitItem().grade)
            viewModel.setGradeUUID(uuid)
            awaitItem()
            viewModel.setGradeName(newNameGrade)
            val state = awaitItem()
            assertEquals(newNameGrade, state.name)
            assertEquals(persistentSetOf(), state.error)
            viewModel.updateGrade()
            awaitItem()
            // Check if the grade was updated
            assertEquals(newNameGrade, gradeScales.value[1].grades[3].namedGrade)
        }
    }
}
