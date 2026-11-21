package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Option

fun interface DeleteGradeScaleUseCase {
    suspend operator fun invoke(gradeScaleId: String): Option<Unit>
}
