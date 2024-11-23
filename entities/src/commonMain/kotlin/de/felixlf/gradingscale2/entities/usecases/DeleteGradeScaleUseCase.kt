package de.felixlf.gradingscale2.entities.usecases

fun interface DeleteGradeScaleUseCase {
    operator fun invoke(gradeScaleId: String): Result<Unit>
}

internal class DeleteGradeScaleUseCaseImpl() : DeleteGradeScaleUseCase {
    override fun invoke(gradeScaleId: String): Result<Unit> {
        return Result.success(Unit)
    }
}
