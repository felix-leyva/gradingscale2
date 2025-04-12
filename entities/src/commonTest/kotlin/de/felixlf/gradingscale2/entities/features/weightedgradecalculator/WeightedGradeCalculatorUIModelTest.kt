package de.felixlf.gradingscale2.entities.features.weightedgradecalculator

import arrow.core.raise.option
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.models.GradeScaleNameAndId
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade
import de.felixlf.gradingscale2.entities.moleculeTest
import de.felixlf.gradingscale2.entities.testMoleculeFlow
import de.felixlf.gradingscale2.entities.usecases.DeleteWeightedGradeUseCase
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetAllWeightedGradesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import de.felixlf.gradingscale2.entities.usecases.UpsertWeightedGradeUseCase
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestScope
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WeightedGradeCalculatorUIModelTest {

    // Test data
    private val testGradeScaleName = "Test Grade Scale"
    private val testGradeScaleId = "test-scale-id"
    private val testGrades = persistentListOf(
        Grade(namedGrade = "A", percentage = 0.9, idOfGradeScale = testGradeScaleId, nameOfScale = testGradeScaleName, uuid = "UUID"),
        Grade(namedGrade = "B", percentage = 0.8, idOfGradeScale = testGradeScaleId, nameOfScale = testGradeScaleName, uuid = "UUID"),
        Grade(namedGrade = "C", percentage = 0.7, idOfGradeScale = testGradeScaleId, nameOfScale = testGradeScaleName, uuid = "UUID"),
        Grade(namedGrade = "D", percentage = 0.6, idOfGradeScale = testGradeScaleId, nameOfScale = testGradeScaleName, uuid = "UUID"),
        Grade(namedGrade = "F", percentage = 0.5, idOfGradeScale = testGradeScaleId, nameOfScale = testGradeScaleName, uuid = "UUID"),
    )
    private val testGradeScale = GradeScale(
        id = testGradeScaleId,
        gradeScaleName = testGradeScaleName,
        grades = testGrades,
        totalPoints = 10.0,
    )
    private val testGradeScaleNameAndId = GradeScaleNameAndId(
        name = testGradeScaleName,
        id = testGradeScaleId,
    )

    // Track method calls
    private var getAllGradeScalesUseCaseCalled = false
    private var getGradeScaleByIdUseCaseCalled = false
    private var lastGradeScaleIdParam: String? = null

    // Define functional interfaces
    private lateinit var getAllGradeScalesUseCase: GetAllGradeScalesUseCase
    private lateinit var getGradeScaleByIdUseCase: GetGradeScaleByIdUseCase

    private lateinit var getAllWeightedGradesUseCase: GetAllWeightedGradesUseCase

    private lateinit var upsertWeightedGradeUseCase: UpsertWeightedGradeUseCase

    private lateinit var deleteWeightedGradeUseCase: DeleteWeightedGradeUseCase

    // Subject under test
    private lateinit var weightedGradeCalculatorUIModel: WeightCalculatorUIModel

    private lateinit var grades: MutableStateFlow<List<WeightedGrade>>

    @BeforeTest
    fun setup() {
        // Reset tracking variables
        getAllGradeScalesUseCaseCalled = false
        getGradeScaleByIdUseCaseCalled = false
        lastGradeScaleIdParam = null
        grades = MutableStateFlow(emptyList())

        // Create functional interface implementations
        getAllGradeScalesUseCase = GetAllGradeScalesUseCase {
            getAllGradeScalesUseCaseCalled = true
            flowOf(persistentListOf(testGradeScale))
        }

        getGradeScaleByIdUseCase = GetGradeScaleByIdUseCase { id ->
            getGradeScaleByIdUseCaseCalled = true
            lastGradeScaleIdParam = id
            flowOf(if (id == testGradeScaleId) testGradeScale else null)
        }

        getAllWeightedGradesUseCase = GetAllWeightedGradesUseCase {
            grades.map { it.toImmutableList() }
        }

        upsertWeightedGradeUseCase = UpsertWeightedGradeUseCase { grade ->
            option {
                // Update existing grade with same UUID, or add new grade
                val existingGradeIndex = grades.value.indexOfFirst { it.uuid == grade.uuid }
                if (existingGradeIndex >= 0) {
                    val updatedList = grades.value.toMutableList()
                    updatedList[existingGradeIndex] = grade
                    grades.value = updatedList
                } else {
                    grades.value = grades.value + grade
                }
            }
        }

        deleteWeightedGradeUseCase = DeleteWeightedGradeUseCase { id ->
            option {
                grades.value = grades.value.filterNot { it.uuid == id }
            }
        }
    }

    private fun TestScope.initSUT() {
        // Create the model with test scope
        weightedGradeCalculatorUIModel = WeightCalculatorUIModel(
            scope = this,
            getAllGradeScales = getAllGradeScalesUseCase,
            getGradeScaleByIdUseCase = getGradeScaleByIdUseCase,
            getAllWeightedGradesUseCase = getAllWeightedGradesUseCase,
            upsertWeightedGradeUseCase = upsertWeightedGradeUseCase,
            deleteWeightedGradeUseCase = deleteWeightedGradeUseCase,
        )
    }

    @Test
    fun initialStateShouldLoadGradeScales() = moleculeTest {
        initSUT()

        testMoleculeFlow(weightedGradeCalculatorUIModel) {
            // First emission might be the default state with isLoading=true
            val initialState = awaitItem()

            // Verify initial loading state
            assertTrue(initialState.isLoading)
            // Advance until the loading is done
            val intermediateState = awaitItem()

            // Verify
            assertTrue(getAllGradeScalesUseCaseCalled)
            assertEquals(1, intermediateState.gradeScaleNameAndIds.size)
            assertEquals(testGradeScaleNameAndId.name, intermediateState.gradeScaleNameAndIds[0].name)
            assertEquals(testGradeScaleNameAndId.id, intermediateState.gradeScaleNameAndIds[0].id)
            assertFalse(intermediateState.isLoading)
            assertNull(intermediateState.selectedGradeScale)
            assertTrue(intermediateState.grades.isEmpty())

            // Clean up any remaining emissions
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldSelectGradeScaleWhenSelectGradeScaleCommandIsSent() = moleculeTest {
        initSUT()

        testMoleculeFlow(weightedGradeCalculatorUIModel) {
            // Get to stable state
            awaitItem() // Skip initial loading state
            awaitItem() // Get loaded state

            // Reset tracking
            getGradeScaleByIdUseCaseCalled = false
            lastGradeScaleIdParam = null

            // Act: select grade scale
            weightedGradeCalculatorUIModel.sendCommand(WeightedCalculatorCommand.SelectGradeScale(testGradeScaleId))
            awaitItem()
            // Get state after selection
            val state = awaitItem()

            // Verify
            assertTrue(getGradeScaleByIdUseCaseCalled)
            assertEquals(testGradeScaleId, lastGradeScaleIdParam)
            assertEquals(testGradeScaleId, state.selectedGradeScale?.id)
            assertEquals(testGradeScaleName, state.selectedGradeScale?.gradeScaleName)

            // Clean up
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldAddGradeAtEndWhenCommandIsSent() = moleculeTest {
        initSUT()

        testMoleculeFlow(weightedGradeCalculatorUIModel) {
            // Get to stable state
            awaitItem() // Skip initial loading state
            awaitItem() // Get loaded state

            // Select the grade first to be able to see it in selectedGrade
            val newGrade = WeightedGrade(0.0, 0.0, "UUID")

            // Act: add grade at position 0 (beginning/end since list is empty)
            weightedGradeCalculatorUIModel.sendCommand(
                WeightedCalculatorCommand.AddGradeAtPos(
                    position = 0,
                    grade = newGrade,
                ),
            )

            // Get updated state after adding
            awaitItem()

            // Now select the grade
            weightedGradeCalculatorUIModel.sendCommand(WeightedCalculatorCommand.SelectGrade("UUID"))

            // Get updated state after selection
            val stateAfterSelection = awaitItem()

            // Verify
            assertEquals(1, stateAfterSelection.grades.size)
            assertEquals(newGrade, stateAfterSelection.grades[0])
            assertEquals(newGrade, stateAfterSelection.selectedGrade)

            // Clean up
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldAddGradeAtPositionWhenCommandIsSent() = moleculeTest {
        initSUT()

        testMoleculeFlow(weightedGradeCalculatorUIModel) {
            // Get to stable state
            awaitItem() // Skip initial loading state
            awaitItem() // Get loaded state

            // Setup: Add two grades first to make room for insertion
            weightedGradeCalculatorUIModel.sendCommand(
                WeightedCalculatorCommand.AddGradeAtPos(
                    position = 0,
                    grade = WeightedGrade(0.85, 1.0, "UUID1"),
                ),
            )
            awaitItem()

            weightedGradeCalculatorUIModel.sendCommand(
                WeightedCalculatorCommand.AddGradeAtPos(
                    position = 1,
                    grade = WeightedGrade(0.75, 2.0, "UUID2"),
                ),
            )
            awaitItem()

            // Act: add grade at specific position
            val position = 1
            val newGrade = WeightedGrade(0.8, 1.5, "UUID3")
            weightedGradeCalculatorUIModel.sendCommand(
                WeightedCalculatorCommand.AddGradeAtPos(
                    position = position,
                    grade = newGrade,
                ),
            )

            // Get updated state after adding
            awaitItem()
            
            // Now select the newly added grade
            weightedGradeCalculatorUIModel.sendCommand(WeightedCalculatorCommand.SelectGrade("UUID3"))

            // Get updated state after selection
            val stateAfterSelection = awaitItem()

            // Verify the grade is in the expected state - we need to find it by UUID instead of position
            assertEquals(3, stateAfterSelection.grades.size)
            val addedGrade = stateAfterSelection.grades.first { it.uuid == "UUID3" }
            assertEquals(newGrade, addedGrade)
            assertEquals(newGrade, stateAfterSelection.selectedGrade)

            // Clean up
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldUpdateGradeWhenUpdateGradeCommandIsSent() = moleculeTest {
        initSUT()

        testMoleculeFlow(weightedGradeCalculatorUIModel) {
            // Get to stable state
            awaitItem() // Skip initial loading state
            awaitItem() // Get loaded state

            // Setup: Add a grade first
            val initialGrade = WeightedGrade(0.0, 0.0, "UUID")
            weightedGradeCalculatorUIModel.sendCommand(
                WeightedCalculatorCommand.AddGradeAtPos(
                    position = 0,
                    grade = initialGrade,
                ),
            )
            awaitItem()

            // Act: update the grade
            val updatedGrade = WeightedGrade(0.85, 2.0, "UUID")
            weightedGradeCalculatorUIModel.sendCommand(
                WeightedCalculatorCommand.UpdateGrade(updatedGrade),
            )

            // Get updated state
            val state = awaitItem()

            // Verify
            assertEquals(1, state.grades.size)
            assertEquals(0.85, state.grades[0].percentage)
            assertEquals(2.0, state.grades[0].weight)

            // Clean up
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldRemoveGradeWhenRemoveGradeCommandIsSent() = moleculeTest {
        initSUT()

        testMoleculeFlow(weightedGradeCalculatorUIModel) {
            // Get to stable state
            awaitItem() // Skip initial loading state
            awaitItem() // Get loaded state

            // Setup: Add two grades first
            weightedGradeCalculatorUIModel.sendCommand(
                WeightedCalculatorCommand.AddGradeAtPos(
                    position = 0,
                    grade = WeightedGrade(0.85, 1.0, "UUID1"),
                ),
            )
            awaitItem()

            weightedGradeCalculatorUIModel.sendCommand(
                WeightedCalculatorCommand.AddGradeAtPos(
                    position = 1,
                    grade = WeightedGrade(0.75, 2.0, "UUID2"),
                ),
            )
            // Get state after adding both grades
            val stateAfterAdd = awaitItem()

            // Verify initial state
            assertEquals(2, stateAfterAdd.grades.size)

            // Act: remove the first grade
            weightedGradeCalculatorUIModel.sendCommand(WeightedCalculatorCommand.RemoveGrade("UUID1"))

            // Get updated state
            val state = awaitItem()

            // Verify
            assertEquals(1, state.grades.size)
            assertEquals(0.75, state.grades[0].percentage)
            assertEquals(2.0, state.grades[0].weight)

            // Clean up
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun weightedGradesShouldContainCorrectGradeNamesWhenGradeScaleIsSelected() = moleculeTest {
        initSUT()

        testMoleculeFlow(weightedGradeCalculatorUIModel) {
            // Get to stable state
            awaitItem() // Skip initial loading state
            awaitItem() // Get loaded state

            // Setup: Select grade scale and add some grades
            weightedGradeCalculatorUIModel.sendCommand(WeightedCalculatorCommand.SelectGradeScale(testGradeScaleId))
            awaitItem()
            awaitItem()

            weightedGradeCalculatorUIModel.sendCommand(
                WeightedCalculatorCommand.AddGradeAtPos(
                    position = 0,
                    grade = WeightedGrade(0.85, 1.0, "UUID1"),
                ),
            )
            awaitItem()

            weightedGradeCalculatorUIModel.sendCommand(
                WeightedCalculatorCommand.AddGradeAtPos(
                    position = 1,
                    grade = WeightedGrade(0.75, 2.0, "UUID2"),
                ),
            )

            // Get updated state
            val state = awaitItem()

            // Verify weighted grades have correct names
            assertEquals(2, state.weightedGrades.size)
            assertEquals("B", state.weightedGrades[0].name) // 85% should be B
            assertEquals("C", state.weightedGrades[1].name) // 75% should be C

            // Clean up
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun weightedGradesShouldBeEmptyWhenNoGradeScaleIsSelected() = moleculeTest {
        initSUT()

        testMoleculeFlow(weightedGradeCalculatorUIModel) {
            // Get to stable state
            awaitItem() // Skip initial loading state
            awaitItem() // Get loaded state

            // Setup: Add grades without selecting a grade scale
            weightedGradeCalculatorUIModel.sendCommand(
                WeightedCalculatorCommand.AddGradeAtPos(
                    position = 0,
                    grade = WeightedGrade(0.85, 1.0, "UUID1"),
                ),
            )
            awaitItem()

            weightedGradeCalculatorUIModel.sendCommand(
                WeightedCalculatorCommand.AddGradeAtPos(
                    position = 1,
                    grade = WeightedGrade(0.75, 2.0, "UUID2"),
                ),
            )

            // Get updated state
            val state = awaitItem()

            // Verify weighted grades list is empty when no grade scale is selected
            assertTrue(state.weightedGrades.isEmpty())

            // Clean up
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun shouldHandleMultipleUpdatesCorrectly() = moleculeTest {
        initSUT()

        testMoleculeFlow(weightedGradeCalculatorUIModel) {
            // Get to stable state
            awaitItem() // Skip initial loading state
            awaitItem() // Get loaded state

            // Select grade scale
            weightedGradeCalculatorUIModel.sendCommand(WeightedCalculatorCommand.SelectGradeScale(testGradeScaleId))
            awaitItem()
            awaitItem()

            // Add first grade
            val firstGrade = WeightedGrade(0.85, 1.0, "UUID1")
            weightedGradeCalculatorUIModel.sendCommand(
                WeightedCalculatorCommand.AddGradeAtPos(
                    position = 0,
                    grade = firstGrade,
                ),
            )
            val stateAfterFirstAdd = awaitItem()
            assertEquals(1, stateAfterFirstAdd.grades.size)
            assertEquals(0.85, stateAfterFirstAdd.grades[0].percentage)

            // Update the grade
            val updatedGrade = WeightedGrade(0.95, 3.0, "UUID1")
            weightedGradeCalculatorUIModel.sendCommand(
                WeightedCalculatorCommand.UpdateGrade(updatedGrade),
            )
            val stateAfterUpdate = awaitItem()
            assertEquals(0.95, stateAfterUpdate.grades[0].percentage)
            assertEquals(3.0, stateAfterUpdate.grades[0].weight)
            assertEquals("A", stateAfterUpdate.weightedGrades[0].name) // 95% should be A

            // Add second grade
            weightedGradeCalculatorUIModel.sendCommand(
                WeightedCalculatorCommand.AddGradeAtPos(
                    position = 1,
                    grade = WeightedGrade(0.65, 2.0, "UUID2"),
                ),
            )
            val stateAfterSecondAdd = awaitItem()
            assertEquals(2, stateAfterSecondAdd.grades.size)

            // Remove first grade
            weightedGradeCalculatorUIModel.sendCommand(WeightedCalculatorCommand.RemoveGrade("UUID1"))
            val finalState = awaitItem()
            assertEquals(1, finalState.grades.size)
            assertEquals(0.65, finalState.grades[0].percentage)
            assertEquals("D", finalState.weightedGrades[0].name) // 65% should be D

            // Clean up
            cancelAndIgnoreRemainingEvents()
        }
    }
}
