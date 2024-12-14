package de.felixlf.gradingscale2.entities.usecases

import de.felixlf.gradingscale2.entities.models.Grade

fun interface UpdateSingleGradeUseCase {
    operator fun invoke(grade: Grade): Result<Unit>
}
