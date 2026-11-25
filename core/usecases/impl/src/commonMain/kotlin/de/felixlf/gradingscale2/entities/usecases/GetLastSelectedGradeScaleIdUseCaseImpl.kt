package de.felixlf.gradingscale2.entities.usecases

import de.felixlf.gradingscale2.entities.repositories.PreferencesRepository
import kotlinx.coroutines.flow.first

internal class GetLastSelectedGradeScaleIdUseCaseImpl(private val preferencesRepository: PreferencesRepository) : GetLastSelectedGradeScaleIdUseCase {
    override suspend fun invoke(): String? {
        return preferencesRepository.getLastSelectedGradeScaleId().first()?.takeIf { it.isNotBlank() }
    }
}
