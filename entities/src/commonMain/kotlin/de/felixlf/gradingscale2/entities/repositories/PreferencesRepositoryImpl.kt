package de.felixlf.gradingscale2.entities.repositories

import arrow.core.Option
import de.felixlf.gradingscale2.entities.daos.PreferencesDao
import kotlinx.coroutines.flow.Flow

class PreferencesRepositoryImpl(private val preferencesDao: PreferencesDao) : PreferencesRepository {
    /**
     * Get the last selected grade scale ID.
     */
    override fun getLastSelectedGradeScaleId(): Flow<String?> = preferencesDao.getLastSelectedGradeScaleId()

    /**
     * Set the last selected grade scale ID.
     * @param gradeScaleId The ID of the grade scale to set as last selected.
     * @return An [Option] containing Unit if the operation was successful, or None if it failed.
     */
    override suspend fun setLastSelectedGradeScaleId(gradeScaleId: String): Option<Unit> =
        preferencesDao.setLastSelectedGradeScaleId(gradeScaleId)
}
