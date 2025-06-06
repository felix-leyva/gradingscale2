package de.felixlf.gradingscale2.entities.usecases

import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.repositories.GradeScaleRepository
import de.felixlf.gradingscale2.entities.util.GradeScaleGenerator
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart

fun interface GetAllGradeScalesUseCase {
    operator fun invoke(): Flow<ImmutableList<GradeScale>>
}

internal class GetAllGradeScalesUseCaseImpl(
    private val gradeScaleRepository: GradeScaleRepository,
    private val generator: GradeScaleGenerator,
) : GetAllGradeScalesUseCase {
    override fun invoke(): Flow<ImmutableList<GradeScale>> =
        gradeScaleRepository.getGradeScales()
            .onStart {
                if (gradeScaleRepository.getGradeScales().first().isEmpty()) {
                    generator.getGradeScales()
                        .forEach { gradeScaleRepository.upsertGradeScale(it) }
                }
            }
}
