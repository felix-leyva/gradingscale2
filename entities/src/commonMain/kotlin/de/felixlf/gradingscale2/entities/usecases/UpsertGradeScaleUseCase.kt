package de.felixlf.gradingscale2.entities.usecases

import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.repositories.GradeScaleRepository
import kotlinx.collections.immutable.persistentListOf
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun interface UpsertGradeScaleUseCase {
    suspend operator fun invoke(gradeScaleName: String, gradeScaleId: String, defaultGradeName: String): Result<String>
}

internal class UpsertGradeScaleUseCaseImpl(val gradeScaleRepository: GradeScaleRepository) : UpsertGradeScaleUseCase {
    @OptIn(ExperimentalUuidApi::class)
    override suspend operator fun invoke(gradeScaleName: String, gradeScaleId: String, defaultGradeName: String): Result<String> =
        runCatching {
            val initialGrade = Grade(
                namedGrade = defaultGradeName,
                percentage = 0.5,
                idOfGradeScale = gradeScaleId,
                nameOfScale = gradeScaleName,
                uuid = Uuid.random().toString(),
            )
            val initialGradeScale = GradeScale(
                gradeScaleName = gradeScaleName,
                id = gradeScaleId,
                totalPoints = 10.0,
                grades = persistentListOf(initialGrade),
            )
            gradeScaleRepository.upsertGradeScale(initialGradeScale).getOrThrow()
        }
}
