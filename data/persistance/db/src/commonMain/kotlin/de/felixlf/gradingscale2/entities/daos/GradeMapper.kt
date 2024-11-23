package de.felixlf.gradingscale2.entities.daos

import de.felixlf.gradingscale2.entities.models.Grade

internal class GradeMapper {
    fun mapToGrade(
        uuid: String,
        namedGrade: String,
        percentage: Double,
        scaleId: String,
    ): Grade =
        Grade(
            namedGrade = namedGrade,
            percentage = percentage,
            nameOfScale = scaleId,
            uuid = uuid,
        )
}
