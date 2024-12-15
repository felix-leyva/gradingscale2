package de.felixlf.gradingscale2.entities.daos

import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

internal class JoinGradeScaleMapper {
    fun mapToJoinedGradeScaleWithGradeDao(
        gradeScaleId: String,
        gradeScaleName: String,
        gradeUuid: String,
        namedGrade: String,
        percentage: Double,
        scaleId: String,
    ): JoinedGradeScaleWithGradeDao =
        JoinedGradeScaleWithGradeDao(
            gradeScaleId = gradeScaleId,
            gradeScaleName = gradeScaleName,
            gradeUuid = gradeUuid,
            namedGrade = namedGrade,
            percentage = percentage,
            scaleId = scaleId,
        )

    fun mapToGradeScale(joinedGradesAndScales: List<JoinedGradeScaleWithGradeDao>): ImmutableList<GradeScale> =
        joinedGradesAndScales
            .groupBy { it.scaleId }
            .map { (_, grades) ->
                val firstGrade = grades.first()
                val gradesList =
                    grades
                        .map { grade ->
                            Grade(
                                namedGrade = grade.namedGrade,
                                percentage = grade.percentage,
                                idOfGradeScale = grade.scaleId,
                                nameOfScale = grade.gradeScaleName,
                                uuid = grade.gradeUuid,
                            )
                        }.toPersistentList()
                GradeScale(
                    id = firstGrade.scaleId,
                    gradeScaleName = firstGrade.gradeScaleName,
                    totalPoints = 1.0,
                    grades = gradesList,
                )
            }.toPersistentList()
}
