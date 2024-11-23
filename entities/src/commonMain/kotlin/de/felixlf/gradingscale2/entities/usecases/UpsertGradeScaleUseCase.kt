package de.felixlf.gradingscale2.entities.usecases

fun interface UpsertGradeScaleUseCase {
    operator fun invoke(): Result<Unit>
}

internal class UpsertGradeScaleUseCaseImpl() : UpsertGradeScaleUseCase {
    override fun invoke(): Result<Unit> {
        return Result.success(Unit)
    }
}
