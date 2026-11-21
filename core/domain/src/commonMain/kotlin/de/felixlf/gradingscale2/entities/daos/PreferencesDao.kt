package de.felixlf.gradingscale2.entities.daos

import arrow.core.Option
import kotlinx.coroutines.flow.Flow

/**
 * Allows the access and modification of user preferences.
 */
interface PreferencesDao {
    /**
     * Get the last selected grade scale ID.
     */
    fun getLastSelectedGradeScaleId(): Flow<String?>
    suspend fun setLastSelectedGradeScaleId(gradeScaleId: String): Option<Unit>
}
