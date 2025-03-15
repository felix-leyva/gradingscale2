package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Option
import de.felixlf.gradingscale2.entities.repositories.GradesRepository

fun interface DeleteGradeUseCase {
    suspend operator fun invoke(gradeId: String): Option<Unit>
}

internal class DeleteGradeUseCaseImpl(private val gradesRepository: GradesRepository) : DeleteGradeUseCase {
    override suspend fun invoke(gradeId: String): Option<Unit> {
        return gradesRepository.deleteGrade(gradeId)
    }
}
