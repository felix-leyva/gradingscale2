package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Option
import arrow.core.raise.option
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.repositories.GradeScaleRepository
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.firstOrNull
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun interface InsertGradeScaleUseCase {
    suspend operator fun invoke(gradeScaleName: String, defaultGradeName: String): Option<String>
}

internal class InsertGradeScaleUseCaseImpl(val gradeScaleRepository: GradeScaleRepository) : InsertGradeScaleUseCase {
    @OptIn(ExperimentalUuidApi::class)
    override suspend operator fun invoke(gradeScaleName: String, defaultGradeName: String): Option<String> =
        option {
            val maxAvailableId =
                ensureNotNull(gradeScaleRepository.getGradeScales().firstOrNull()?.mapNotNull { it.id.toIntOrNull() }?.maxOrNull()) + 1

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
            gradeScaleRepository.upsertGradeScale(initialGradeScale).bind()
        }
}
