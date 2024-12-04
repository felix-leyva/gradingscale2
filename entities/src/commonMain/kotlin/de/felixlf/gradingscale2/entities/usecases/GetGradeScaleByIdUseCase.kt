package de.felixlf.gradingscale2.entities.usecases

import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.repositories.GradeScaleRepository
import kotlinx.coroutines.flow.Flow

fun interface GetGradeScaleByIdUseCase {
    operator fun invoke(gradeScaleId: String): Flow<GradeScale?>
}

internal class GetGradeScaleByIdUseCaseImpl(
    private val gradeScaleRepository: GradeScaleRepository,
) : GetGradeScaleByIdUseCase {
    override operator fun invoke(gradeScaleId: String): Flow<GradeScale?> =
        gradeScaleRepository.getGradeScaleById(gradeScaleId)
}
