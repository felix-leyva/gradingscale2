package de.felixlf.gradingscale2.entities.features.list

import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GradeScaleListUIStateTest {

    private val testScaleId = "test-scale-123"
    private val testScaleName = "Standard High School"

    private val testGrades = persistentListOf(
        Grade(namedGrade = "A", percentage = 0.95, idOfGradeScale = testScaleId, nameOfScale = testScaleName, uuid = "uuid-a"),
        Grade(namedGrade = "B", percentage = 0.85, idOfGradeScale = testScaleId, nameOfScale = testScaleName, uuid = "uuid-b"),
        Grade(namedGrade = "C", percentage = 0.75, idOfGradeScale = testScaleId, nameOfScale = testScaleName, uuid = "uuid-c"),
        Grade(namedGrade = "D", percentage = 0.65, idOfGradeScale = testScaleId, nameOfScale = testScaleName, uuid = "uuid-d"),
        Grade(namedGrade = "F", percentage = 0.50, idOfGradeScale = testScaleId, nameOfScale = testScaleName, uuid = "uuid-f"),
    )

    private val testGradeScale = GradeScale(
        id = testScaleId,
        gradeScaleName = testScaleName,
        totalPoints = 100.0,
        grades = testGrades,
    )

    private val testNamesWithId = persistentListOf(
        GradeScaleListUIState.GradeScaleNameWithId(gradeScaleName = "Scale 1", gradeScaleId = "id-1"),
        GradeScaleListUIState.GradeScaleNameWithId(gradeScaleName = "Scale 2", gradeScaleId = "id-2"),
    )

    @Test
    fun initialStateWithNullSelectedGradeScale() {
        val uiState = GradeScaleListUIState(
            selectedGradeScale = null,
            gradeScalesNamesWithId = testNamesWithId,
        )

        assertFalse(uiState.hasSelectedGradeScale)
        assertNull(uiState.selectedGradeScaleId)
        assertEquals("", uiState.selectedGradeScaleName)
        assertEquals(10.0, uiState.totalPoints)
        assertEquals("", uiState.totalPointsString)
        assertTrue(uiState.gradeItems.isEmpty())
        assertTrue(uiState.isEmpty)
        assertEquals(0, uiState.gradeCount)
        assertEquals(2, uiState.gradeScaleItems.size)
        assertEquals("Scale 1", uiState.gradeScaleItems[0].name)
        assertEquals("id-1", uiState.gradeScaleItems[0].id)
    }

    @Test
    fun stateWithSelectedGradeScaleDerivesAllFields() {
        val uiState = GradeScaleListUIState(
            selectedGradeScale = testGradeScale,
            gradeScalesNamesWithId = testNamesWithId,
        )

        assertTrue(uiState.hasSelectedGradeScale)
        assertEquals(testScaleId, uiState.selectedGradeScaleId)
        assertEquals(testScaleName, uiState.selectedGradeScaleName)
        assertEquals(100.0, uiState.totalPoints)
        assertEquals("100", uiState.totalPointsString)
        assertFalse(uiState.isEmpty)
        assertEquals(5, uiState.gradeCount)

        val items = uiState.gradeItems
        assertEquals(5, items.size)

        // Grade A
        assertEquals("uuid-a", items[0].uuid)
        assertEquals("A", items[0].namedGrade)
        assertEquals(0.95, items[0].percentage)
        assertEquals(0.95f, items[0].percentageProgress)
        assertEquals("95 %", items[0].percentageFormatted)
        assertEquals(95.0, items[0].points)
        assertEquals("95", items[0].pointsFormatted)

        // Grade B
        assertEquals("uuid-b", items[1].uuid)
        assertEquals("B", items[1].namedGrade)
        assertEquals(0.85, items[1].percentage)
        assertEquals("85 %", items[1].percentageFormatted)
        assertEquals(85.0, items[1].points)
        assertEquals("85", items[1].pointsFormatted)

        // Grade C
        assertEquals("uuid-c", items[2].uuid)
        assertEquals("C", items[2].namedGrade)
        assertEquals(0.75, items[2].percentage)
        assertEquals("75 %", items[2].percentageFormatted)
        assertEquals(75.0, items[2].points)
        assertEquals("75", items[2].pointsFormatted)

        // Grade D
        assertEquals("uuid-d", items[3].uuid)
        assertEquals("D", items[3].namedGrade)
        assertEquals(0.65, items[3].percentage)
        assertEquals("65 %", items[3].percentageFormatted)
        assertEquals(65.0, items[3].points)
        assertEquals("65", items[3].pointsFormatted)

        // Grade F
        assertEquals("uuid-f", items[4].uuid)
        assertEquals("F", items[4].namedGrade)
        assertEquals(0.50, items[4].percentage)
        assertEquals("50 %", items[4].percentageFormatted)
        assertEquals(50.0, items[4].points)
        assertEquals("50", items[4].pointsFormatted)
    }

    @Test
    fun decimalPointsCalculationsAreFormattedCorrectly() {
        val scaleWithDecimals = testGradeScale.copy(totalPoints = 37.5)
        val uiState = GradeScaleListUIState(
            selectedGradeScale = scaleWithDecimals,
            gradeScalesNamesWithId = persistentListOf(),
        )

        assertEquals("37.5", uiState.totalPointsString)
        // 0.95 * 37.5 = 35.625 -> 35.63 rounded
        assertEquals("35.63", uiState.gradeItems[0].pointsFormatted)
    }
}
