package de.felixlf.gradingscale2.entities.util

import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * Comfort class to generate a grade scale with a given size. This should only be used for Compose previews generation and testing.
 * @param size The size of the grade scale.
 */
class MockGradeScalesGenerator(
    val size: Int = 20,
) : GradeScaleGenerator {
    val gradeScaleNames: PersistentList<String> = persistentListOf("Hamburg", "Berlin", "Munich")
    val gradeScales: ImmutableList<GradeScale> = run {
        val range = 0..size
        val percentages = range.map { it.toDouble() / size }
        val gradeNames = range.map { Char('A'.code + it) }.reversed()
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

    override suspend fun getGradeScales(): List<GradeScale> = gradeScales
}
