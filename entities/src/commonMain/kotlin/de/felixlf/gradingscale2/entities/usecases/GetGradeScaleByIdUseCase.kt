package de.felixlf.gradingscale2.entities.usecases

import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.repositories.GradeScaleRepository
import kotlinx.coroutines.flow.StateFlow

fun interface GetGradeScaleByIdUseCase {
    operator fun invoke(gradeScaleId: String): StateFlow<GradeScale?>
}

internal class GetGradeScaleByIdUseCaseImpl(
    private val gradeScaleRepository: GradeScaleRepository,
) : GetGradeScaleByIdUseCase {
    override fun invoke(gradeScaleId: String): StateFlow<GradeScale?> =
        gradeScaleRepository.getGradeScaleById(gradeScaleId)
}
