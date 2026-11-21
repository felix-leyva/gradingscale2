package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Option

fun interface UpdateGradeScaleUseCase {
    suspend operator fun invoke(gradeScaleName: String, gradeScaleId: String, defaultGradeName: String): Option<String>
}
