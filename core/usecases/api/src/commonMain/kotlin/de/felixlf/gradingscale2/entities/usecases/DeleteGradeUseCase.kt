package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Option

fun interface DeleteGradeUseCase {
    suspend operator fun invoke(gradeId: String): Option<Unit>
}
