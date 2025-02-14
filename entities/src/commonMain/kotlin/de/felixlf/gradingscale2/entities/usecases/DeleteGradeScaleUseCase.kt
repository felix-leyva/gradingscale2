package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Option
import de.felixlf.gradingscale2.entities.repositories.GradeScaleRepository

fun interface DeleteGradeScaleUseCase {
    suspend operator fun invoke(gradeScaleId: String): Option<Unit>
}

internal class DeleteGradeScaleUseCaseImpl(private val gradeScaleRepository: GradeScaleRepository) : DeleteGradeScaleUseCase {
    override suspend fun invoke(gradeScaleId: String): Option<Unit> {
        return gradeScaleRepository.deleteGradeScale(gradeScaleId)
    }
}
