package de.felixlf.gradingscale2.entities.util

import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import kotlinx.collections.immutable.toImmutableList

/**
 * Comfort class to generate a grade scale with a given size.
 */
class MockGradeScalesGenerator(
    val size: Int = 20,
) {
    private val range = 0..size
    val percentages = range.map { it.toDouble() / size }
    val gradeNames = range.map { Char('A'.code + it) }.reversed()
    val gradeScaleNames = listOf("Hamburg", "Berlin", "Munich")

    val gradeScales =
        gradeScaleNames
            .mapIndexed { scaleIndex, gradeScaleName ->
                GradeScale(
                    id = scaleIndex.toString(),
                    gradeScaleName = gradeScaleName,
                    totalPoints = 10.0,
                    grades =
                    percentages
                        .mapIndexed { percentagesIndex, percentage ->
                            Grade(
                                namedGrade = gradeNames[percentagesIndex].toString(),
                                percentage = percentage,
                                idOfGradeScale = "$scaleIndex",
                                nameOfScale = gradeScaleName,
                                uuid = "${gradeNames[percentagesIndex]}_${gradeScaleName}_$percentagesIndex",
                            )
                        }.toImmutableList(),
                )
            }.toImmutableList()
}
