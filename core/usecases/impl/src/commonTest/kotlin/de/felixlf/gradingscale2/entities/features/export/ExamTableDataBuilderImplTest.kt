package de.felixlf.gradingscale2.entities.features.export

import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.models.TableExportOptions
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExamTableDataBuilderImplTest {

    private val builder = ExamTableDataBuilderImpl()

    private val sampleGrades = persistentListOf(
        Grade(namedGrade = "1", percentage = 0.90, idOfGradeScale = "scale-1", nameOfScale = "Scale 1", uuid = "u1"),
        Grade(namedGrade = "2", percentage = 0.75, idOfGradeScale = "scale-1", nameOfScale = "Scale 1", uuid = "u2"),
        Grade(namedGrade = "3", percentage = 0.60, idOfGradeScale = "scale-1", nameOfScale = "Scale 1", uuid = "u3"),
        Grade(namedGrade = "4", percentage = 0.50, idOfGradeScale = "scale-1", nameOfScale = "Scale 1", uuid = "u4"),
        Grade(namedGrade = "5", percentage = 0.00, idOfGradeScale = "scale-1", nameOfScale = "Scale 1", uuid = "u5"),
    )

    private val sampleScale = GradeScale(
        id = "scale-1",
        gradeScaleName = "Sample Scale",
        totalPoints = 80.0,
        grades = sampleGrades,
    )

    @Test
    fun emptyGrades_returnsEmptyList() {
        val emptyScale = sampleScale.copy(grades = persistentListOf())
        val options = TableExportOptions(sortDescending = true)

        val result = builder.buildRows(emptyScale, options, totalPoints = 100.0)

        assertTrue(result.isEmpty())
    }

    @Test
    fun totalPointsZeroOrNegative_fallsBackToGradeScaleTotalPoints() {
        val options = TableExportOptions(sortDescending = true)

        // totalPoints = 0.0 should fall back to sampleScale.totalPoints (80.0)
        val resultZero = builder.buildRows(sampleScale, options, totalPoints = 0.0)
        assertEquals(5, resultZero.size)
        val topRowZero = resultZero.first()
        assertEquals(72.0, topRowZero.minPoints, 0.001) // 0.90 * 80.0
        assertEquals(80.0, topRowZero.maxPoints, 0.001) // 1.0 * 80.0

        // totalPoints negative should also fall back to sampleScale.totalPoints (80.0)
        val resultNegative = builder.buildRows(sampleScale, options, totalPoints = -10.0)
        assertEquals(5, resultNegative.size)
        val topRowNeg = resultNegative.first()
        assertEquals(72.0, topRowNeg.minPoints, 0.001)
        assertEquals(80.0, topRowNeg.maxPoints, 0.001)
    }

    @Test
    fun positiveTotalPoints_usesCustomTotalPoints() {
        val options = TableExportOptions(sortDescending = true)

        val result = builder.buildRows(sampleScale, options, totalPoints = 100.0)
        assertEquals(5, result.size)

        // Top grade (1): min% = 0.90, max% = maxOf(1.0, 0.90) = 1.0
        val row1 = result[0]
        assertEquals("1", row1.gradeName)
        assertEquals(90.0, row1.minPoints, 0.001)
        assertEquals(100.0, row1.maxPoints, 0.001)
        assertEquals(0.90, row1.minPercentage, 0.001)
        assertEquals(1.0, row1.maxPercentage, 0.001)

        // Second grade (2): min% = 0.75, max% = sortedGrades[0].percentage = 0.90
        val row2 = result[1]
        assertEquals("2", row2.gradeName)
        assertEquals(75.0, row2.minPoints, 0.001)
        assertEquals(90.0, row2.maxPoints, 0.001)
        assertEquals(0.75, row2.minPercentage, 0.001)
        assertEquals(0.90, row2.maxPercentage, 0.001)
    }

    @Test
    fun sortOrder_descendingVsAscending() {
        val descOptions = TableExportOptions(sortDescending = true)
        val ascOptions = TableExportOptions(sortDescending = false)

        val descResult = builder.buildRows(sampleScale, descOptions, totalPoints = 100.0)
        val ascResult = builder.buildRows(sampleScale, ascOptions, totalPoints = 100.0)

        assertEquals("1", descResult.first().gradeName)
        assertEquals("5", descResult.last().gradeName)

        assertEquals("5", ascResult.first().gradeName)
        assertEquals("1", ascResult.last().gradeName)

        assertEquals(descResult.reversed(), ascResult)
    }
}
