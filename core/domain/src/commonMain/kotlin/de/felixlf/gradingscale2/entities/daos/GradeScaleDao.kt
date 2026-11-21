package de.felixlf.gradingscale2.entities.daos

import arrow.core.Option
import de.felixlf.gradingscale2.entities.models.GradeScale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

interface GradeScaleDao {
    /**
     * Get a [GradeScale] by its unique identifier.
     */
    fun getGradeScaleById(id: String): Flow<GradeScale?>

    /**
     * Get all [GradeScale]s.
     */
    fun getGradeScales(): Flow<ImmutableList<GradeScale>>

    /**
     * Insert or update a [GradeScale].
     */
    suspend fun upsertGradeScale(gradeScale: GradeScale): Option<Unit>

    /**
     * Delete a [GradeScale].
     */
    suspend fun deleteGradeScale(gradeScaleId: String): Option<Unit>
}
