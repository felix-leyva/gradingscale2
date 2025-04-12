package de.felixlf.gradingscale2.entities.features.weightedgradecalculator.dialog

import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade
import de.felixlf.gradingscale2.entities.moleculeTest
import de.felixlf.gradingscale2.entities.testMoleculeFlow
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.TestScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WeightedGradeDialogUIModelTest {

    // Test data
    private val testGradeScaleName = "Test Grade Scale"
    private val testGradeScaleId = "test-scale-id"
    private val testGrades = persistentListOf(
        Grade(namedGrade = "A", percentage = 0.9, idOfGradeScale = testGradeScaleId, nameOfScale = testGradeScaleName, uuid = "UUID-A"),
        Grade(namedGrade = "B", percentage = 0.8, idOfGradeScale = testGradeScaleId, nameOfScale = testGradeScaleName, uuid = "UUID-B"),
        Grade(namedGrade = "C", percentage = 0.7, idOfGradeScale = testGradeScaleId, nameOfScale = testGradeScaleName, uuid = "UUID-C"),
        Grade(namedGrade = "D", percentage = 0.6, idOfGradeScale = testGradeScaleId, nameOfScale = testGradeScaleName, uuid = "UUID-D"),
        Grade(namedGrade = "F", percentage = 0.5, idOfGradeScale = testGradeScaleId, nameOfScale = testGradeScaleName, uuid = "UUID-F"),
    )
    private val testGradeScale = GradeScale(
        id = testGradeScaleId,
        gradeScaleName = testGradeScaleName,
        grades = testGrades,
        totalPoints = 10.0,
    )
    
    private val testWeightedGrade = WeightedGrade(
        percentage = 0.75,
        weight = 2.0,
        uuid = "TEST-UUID"
    )

    private fun TestScope.createModel(): WeightedGradeDialogUIModel {
        return WeightedGradeDialogUIModel(scope = this)
    }

    @Test
    fun initialStateHasNullValues() = moleculeTest {
        val model = createModel()

        testMoleculeFlow(model) {
            val state = awaitItem()
            
            // Check initial values are null
            assertNull(state.gradeScale)
            assertNull(state.percentage)
            assertNull(state.weight)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun initCommandSetsInitialValues() = moleculeTest {
        val model = createModel()

        testMoleculeFlow(model) {
            // Get initial state
            val initialState = awaitItem()
            
            // Send Init command
            model.sendCommand(
                WeightedGradeDialogCommand.Init(
                    weightedGrade = testWeightedGrade,
                    gradeScale = testGradeScale
                )
            )
            
            // Get updated state
            val state = awaitItem()
            
            // Verify values were set correctly
            assertEquals(testGradeScale, state.gradeScale)
            assertEquals(testWeightedGrade.percentage, state.percentage)
            assertEquals(testWeightedGrade.weight, state.weight)
            
            // Check derived values are correct
            assertEquals("C", state.gradeNameString) // 75% should be C
            assertEquals("75", state.percentageString)
            assertEquals("2", state.weightString)
            assertEquals("1.5", state.relativeWeightString) // 0.75 * 2.0 = 1.5
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun selectGradeNameUpdatesPercentage() = moleculeTest {
        val model = createModel()

        testMoleculeFlow(model) {
            // Skip initial state
            awaitItem()
            
            // Initialize model
            model.sendCommand(
                WeightedGradeDialogCommand.Init(
                    weightedGrade = testWeightedGrade,
                    gradeScale = testGradeScale
                )
            )
            awaitItem()
            
            // Select grade name "A"
            model.sendCommand(WeightedGradeDialogCommand.SelectGradeName("A"))
            
            // Get updated state
            val state = awaitItem()
            
            // Verify percentage was updated to match "A" grade (0.9)
            assertEquals(0.9, state.percentage)
            assertEquals("A", state.gradeNameString)
            assertEquals("90", state.percentageString)
            assertEquals("1.8", state.relativeWeightString) // 0.9 * 2.0 = 1.8
            
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun selectNonExistentGradeNameDoesNothing() = moleculeTest {
        val model = createModel()

        testMoleculeFlow(model) {
            // Skip initial state
            awaitItem()
            
            // Initialize model
            model.sendCommand(
                WeightedGradeDialogCommand.Init(
                    weightedGrade = testWeightedGrade,
                    gradeScale = testGradeScale
                )
            )
            awaitItem()
            
            // Select a grade name that doesn't exist
            model.sendCommand(WeightedGradeDialogCommand.SelectGradeName("Z"))
            
            // Try to get updated state, but there should be none
            expectNoEvents()
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setPercentageUpdatesPercentage() = moleculeTest {
        val model = createModel()

        testMoleculeFlow(model) {
            // Skip initial state
            awaitItem()
            
            // Initialize model
            model.sendCommand(
                WeightedGradeDialogCommand.Init(
                    weightedGrade = testWeightedGrade,
                    gradeScale = testGradeScale
                )
            )
            awaitItem()
            
            // Set percentage to 85%
            model.sendCommand(WeightedGradeDialogCommand.SetPercentage("85"))
            
            // Get updated state
            val state = awaitItem()
            
            // Verify percentage was updated
            assertEquals(0.85, state.percentage)
            assertEquals("B", state.gradeNameString) // 85% should be B
            assertEquals("85", state.percentageString)
            assertEquals("1.7", state.relativeWeightString) // 0.85 * 2.0 = 1.7
            
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun setInvalidPercentageDoesNothing() = moleculeTest {
        val model = createModel()

        testMoleculeFlow(model) {
            // Skip initial state
            awaitItem()
            
            // Initialize model
            model.sendCommand(
                WeightedGradeDialogCommand.Init(
                    weightedGrade = testWeightedGrade,
                    gradeScale = testGradeScale
                )
            )
            awaitItem()
            
            // Set an invalid percentage
            model.sendCommand(WeightedGradeDialogCommand.SetPercentage("not a number"))
            
            // Verify no state update
            expectNoEvents()
            
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun percentageOutOfRangeIsCoerced() = moleculeTest {
        val model = createModel()

        testMoleculeFlow(model) {
            // Skip initial state
            awaitItem()
            
            // Initialize model
            model.sendCommand(
                WeightedGradeDialogCommand.Init(
                    weightedGrade = testWeightedGrade,
                    gradeScale = testGradeScale
                )
            )
            awaitItem()
            
            // Set percentage too high (150%)
            model.sendCommand(WeightedGradeDialogCommand.SetPercentage("150"))
            
            // Get updated state
            val state = awaitItem()
            
            // Verify percentage was coerced to 100%
            assertEquals(1.0, state.percentage)
            assertEquals("A", state.gradeNameString) // 100% should be A
            assertEquals("100", state.percentageString)
            
            // Set percentage too low (-50%)
            model.sendCommand(WeightedGradeDialogCommand.SetPercentage("-50"))
            
            // Get updated state
            val state2 = awaitItem()
            
            // Verify percentage was coerced to 0%
            assertEquals(0.0, state2.percentage)
            assertEquals("F", state2.gradeNameString) // 0% should be F
            assertEquals("0", state2.percentageString)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setWeightUpdatesWeight() = moleculeTest {
        val model = createModel()

        testMoleculeFlow(model) {
            // Skip initial state
            awaitItem()
            
            // Initialize model
            model.sendCommand(
                WeightedGradeDialogCommand.Init(
                    weightedGrade = testWeightedGrade,
                    gradeScale = testGradeScale
                )
            )
            awaitItem()
            
            // Set weight to 3.5
            model.sendCommand(WeightedGradeDialogCommand.SetWeight("3.5"))
            
            // Get updated state
            val state = awaitItem()
            
            // Verify weight was updated
            assertEquals(3.5, state.weight)
            assertEquals("C", state.gradeNameString) // Grade should not change
            assertEquals("75", state.percentageString) // Percentage should not change
            assertEquals("2.63", state.relativeWeightString) // 0.75 * 3.5 = 2.625, rounded to 2.63
            
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun setInvalidWeightDoesNothing() = moleculeTest {
        val model = createModel()

        testMoleculeFlow(model) {
            // Skip initial state
            awaitItem()
            
            // Initialize model
            model.sendCommand(
                WeightedGradeDialogCommand.Init(
                    weightedGrade = testWeightedGrade,
                    gradeScale = testGradeScale
                )
            )
            awaitItem()
            
            // Set an invalid weight
            model.sendCommand(WeightedGradeDialogCommand.SetWeight("not a number"))
            
            // Verify no state update
            expectNoEvents()
            
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun weightTooLowIsCoerced() = moleculeTest {
        val model = createModel()

        testMoleculeFlow(model) {
            // Skip initial state
            awaitItem()
            
            // Initialize model
            model.sendCommand(
                WeightedGradeDialogCommand.Init(
                    weightedGrade = testWeightedGrade,
                    gradeScale = testGradeScale
                )
            )
            awaitItem()
            
            // Set weight too low (0)
            model.sendCommand(WeightedGradeDialogCommand.SetWeight("0"))
            
            // Get updated state
            val state = awaitItem()
            
            // Verify weight was coerced to 0.01 (minimum allowed)
            assertEquals(0.01, state.weight)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setRelativeWeightUpdatesPercentageWhenBelowWeight() = moleculeTest {
        val model = createModel()

        testMoleculeFlow(model) {
            // Skip initial state
            awaitItem()
            
            // Initialize model
            model.sendCommand(
                WeightedGradeDialogCommand.Init(
                    weightedGrade = testWeightedGrade,
                    gradeScale = testGradeScale
                )
            )
            awaitItem()
            
            // Set relative weight to 1.0 (below current weight of 2.0)
            model.sendCommand(WeightedGradeDialogCommand.SetRelativeWeight("1.0"))
            
            // Get updated state
            val state = awaitItem()
            
            // Verify percentage was updated to 0.5 (1.0/2.0)
            assertEquals(0.5, state.percentage)
            assertEquals(2.0, state.weight) // Weight should not change
            assertEquals("F", state.gradeNameString) // 50% should be F
            assertEquals("50", state.percentageString)
            assertEquals("1", state.relativeWeightString)
            
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun setRelativeWeightUpdatesWeightWhenAboveWeight() = moleculeTest {
        val model = createModel()

        testMoleculeFlow(model) {
            // Skip initial state
            awaitItem()
            
            // Initialize model
            model.sendCommand(
                WeightedGradeDialogCommand.Init(
                    weightedGrade = testWeightedGrade,
                    gradeScale = testGradeScale
                )
            )
            awaitItem()
            
            // Set relative weight to 3.0 (above current weight of 2.0)
            model.sendCommand(WeightedGradeDialogCommand.SetRelativeWeight("3.0"))
            
            // Get updated state
            val state = awaitItem()
            
            // Verify weight was updated to 3.0 and percentage to 100%
            assertEquals(1.0, state.percentage)
            assertEquals(3.0, state.weight)
            assertEquals("A", state.gradeNameString) // 100% should be A
            assertEquals("100", state.percentageString)
            assertEquals("3", state.relativeWeightString)
            
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun setInvalidRelativeWeightDoesNothing() = moleculeTest {
        val model = createModel()

        testMoleculeFlow(model) {
            // Skip initial state
            awaitItem()
            
            // Initialize model
            model.sendCommand(
                WeightedGradeDialogCommand.Init(
                    weightedGrade = testWeightedGrade,
                    gradeScale = testGradeScale
                )
            )
            awaitItem()
            
            // Set an invalid relative weight
            model.sendCommand(WeightedGradeDialogCommand.SetRelativeWeight("not a number"))
            
            // Verify no state update
            expectNoEvents()
            
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun relativeWeightTooLowIsCoerced() = moleculeTest {
        val model = createModel()

        testMoleculeFlow(model) {
            // Skip initial state
            awaitItem()
            
            // Initialize model
            model.sendCommand(
                WeightedGradeDialogCommand.Init(
                    weightedGrade = testWeightedGrade,
                    gradeScale = testGradeScale
                )
            )
            awaitItem()
            
            // Set relative weight too low (0)
            model.sendCommand(WeightedGradeDialogCommand.SetRelativeWeight("0"))
            
            // Get updated state
            val state = awaitItem()
            
            // Verify percentage was updated based on minimum 0.01 relative weight
            assertEquals(0.005, state.percentage) // 0.01/2.0
            assertEquals(2.0, state.weight)
            assertEquals("0.5", state.percentageString) // 0.5% (rounded)
            assertEquals("0.01", state.relativeWeightString)
            
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun setRelativeWeightWithNoWeightDoesNothing() = moleculeTest {
        val model = createModel()

        testMoleculeFlow(model) {
            // Skip initial state
            awaitItem()
            
            // Don't initialize with weight
            
            // Try to set relative weight without weight set
            model.sendCommand(WeightedGradeDialogCommand.SetRelativeWeight("1.0"))
            
            // Verify no state update
            expectNoEvents()
            
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun selectGradeNameWithNoGradeScale() = moleculeTest {
        val model = createModel()

        testMoleculeFlow(model) {
            // Skip initial state
            awaitItem()
            
            // Try to select a grade name without grade scale
            model.sendCommand(WeightedGradeDialogCommand.SelectGradeName("A"))
            
            // Verify no state update
            expectNoEvents()
            
            cancelAndIgnoreRemainingEvents()
        }
    }
}
