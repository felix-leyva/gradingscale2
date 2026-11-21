package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Option
import de.felixlf.gradingscale2.entities.repositories.PreferencesRepository

internal class SetLastSelectedGradeScaleIdUseCaseImpl(
    private val preferencesRepository: PreferencesRepository,
) : SetLastSelectedGradeScaleIdUseCase {
    override suspend fun invoke(gradeScaleId: String): Option<Unit> {
        return preferencesRepository.setLastSelectedGradeScaleId(gradeScaleId)
    }
}
