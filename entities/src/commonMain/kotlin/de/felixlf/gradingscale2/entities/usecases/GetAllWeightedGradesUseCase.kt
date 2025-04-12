package de.felixlf.gradingscale2.entities.usecases

import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade
import de.felixlf.gradingscale2.entities.repositories.WeightedGradesRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun interface GetAllWeightedGradesUseCase {
    operator fun invoke(): Flow<ImmutableList<WeightedGrade>>
}

class GetAllWeightedGradesUseCaseImpl(
    private val weightedGradesRepository: WeightedGradesRepository,
) : GetAllWeightedGradesUseCase {
    override fun invoke(): Flow<ImmutableList<WeightedGrade>> =
        weightedGradesRepository.getAllWeightedGrades().map { it.toImmutableList() }
}
