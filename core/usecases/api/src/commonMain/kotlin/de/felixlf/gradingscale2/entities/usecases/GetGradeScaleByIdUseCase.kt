package de.felixlf.gradingscale2.entities.usecases

import de.felixlf.gradingscale2.entities.models.GradeScale
import kotlinx.coroutines.flow.Flow

fun interface GetGradeScaleByIdUseCase {
    operator fun invoke(gradeScaleId: String): Flow<GradeScale?>
}
