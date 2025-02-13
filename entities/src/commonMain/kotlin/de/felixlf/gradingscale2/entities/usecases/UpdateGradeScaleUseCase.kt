package de.felixlf.gradingscale2.entities.usecases

import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.repositories.GradeScaleRepository
import kotlinx.coroutines.flow.firstOrNull

fun interface UpdateGradeScaleUseCase {
    suspend operator fun invoke(gradeScaleName: String, gradeScaleId: String, defaultGradeName: String): Result<String>
}

internal class UpdateGradeScaleUseCaseImpl(val gradeScaleRepository: GradeScaleRepository) : UpdateGradeScaleUseCase {
    override suspend operator fun invoke(gradeScaleName: String, gradeScaleId: String, defaultGradeName: String): Result<String> =
        runCatching {
            val grades = gradeScaleRepository.getGradeScaleById(gradeScaleId).firstOrNull()?.grades
                ?: throw IllegalStateException("No grade scales found")

            val initialGradeScale = GradeScale(
                gradeScaleName = gradeScaleName,
                id = gradeScaleId,
                totalPoints = 10.0,
                grades = grades,
            )
            gradeScaleRepository.upsertGradeScale(initialGradeScale).getOrThrow()
        }
}
