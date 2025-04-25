package de.felixlf.gradingscale2.entities.usecases

import de.felixlf.gradingscale2.entities.repositories.PreferencesRepository
import kotlinx.coroutines.flow.first

/**
 * Use case to get the last selected grade scale ID.
 * Retrieves the ID of the last selected grade scale from the preferences repository.
 */
fun interface GetLastSelectedGradeScaleId {
    suspend operator fun invoke(): String?
}

internal class GetLastSelectedGradeScaleIdImpl(private val preferencesRepository: PreferencesRepository) : GetLastSelectedGradeScaleId {
    override suspend fun invoke(): String? {
        return preferencesRepository.getLastSelectedGradeScaleId().first()
    }
}
