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
        assertEquals(2, gradeScale.grades.size)

        val gradeA = gradeScale.grades.first { it.namedGrade == "A" }
        assertEquals("grade1", gradeA.uuid)
        assertEquals(0.90, gradeA.percentage)
        assertEquals("scale1", gradeA.idOfGradeScale)
        assertEquals("Scale 1", gradeA.nameOfScale)

        val gradeB = gradeScale.grades.first { it.namedGrade == "B" }
        assertEquals("grade2", gradeB.uuid)
        assertEquals(0.80, gradeB.percentage)
        assertEquals("scale1", gradeB.idOfGradeScale)
        assertEquals("Scale 1", gradeB.nameOfScale)
    }

    @Test
    fun mapToGradeScale_groupsMultipleScalesCorrectly() {
        val joinedGradesAndScales = listOf(
            JoinedGradeScaleWithGradeDao(
                gradeScaleId = "scale1",
                gradeScaleName = "Scale 1",
                gradeUuid = "g1",
                namedGrade = "A",
                percentage = 0.90,
                scaleId = "scale1",
            ),
            JoinedGradeScaleWithGradeDao(
                gradeScaleId = "scale2",
                gradeScaleName = "Scale 2",
                gradeUuid = "g2",
                namedGrade = "Pass",
                percentage = 0.50,
                scaleId = "scale2",
            ),
            JoinedGradeScaleWithGradeDao(
                gradeScaleId = "scale1",
                gradeScaleName = "Scale 1",
                gradeUuid = "g3",
                namedGrade = "B",
                percentage = 0.80,
                scaleId = "scale1",
            ),
        )

        val result = mapper.mapToGradeScale(joinedGradesAndScales)

        assertEquals(2, result.size)

        val scale1 = result.first { it.id == "scale1" }
        assertEquals("Scale 1", scale1.gradeScaleName)
        assertEquals(2, scale1.grades.size)

        val scale2 = result.first { it.id == "scale2" }
        assertEquals("Scale 2", scale2.gradeScaleName)
        assertEquals(1, scale2.grades.size)
        assertEquals("Pass", scale2.grades.first().namedGrade)
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
