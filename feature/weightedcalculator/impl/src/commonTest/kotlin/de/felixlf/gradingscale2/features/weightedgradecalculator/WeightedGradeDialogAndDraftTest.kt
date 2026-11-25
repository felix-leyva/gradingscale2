package de.felixlf.gradingscale2.features.weightedgradecalculator

import app.cash.molecule.RecompositionMode
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightCalculatorUIState
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightedCalculatorCommand
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.dialog.WeightedGradeDialogCommand
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.dialog.WeightedGradeDialogUIModelWithEvents
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.dialog.WeightedGradeDialogUIState
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.models.GradeScaleNameAndId
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade
import de.felixlf.gradingscale2.entities.uimodel.StateProducer
import de.felixlf.gradingscale2.features.weightedgradecalculator.dialogs.resolveWeightedGradeForSave
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class WeightedGradeDialogAndDraftTest {

    private val testScale = GradeScale(
        id = "scale-1",
        gradeScaleName = "Test Scale",
        totalPoints = 100.0,
        grades = persistentListOf(
            Grade(namedGrade = "A", percentage = 0.9, idOfGradeScale = "scale-1", nameOfScale = "Test Scale", uuid = "g-1"),
            Grade(namedGrade = "B", percentage = 0.8, idOfGradeScale = "scale-1", nameOfScale = "Test Scale", uuid = "g-2"),
            Grade(namedGrade = "F", percentage = 0.5, idOfGradeScale = "scale-1", nameOfScale = "Test Scale", uuid = "g-3"),
        ),
    )

    private val existingGrade = WeightedGrade(
        percentage = 0.75,
        weight = 2.0,
        uuid = "wg-1",
    )

    @Test
    fun resolveWeightedGradeForSaveCoercesOutOfRangeAndNonFinitePercentageSafely() {
        val high = WeightedGradeDialogUIState(gradeScale = testScale, percentage = 1.2, weight = 2.0)
        assertEquals(1.0, resolveWeightedGradeForSave(high, existingGrade).percentage)

        val negative = WeightedGradeDialogUIState(gradeScale = testScale, percentage = -0.2, weight = 2.0)
        assertEquals(0.0, resolveWeightedGradeForSave(negative, existingGrade).percentage)

        val nonFinite = WeightedGradeDialogUIState(gradeScale = testScale, percentage = Double.POSITIVE_INFINITY, weight = 2.0)
        assertEquals(existingGrade.percentage, resolveWeightedGradeForSave(nonFinite, existingGrade).percentage)

        val nullPct = WeightedGradeDialogUIState(gradeScale = testScale, percentage = null, weight = 2.0)
        assertEquals(existingGrade.percentage, resolveWeightedGradeForSave(nullPct, existingGrade).percentage)
    }

    @Test
    fun resolveWeightedGradeForSavePreservesScoreEditFromDialogModel() = runTest {
        val model = WeightedGradeDialogUIModelWithEvents(
            stateProducer = object : StateProducer {
                override val scope: CoroutineScope = backgroundScope
                override val recompositionMode: RecompositionMode = RecompositionMode.Immediate
            },
        )
        model.uiState
        model.sendCommand(WeightedGradeDialogCommand.Init(gradeScale = testScale, weightedGrade = existingGrade))
        runCurrent()

        model.sendCommand(WeightedGradeDialogCommand.SetRelativeWeight("1.0"))
        runCurrent()

        val resolved = resolveWeightedGradeForSave(model.uiState.value, existingGrade)
        assertEquals(0.5, resolved.percentage)
        assertEquals(2.0, resolved.weight)
        assertEquals(existingGrade.uuid, resolved.uuid)
    }

    @Test
    fun resolveWeightedGradeForSaveNeverCorruptsWeightToZeroOrNonFinite() {
        listOf(0.0, null, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { invalidWeight ->
            val state = WeightedGradeDialogUIState(
                gradeScale = testScale,
                percentage = 0.85,
                weight = invalidWeight,
            )
            val resolved = resolveWeightedGradeForSave(state, existingGrade)
            assertTrue(resolved.weight >= 0.01 && resolved.weight.isFinite())
            assertEquals(existingGrade.weight, resolved.weight)
        }

        val badFallback = WeightedGrade(percentage = 0.5, weight = 0.0, uuid = "wg-zero")
        val state = WeightedGradeDialogUIState(gradeScale = testScale, percentage = 0.5, weight = 0.0)
        assertEquals(1.0, resolveWeightedGradeForSave(state, badFallback).weight)
    }

    @Test
    fun startAddGradeCreatesLocalDraftWithoutPersistingAndDismissLeavesZeroCommands() {
        val commands = mutableListOf<WeightedCalculatorCommand>()
        val coordinator = WeightedGradeDialogCoordinator { "draft-uuid" }
        val uiState = WeightCalculatorUIState(
            gradeScaleNameAndIds = persistentListOf(GradeScaleNameAndId("Test Scale", "scale-1")),
            selectedGradeScale = testScale,
            grades = persistentListOf(),
            selectedGrade = null,
        )

        assertTrue(coordinator.startAddGrade(uiState))
        assertTrue(commands.isEmpty())
        assertTrue(coordinator.isEditingDraft())
        assertEquals("draft-uuid", coordinator.activeDialogGrade(uiState)?.uuid)

        coordinator.dismiss(commands::add)
        assertNull(coordinator.draftGrade)
        assertFalse(coordinator.isEditingDraft())
        assertTrue(commands.isEmpty())
    }

    @Test
    fun doubleTappingStartAddGradeWhileDraftOpenIsNoOp() {
        val commands = mutableListOf<WeightedCalculatorCommand>()
        var nextId = 1
        val coordinator = WeightedGradeDialogCoordinator { "draft-${nextId++}" }
        val uiState = WeightCalculatorUIState(
            gradeScaleNameAndIds = persistentListOf(GradeScaleNameAndId("Test Scale", "scale-1")),
            selectedGradeScale = testScale,
            grades = persistentListOf(),
            selectedGrade = null,
        )

        assertTrue(coordinator.startAddGrade(uiState))
        assertFalse(coordinator.startAddGrade(uiState))
        assertEquals("draft-1", coordinator.draftGrade?.uuid)
        assertTrue(commands.isEmpty())
    }

    @Test
    fun savingDraftDispatchesAddGradeAtPosOnceAndClearsDraft() {
        val commands = mutableListOf<WeightedCalculatorCommand>()
        val coordinator = WeightedGradeDialogCoordinator { "draft-uuid" }
        val uiState = WeightCalculatorUIState(
            gradeScaleNameAndIds = persistentListOf(GradeScaleNameAndId("Test Scale", "scale-1")),
            selectedGradeScale = testScale,
            grades = persistentListOf(existingGrade),
            selectedGrade = null,
        )

        assertTrue(coordinator.startAddGrade(uiState))
        val editedDraft = WeightedGrade(percentage = 0.88, weight = 3.0, uuid = "draft-uuid")
        coordinator.save(editedDraft, uiState, commands::add)

        assertNull(coordinator.draftGrade)
        assertEquals(listOf<WeightedCalculatorCommand>(WeightedCalculatorCommand.AddGradeAtPos(1, editedDraft)), commands)
    }

    @Test
    fun savingExistingSelectedGradeDispatchesUpdateGrade() {
        val commands = mutableListOf<WeightedCalculatorCommand>()
        val coordinator = WeightedGradeDialogCoordinator { "unused" }
        val uiState = WeightCalculatorUIState(
            gradeScaleNameAndIds = persistentListOf(GradeScaleNameAndId("Test Scale", "scale-1")),
            selectedGradeScale = testScale,
            grades = persistentListOf(existingGrade),
            selectedGrade = existingGrade,
        )

        val edited = existingGrade.copy(percentage = 0.92)
        coordinator.save(edited, uiState, commands::add)

        assertEquals(listOf<WeightedCalculatorCommand>(WeightedCalculatorCommand.UpdateGrade(edited)), commands)
    }

    @Test
    fun startAddGradeWhenSelectedGradeScaleIsNullReturnsFalseAndNoDraft() {
        val commands = mutableListOf<WeightedCalculatorCommand>()
        val coordinator = WeightedGradeDialogCoordinator { "draft-uuid" }
        val uiState = WeightCalculatorUIState(
            gradeScaleNameAndIds = persistentListOf(GradeScaleNameAndId("Test Scale", "scale-1")),
            selectedGradeScale = null,
            grades = persistentListOf(),
            selectedGrade = null,
        )

        assertFalse(coordinator.startAddGrade(uiState))
        assertNull(coordinator.draftGrade)
        assertNull(coordinator.activeDialogGrade(uiState))
        assertTrue(commands.isEmpty())
    }
}
