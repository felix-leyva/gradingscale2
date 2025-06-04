package de.felixlf.gradingscale2.entities.usecases

import de.felixlf.gradingscale2.entities.repositories.PreferencesRepository
import kotlinx.coroutines.flow.first

/**
 * Use case to get the last selected grade scale ID.
 * Retrieves the ID of the last selected grade scale from the preferences repository.
 */
fun interface GetLastSelectedGradeScaleIdUseCase {
    suspend operator fun invoke(): String?
}

internal class GetLastSelectedGradeScaleIdUseCaseImpl(private val preferencesRepository: PreferencesRepository) : GetLastSelectedGradeScaleIdUseCase {
    override suspend fun invoke(): String? {
        return preferencesRepository.getLastSelectedGradeScaleId().first()
    }
}
