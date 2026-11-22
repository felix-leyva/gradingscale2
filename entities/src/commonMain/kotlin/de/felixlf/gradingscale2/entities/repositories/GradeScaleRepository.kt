package de.felixlf.gradingscale2.entities.repositories

import arrow.core.Option
import de.felixlf.gradingscale2.entities.models.GradeScale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Repository for [GradeScale] entities.
 */
internal interface GradeScaleRepository {
    /**
     * Get a [GradeScale] by its unique identifier.
     */
    fun getGradeScaleById(id: String): Flow<GradeScale?>

    /**
     * Shared stream of all [GradeScale]s.
     */
    val gradeScales: SharedFlow<ImmutableList<GradeScale>>

    /**
     * Insert or update a [GradeScale] and return its id.
     */
    suspend fun upsertGradeScale(gradeScale: GradeScale): Option<String>

    /**
     * Delete a [GradeScale].
     */
    suspend fun deleteGradeScale(gradeScaleId: String): Option<Unit>
}
