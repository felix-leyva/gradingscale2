package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Option

fun interface DeleteWeightedGradeUseCase {
    suspend operator fun invoke(weightedGradeId: String): Option<Unit>
}
