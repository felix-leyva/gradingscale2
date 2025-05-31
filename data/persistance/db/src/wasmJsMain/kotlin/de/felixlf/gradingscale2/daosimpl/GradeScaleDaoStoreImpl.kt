package de.felixlf.gradingscale2.daosimpl

import arrow.core.Option
import arrow.core.raise.option
import de.felixlf.gradingscale2.entities.daos.GradeScaleDao
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.store.GradeScaleStoreProvider
import de.felixlf.gradingscale2.store.GradeScalesStoreData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
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
        gradeScaleStoreProvider.gradeScalesStore.update { storeData ->
            storeData?.let {
                val existing = it.gradeScales.any { scale -> scale.id == gradeScale.id }
                val updatedList = if (existing) {
                    it.gradeScales.map { scale -> if (scale.id == gradeScale.id) gradeScale else scale }
                } else {
                    it.gradeScales + gradeScale
                }.toPersistentList()
                println("DEBUG: Updated GradeScalesStoreData: $updatedList")
                GradeScalesStoreData(updatedList)
            }
        }.bind()
    }

    override suspend fun deleteGradeScale(gradeScaleId: String): Option<Unit> = option {
        gradeScaleStoreProvider.gradeScalesStore.update { storeData ->
            storeData?.let {
                val updatedList = it.gradeScales
                    .filterNot { scale -> scale.id == gradeScaleId }
                    .toPersistentList()
                GradeScalesStoreData(updatedList)
            }
        }.bind()
    }
}
