package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Option
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.repositories.GradesRepository

internal class UpsertGradeUseCaseImpl(private val gradesRepository: GradesRepository) : UpsertGradeUseCase {
    override suspend fun invoke(grade: Grade): Option<Unit> {
        return gradesRepository.upsertGrade(grade).map { Unit }
    }
}
