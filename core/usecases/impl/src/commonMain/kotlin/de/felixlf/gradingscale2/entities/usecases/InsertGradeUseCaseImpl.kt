package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.repositories.GradeScaleRepository
import de.felixlf.gradingscale2.entities.repositories.GradesRepository
import kotlinx.coroutines.flow.first
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class InsertGradeUseCaseImpl(
    private val gradesRepository: GradesRepository,
    private val gradeScaleRepository: GradeScaleRepository,
) : InsertGradeUseCase {
    @OptIn(ExperimentalUuidApi::class)
    override suspend fun invoke(gradeScaleId: String, percentage: Double, namedGrade: String): Either<InsertGradeUseCaseError, Unit> =
        either {
            ensure(percentage.isFinite() && percentage in 0.0..1.0) { InsertGradeUseCaseError.InvalidPercentage }
            val gradeScales = gradeScaleRepository.gradeScales.first()
            val gradeScale =
                ensureNotNull(gradeScales.firstOrNull { it.id == gradeScaleId }) { InsertGradeUseCaseError.GradeScaleIdNotFound }
            ensure(gradeScale.grades.none { it.namedGrade == namedGrade }) { InsertGradeUseCaseError.GradeWithSameNameAlreadyExists }
            ensure(gradeScale.grades.none { it.percentage == percentage }) { InsertGradeUseCaseError.PercentageAlreadyExists }

            val newGrade = Grade(
                namedGrade = namedGrade,
                percentage = percentage,
                idOfGradeScale = gradeScaleId,
                nameOfScale = gradeScale.gradeScaleName,
                uuid = Uuid.random().toString(),
            )
            gradesRepository.upsertGrade(newGrade).toEither { InsertGradeUseCaseError.ErrorInsertingGrade }.bind()
        }
}
