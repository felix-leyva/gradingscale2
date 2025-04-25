package de.felixlf.gradingscale2.sharedprefs

import arrow.core.Option
import arrow.core.raise.option
import de.felixlf.gradingscale2.entities.daos.PreferencesDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesDaoImpl(
    private val preferencesStore: PreferenceStore,
) : PreferencesDao {
    override fun getLastSelectedGradeScaleId(): Flow<String?> {
        return preferencesStore.updates.map { it?.lastSelectedGradeScaleId }
    }

    override suspend fun setLastSelectedGradeScaleId(gradeScaleId: String): Option<Unit> = option {
        preferencesStore.update { it?.copy(lastSelectedGradeScaleId = gradeScaleId) }.bind()
    }
}
