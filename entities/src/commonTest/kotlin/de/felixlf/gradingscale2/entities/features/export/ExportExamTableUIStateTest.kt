package de.felixlf.gradingscale2.entities.features.export

import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.models.TableExportOptions
import de.felixlf.gradingscale2.entities.models.TableHeaders
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExportExamTableUIStateTest {

    private val testGradeScale = GradeScale(
        id = "test-1",
        gradeScaleName = "Math Exam",
        totalPoints = 20.0,
        grades = persistentListOf(
            Grade(namedGrade = "A", percentage = 0.90, idOfGradeScale = "test-1", nameOfScale = "Math Exam", uuid = "g1"),
            Grade(namedGrade = "B", percentage = 0.80, idOfGradeScale = "test-1", nameOfScale = "Math Exam", uuid = "g2"),
            Grade(namedGrade = "F", percentage = 0.00, idOfGradeScale = "test-1", nameOfScale = "Math Exam", uuid = "g3"),
        ),
    )

    private val builder = ExamTableDataBuilderImpl()

    @Test
    fun derivedProperties_whenGradeScaleProvided() {
        val options = TableExportOptions(includePercentage = true, showAsRange = false, sortDescending = true)
        val rows = builder.buildRows(testGradeScale, options, 20.0).toImmutableList()
        val state = ExportExamTableUIState(
            gradeScale = testGradeScale,
            totalPoints = 20.0,
            options = options,
            headers = TableHeaders("Grade", "Points", "%"),
            rows = rows,
        )

        assertTrue(state.hasScale)
        assertEquals("Math Exam", state.scaleName)
        assertEquals("Math Exam • 20 pts", state.subtitleText)
        assertFalse(state.isEmpty)
        assertTrue(state.canExport)
        assertEquals(3, state.rows.size)

        // First row (Grade A)
        assertEquals("A", state.rows[0].gradeName)
        assertEquals(18.0, state.rows[0].minPoints)
        assertEquals(20.0, state.rows[0].maxPoints)

        // Last row (Grade F)
        assertEquals("F", state.rows[2].gradeName)
        assertEquals(0.0, state.rows[2].minPoints)
    }

    @Test
    fun derivedRows_respectsSortAscending() {
        val options = TableExportOptions(sortDescending = false)
        val rows = builder.buildRows(testGradeScale, options, 20.0).toImmutableList()
        val state = ExportExamTableUIState(
            gradeScale = testGradeScale,
            totalPoints = 20.0,
            options = options,
            rows = rows,
        )

        assertEquals("F", state.rows[0].gradeName)
        assertEquals("B", state.rows[1].gradeName)
        assertEquals("A", state.rows[2].gradeName)
    }

    @Test
    fun derivedProperties_whenNoGradeScale() {
        val state = ExportExamTableUIState(gradeScale = null)

        assertFalse(state.hasScale)
        assertEquals("", state.scaleName)
        assertEquals("", state.subtitleText)
        assertTrue(state.isEmpty)
        assertFalse(state.canExport)
        assertTrue(state.rows.isEmpty())
    }
}
