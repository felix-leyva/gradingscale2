package de.felixlf.gradingscale2.entities.usecases

import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.repositories.GradeScaleRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.StateFlow

fun interface GetAllGradeScalesUseCase {
    operator fun invoke(): StateFlow<ImmutableList<GradeScale>>
}

internal class GetAllGradeScalesUseCaseImpl(
    private val gradeScaleRepository: GradeScaleRepository,
) : GetAllGradeScalesUseCase {
    override fun invoke(): StateFlow<ImmutableList<GradeScale>> =
        gradeScaleRepository.getGradeScales()
}
