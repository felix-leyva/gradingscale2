package de.felixlf.gradingscale2.entities.usecases

import de.felixlf.gradingscale2.entities.models.Grade
import kotlinx.coroutines.flow.Flow

fun interface GetGradeByUUIDUseCase {
    operator fun invoke(gradeUUID: String): Flow<Grade?>
}
