package de.felixlf.gradingscale2.entities.daos

import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

internal class JoinGradeScaleMapper {
    fun mapToJoinedGradeScaleWithGradeDao(
        gradeScaleId: String,
        gradeScaleName: String,
        totalPoints: Double,
        gradeUuid: String?,
        namedGrade: String?,
        percentage: Double?,
        scaleId: String?,
    ): JoinedGradeScaleWithGradeDao =
        JoinedGradeScaleWithGradeDao(
            gradeScaleId = gradeScaleId,
            gradeScaleName = gradeScaleName,
            totalPoints = totalPoints,
            gradeUuid = gradeUuid,
            namedGrade = namedGrade,
            percentage = percentage,
            scaleId = scaleId,
        )

    fun mapToGradeScale(joinedGradesAndScales: List<JoinedGradeScaleWithGradeDao>): ImmutableList<GradeScale> =
        joinedGradesAndScales
            .groupBy { it.gradeScaleId }
            .map { (_, rows) ->
                val firstRow = rows.first()
                val gradesList =
                    rows
                        .mapNotNull { row ->
                            val uuid = row.gradeUuid ?: return@mapNotNull null
                            val namedGrade = row.namedGrade ?: return@mapNotNull null
                            val percentage = row.percentage ?: return@mapNotNull null
                            Grade(
                                namedGrade = namedGrade,
                                percentage = percentage,
                                idOfGradeScale = row.scaleId ?: row.gradeScaleId,
                                nameOfScale = row.gradeScaleName,
                                uuid = uuid,
                            )
                        }.toPersistentList()
                GradeScale(
                    id = firstRow.gradeScaleId,
                    gradeScaleName = firstRow.gradeScaleName,
                    totalPoints = firstRow.totalPoints,
                    grades = gradesList,
                )
            }.toPersistentList()
}
