package de.felixlf.gradingscale2.entities.usecases

import de.felixlf.gradingscale2.entities.repositories.GradeScaleRepository

fun interface UpsertGradeScaleUseCase {
    operator fun invoke(): Result<Unit>
}

internal class UpsertGradeScaleUseCaseImpl(val gradeScaleRepository: GradeScaleRepository) : UpsertGradeScaleUseCase {
    override fun invoke(): Result<Unit> {
        TODO()
    }
}
