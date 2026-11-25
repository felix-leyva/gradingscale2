package de.felixlf.gradingscale2.daosimpl

import arrow.core.Option
import de.felixlf.gradingscale2.entities.daos.GradeScaleDao
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.store.GradeScaleStoreProvider
import de.felixlf.gradingscale2.store.GradeScalesStoreData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
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

    override suspend fun upsertGradeScale(gradeScale: GradeScale): Option<Unit> =
        Option.catch {
            val normalizedGradeScale = gradeScale.copy(
                grades = gradeScale.grades.map { grade ->
                    grade.copy(
                        idOfGradeScale = gradeScale.id,
                        nameOfScale = gradeScale.gradeScaleName,
                    )
                }.toPersistentList(),
            )
            gradeScaleStoreProvider.gradeScalesStore.update { storeData ->
                val current = storeData ?: GradeScalesStoreData(persistentListOf())
                val existing = current.gradeScales.any { scale -> scale.id == normalizedGradeScale.id }
                val updatedList = if (existing) {
                    current.gradeScales.map { scale ->
                        if (scale.id == normalizedGradeScale.id) normalizedGradeScale else scale
                    }
                } else {
                    current.gradeScales + normalizedGradeScale
                }.toPersistentList()
                GradeScalesStoreData(updatedList)
            }
        }

    override suspend fun deleteGradeScale(gradeScaleId: String): Option<Unit> =
        Option.catch {
            val existing = gradeScaleStoreProvider.gradeScalesStore.get()
            if (existing?.gradeScales?.any { scale -> scale.id == gradeScaleId } != true) {
                false
            } else {
                var deleted = false
                gradeScaleStoreProvider.gradeScalesStore.update { storeData ->
                    val current = storeData ?: GradeScalesStoreData(persistentListOf())
                    if (current.gradeScales.any { scale -> scale.id == gradeScaleId }) {
                        deleted = true
                        val updatedList = current.gradeScales
                            .filterNot { scale -> scale.id == gradeScaleId }
                            .toPersistentList()
                        GradeScalesStoreData(updatedList)
                    } else {
                        current
                    }
                }
                deleted
            }
        }.filter { deleted -> deleted }.map {}
}
