package de.felixlf.gradingscale2.daosimpl

import de.felixlf.gradingscale2.entities.daos.GradeScaleDao
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.store.GradeScaleStoreProvider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class GradeScaleDaoStoreImpl(private val gradeScaleStoreProvider: GradeScaleStoreProvider) : GradeScaleDao {
    override fun getGradeScaleById(gradeScaleId: String): Flow<GradeScale?> {
        return gradeScaleStoreProvider.flow.map { gradeScales ->
            gradeScales.find { it.id == gradeScaleId }
        }
    }

    override fun getGradeScales(): Flow<ImmutableList<GradeScale>> {
        return gradeScaleStoreProvider.flow
    }

    override suspend fun upsertGradeScale(gradeScale: GradeScale): Result<Unit> = runCatching {
        gradeScaleStoreProvider.gradeScalesStore.update { gradeScales ->
            gradeScales?.map { if (it.id == gradeScale.id) gradeScale else it }?.toImmutableList()
        }
    }

    override suspend fun deleteGradeScale(gradeScaleId: String): Result<Unit> = runCatching {
        gradeScaleStoreProvider.gradeScalesStore.update { gradeScales ->
            gradeScales?.filterNot { it.id == gradeScaleId }?.toImmutableList()
        }
    }
}