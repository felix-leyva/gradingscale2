package de.felixlf.gradingscale2.entities.repositories

import de.felixlf.gradingscale2.entities.models.GradeScale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

/**
 * Repository for [GradeScale] entities.
 */
interface GradeScaleRepository {
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
    suspend fun upsertGradeScale(gradeScale: GradeScale): Result<Unit>

    /**
     * Delete a [GradeScale].
     */
    suspend fun deleteGradeScale(gradeScaleId: String): Result<Unit>
}
