package de.felixlf.gradingscale2.entities.daos

import de.felixlf.gradingscale2.entities.models.Grade

internal class GradeMapper {
    fun mapToGrade(
        gradeScaleName: String,
        uuid: String,
        namedGrade: String,
        percentage: Double,
        scaleId: String,
    ): Grade =
        Grade(
            namedGrade = namedGrade,
            percentage = percentage,
            idOfGradeScale = scaleId,
            nameOfScale = gradeScaleName,
            uuid = uuid,
        )
}
