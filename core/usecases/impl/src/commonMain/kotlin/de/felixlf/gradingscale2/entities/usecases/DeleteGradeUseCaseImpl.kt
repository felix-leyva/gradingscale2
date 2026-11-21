package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Option
import de.felixlf.gradingscale2.entities.repositories.GradesRepository

internal class DeleteGradeUseCaseImpl(private val gradesRepository: GradesRepository) : DeleteGradeUseCase {
    override suspend fun invoke(gradeId: String): Option<Unit> {
        return gradesRepository.deleteGrade(gradeId)
    }
}
