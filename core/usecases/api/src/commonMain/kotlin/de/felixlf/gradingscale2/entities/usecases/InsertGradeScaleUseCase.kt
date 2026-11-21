package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Option

fun interface InsertGradeScaleUseCase {
    suspend operator fun invoke(gradeScaleName: String, defaultGradeName: String): Option<String>
}
