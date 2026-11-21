package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Option
import de.felixlf.gradingscale2.entities.models.Grade

fun interface UpsertGradeUseCase {
    suspend operator fun invoke(grade: Grade): Option<Unit>
}
