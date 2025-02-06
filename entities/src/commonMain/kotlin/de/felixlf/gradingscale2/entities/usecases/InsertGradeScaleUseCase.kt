package de.felixlf.gradingscale2.entities.usecases

import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.repositories.GradeScaleRepository
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.firstOrNull
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun interface InsertGradeScaleUseCase {
    suspend operator fun invoke(gradeScaleName: String, defaultGradeName: String): Result<String>
}

internal class InsertGradeScaleUseCaseImpl(val gradeScaleRepository: GradeScaleRepository) : InsertGradeScaleUseCase {
    @OptIn(ExperimentalUuidApi::class)
    override suspend operator fun invoke(gradeScaleName: String, defaultGradeName: String): Result<String> =
        runCatching {
            val maxAvailableId = gradeScaleRepository.getGradeScales().firstOrNull()?.mapNotNull { it.id.toIntOrNull() }?.maxOrNull()
                ?: throw IllegalStateException("No grade scales found")
            val initialGrade = Grade(
                namedGrade = defaultGradeName,
                percentage = 0.5,
                idOfGradeScale = maxAvailableId.toString(),
                nameOfScale = gradeScaleName,
                uuid = Uuid.random().toString(),
            )
            val initialGradeScale = GradeScale(
                gradeScaleName = gradeScaleName,
                id = maxAvailableId.toString(),
                totalPoints = 10.0,
                grades = persistentListOf(initialGrade),
            )
            gradeScaleRepository.upsertGradeScale(initialGradeScale).getOrThrow()
        }
}
