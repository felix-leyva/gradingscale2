package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Option
import de.felixlf.gradingscale2.entities.repositories.WeightedGradesRepository

internal class DeleteWeightedGradeUseCaseImpl(
    private val weightedGradesRepository: WeightedGradesRepository,
) : DeleteWeightedGradeUseCase {
    override suspend fun invoke(weightedGradeId: String): Option<Unit> {
        return weightedGradesRepository.deleteWeightedGrade(weightedGradeId)
    }
}
