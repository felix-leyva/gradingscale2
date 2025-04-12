package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Option
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade
import de.felixlf.gradingscale2.entities.repositories.WeightedGradesRepository

fun interface UpsertWeightedGradeUseCase {
    suspend operator fun invoke(weightedGrade: WeightedGrade): Option<Unit>
}

internal class UpsertWeightedGradeUseCaseImpl(private val weightedGradesRepository: WeightedGradesRepository) :
    UpsertWeightedGradeUseCase {
    override suspend fun invoke(weightedGrade: WeightedGrade): Option<Unit> {
        return weightedGradesRepository.upsertWeightedGrade(weightedGrade)
    }
}
