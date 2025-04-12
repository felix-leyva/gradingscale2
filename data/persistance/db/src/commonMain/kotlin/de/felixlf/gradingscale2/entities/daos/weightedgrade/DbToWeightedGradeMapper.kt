package de.felixlf.gradingscale2.entities.daos.weightedgrade

import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade

fun interface DbToWeightedGradeMapper {
    operator fun invoke(
        uuid: String,
        percentage: Double,
        weight: Double,
    ): WeightedGrade
}

internal class DbToWeightedGradeMapperImpl : DbToWeightedGradeMapper {
    override operator fun invoke(uuid: String, percentage: Double, weight: Double): WeightedGrade =
        WeightedGrade(
            percentage = percentage,
            weight = weight,
            uuid = uuid,
        )
}
