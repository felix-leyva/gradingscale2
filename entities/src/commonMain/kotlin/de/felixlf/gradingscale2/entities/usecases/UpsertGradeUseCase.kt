package de.felixlf.gradingscale2.entities.usecases

import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.repositories.GradesRepository

fun interface UpsertGradeUseCase {
    suspend operator fun invoke(grade: Grade, scaleId: String): Result<Unit>
}

internal class UpsertGradeUseCaseImpl(
    private val gradesRepository: GradesRepository,
) : UpsertGradeUseCase {
    override suspend fun invoke(grade: Grade, scaleId: String): Result<Unit> {
        return gradesRepository.upsertGrade(grade, scaleId)
    }
}
