package de.felixlf.gradingscale2.daosimpl

import arrow.core.Option
import arrow.core.raise.option
import de.felixlf.gradingscale2.entities.daos.GradeScaleDao
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.store.GradeScaleStoreProvider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class GradeScaleDaoStoreImpl(private val gradeScaleStoreProvider: GradeScaleStoreProvider) : GradeScaleDao {
    override fun getGradeScaleById(id: String): Flow<GradeScale?> {
        return gradeScaleStoreProvider.flow.map { gradeScales ->
            gradeScales.find { it.id == id }
        }
    }

    override fun getGradeScales(): Flow<ImmutableList<GradeScale>> {
        return gradeScaleStoreProvider.flow
    }

    override suspend fun upsertGradeScale(gradeScale: GradeScale): Option<Unit> = option {
        gradeScaleStoreProvider.gradeScalesStore.update { gradeScales ->
            gradeScales?.map { if (it.id == gradeScale.id) gradeScale else it }?.toImmutableList()
        }.bind()
    }

    override suspend fun deleteGradeScale(gradeScaleId: String): Option<Unit> = option {
        gradeScaleStoreProvider.gradeScalesStore.update { gradeScales ->
            gradeScales?.filterNot { it.id == gradeScaleId }?.toImmutableList()
        }.bind()
    }
}
