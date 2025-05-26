package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Option
import de.felixlf.gradingscale2.entities.repositories.PreferencesRepository

fun interface SetLastSelectedGradeScaleIdUseCase {
    suspend operator fun invoke(gradeScaleId: String): Option<Unit>
}

internal class SetLastSelectedGradeScaleIdUseCaseImpl(
    private val preferencesRepository: PreferencesRepository,
) : SetLastSelectedGradeScaleIdUseCase {
    override suspend fun invoke(gradeScaleId: String): Option<Unit> {
        return preferencesRepository.setLastSelectedGradeScaleId(gradeScaleId)
    }
}
