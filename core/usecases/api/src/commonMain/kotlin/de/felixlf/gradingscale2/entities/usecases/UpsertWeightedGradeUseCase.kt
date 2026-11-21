package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Option
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade

fun interface UpsertWeightedGradeUseCase {
    suspend operator fun invoke(weightedGrade: WeightedGrade): Option<Long>
}
