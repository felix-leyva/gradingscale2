package de.felixlf.gradingscale2.entities.usecases

import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.repositories.GradesRepository
import kotlinx.coroutines.flow.Flow

fun interface GetGradeByUUIDUseCase {
    operator fun invoke(gradeUUID: String): Flow<Grade?>
}

internal class GetGradeByUUIDUseCaseImpl(private val gradesRepository: GradesRepository) : GetGradeByUUIDUseCase {
    override fun invoke(gradeUUID: String): Flow<Grade?> {
        return gradesRepository.getGradeById(gradeUUID)
    }
}
