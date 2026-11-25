package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Either

fun interface InsertGradeUseCase {
    suspend operator fun invoke(gradeScaleId: String, percentage: Double, namedGrade: String): Either<InsertGradeUseCaseError, Unit>
}

enum class InsertGradeUseCaseError {
    GradeWithSameNameAlreadyExists, GradeScaleIdNotFound, PercentageAlreadyExists, ErrorInsertingGrade, InvalidPercentage
}
