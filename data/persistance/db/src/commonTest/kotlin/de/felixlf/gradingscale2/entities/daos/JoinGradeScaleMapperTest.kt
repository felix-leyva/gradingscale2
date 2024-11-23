package de.felixlf.gradingscale2.entities.daos

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JoinGradeScaleMapperTest {
    private val mapper = JoinGradeScaleMapper()

    @Test
    fun mapToJoinedGradeScaleWithGradeDao_returnsCorrectObject() {
        val result =
            mapper.mapToJoinedGradeScaleWithGradeDao(
                gradeScaleId = "scale1",
                gradeScaleName = "Scale 1",
                gradeUuid = "grade1",
                namedGrade = "A",
                percentage = 0.9,
                scaleId = "scale1",
            )

        assertEquals("scale1", result.gradeScaleId)
        assertEquals("Scale 1", result.gradeScaleName)
        assertEquals("grade1", result.gradeUuid)
        assertEquals("A", result.namedGrade)
        assertEquals(0.9, result.percentage)
        assertEquals("scale1", result.scaleId)
    }

    @Test
    fun mapToGradeScale_returnsCorrectGradeScale() {
        val joinedGradesAndScales =
            listOf(
                JoinedGradeScaleWithGradeDao(
                    gradeScaleId = "scale1",
                    gradeScaleName = "Scale 1",
                    gradeUuid = "grade1",
                    namedGrade = "A",
                    percentage = 0.90,
                    scaleId = "scale1",
                ),
                JoinedGradeScaleWithGradeDao(
                    gradeScaleId = "scale1",
                    gradeScaleName = "Scale 1",
                    gradeUuid = "grade2",
                    namedGrade = "B",
                    percentage = 0.80,
                    scaleId = "scale1",
                ),
            )

        val result = mapper.mapToGradeScale(joinedGradesAndScales)

        assertEquals(1, result.size)
        val gradeScale = result.first()
        assertEquals("scale1", gradeScale.id)
        assertEquals("Scale 1", gradeScale.gradeScaleName)
        assertEquals(1.0, gradeScale.totalPoints)
        assertEquals(2, gradeScale.sortedPointedGrades.size)
    }

    @Test
    fun mapToGradeScale_handlesEmptyList() {
        val result = mapper.mapToGradeScale(emptyList())

        assertTrue(result.isEmpty())
    }

    @Test
    fun mapToGradeScale_handlesSingleGrade() {
        val joinedGradesAndScales =
            listOf(
                JoinedGradeScaleWithGradeDao(
                    gradeScaleId = "scale1",
                    gradeScaleName = "Scale 1",
                    gradeUuid = "grade1",
                    namedGrade = "A",
                    percentage = 0.90,
                    scaleId = "scale1",
                ),
            )

        val result = mapper.mapToGradeScale(joinedGradesAndScales)

        assertEquals(1, result.size)
        val gradeScale = result.first()
        assertEquals("scale1", gradeScale.id)
        assertEquals("Scale 1", gradeScale.gradeScaleName)
        assertEquals(1.0, gradeScale.totalPoints)
        assertEquals(1, gradeScale.sortedPointedGrades.size)
    }
}
