package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Option
import arrow.core.raise.option
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.repositories.GradeScaleRepository
import kotlinx.coroutines.flow.firstOrNull

fun interface UpdateGradeScaleUseCase {
    suspend operator fun invoke(gradeScaleName: String, gradeScaleId: String, defaultGradeName: String): Option<String>
}

internal class UpdateGradeScaleUseCaseImpl(val gradeScaleRepository: GradeScaleRepository) : UpdateGradeScaleUseCase {
    override suspend operator fun invoke(gradeScaleName: String, gradeScaleId: String, defaultGradeName: String): Option<String> =
        option {
            val grades = gradeScaleRepository.getGradeScaleById(gradeScaleId).firstOrNull()?.grades
                ?: raise()

            val initialGradeScale = GradeScale(
                gradeScaleName = gradeScaleName,
                id = gradeScaleId,
                totalPoints = 10.0,
                grades = grades,
            )
            gradeScaleRepository.upsertGradeScale(initialGradeScale).bind()
        }
}
