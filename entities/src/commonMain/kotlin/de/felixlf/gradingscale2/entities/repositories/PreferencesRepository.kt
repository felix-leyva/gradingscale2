package de.felixlf.gradingscale2.entities.repositories

import arrow.core.Option
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    /**
     * Get the last selected grade scale ID.
     */
    fun getLastSelectedGradeScaleId(): Flow<String?>

    /**
     * Set the last selected grade scale ID.
     * @param gradeScaleId The ID of the grade scale to set as last selected.
     * @return An [Option] containing Unit if the operation was successful, or None if it failed.
     */
    suspend fun setLastSelectedGradeScaleId(gradeScaleId: String): Option<Unit>
}
