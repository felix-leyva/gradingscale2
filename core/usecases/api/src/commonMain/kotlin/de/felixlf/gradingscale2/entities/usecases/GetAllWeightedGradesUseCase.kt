package de.felixlf.gradingscale2.entities.usecases

import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

fun interface GetAllWeightedGradesUseCase {
    operator fun invoke(): Flow<ImmutableList<WeightedGrade>>
}
