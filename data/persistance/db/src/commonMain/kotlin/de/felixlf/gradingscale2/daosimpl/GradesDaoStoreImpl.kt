package de.felixlf.gradingscale2.daosimpl

import arrow.core.Option
import de.felixlf.gradingscale2.entities.daos.GradesDao
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.store.GradeScaleStoreProvider
import de.felixlf.gradingscale2.store.GradeScalesStoreData
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class GradesDaoStoreImpl(private val gradeScaleStoreProvider: GradeScaleStoreProvider) : GradesDao {
    override fun getAllGradesFromGradeScale(gradeScaleId: String): Flow<List<Grade>> {
        return gradeScaleStoreProvider.flow.map { gradeScales ->
            gradeScales.firstOrNull { it.id == gradeScaleId }?.grades ?: emptyList()
        }
    }

    override fun getGradeById(gradeId: String): Flow<Grade?> {
        return gradeScaleStoreProvider.flow.map { gradeScales ->
            gradeScales.flatMap { it.grades }
                .find { it.uuid == gradeId }
        }
    }

    override suspend fun upsertGrade(grade: Grade): Option<Long> =
        Option.catch {
            val existing = gradeScaleStoreProvider.gradeScalesStore.get()
            if (existing?.gradeScales?.any { it.id == grade.idOfGradeScale } != true) {
                false
            } else {
                var upserted = false
                gradeScaleStoreProvider.gradeScalesStore.update { storeData ->
                    val current = storeData ?: GradeScalesStoreData(persistentListOf())
                    if (current.gradeScales.none { it.id == grade.idOfGradeScale }) {
                        current
                    } else {
                        upserted = true
                        val updatedScales = current.gradeScales.map { gradeScale ->
                            val withoutUuid = gradeScale.grades.filterNot { it.uuid == grade.uuid }
                            if (gradeScale.id == grade.idOfGradeScale) {
                                val syncedGrade = grade.copy(nameOfScale = gradeScale.gradeScaleName)
                                gradeScale.copy(grades = (withoutUuid + syncedGrade).toPersistentList())
                            } else if (withoutUuid.size != gradeScale.grades.size) {
                                gradeScale.copy(grades = withoutUuid.toPersistentList())
                            } else {
                                gradeScale
                            }
                        }.toPersistentList()
                        current.copy(gradeScales = updatedScales)
                    }
                }
                upserted
            }
        }.filter { upserted -> upserted }.map { 1L }

    override suspend fun deleteGrade(gradeId: String): Option<Unit> =
        Option.catch {
            val existing = gradeScaleStoreProvider.gradeScalesStore.get()
            if (existing?.gradeScales?.any { scale -> scale.grades.any { it.uuid == gradeId } } != true) {
                false
            } else {
                var deleted = false
                gradeScaleStoreProvider.gradeScalesStore.update { storeData ->
                    val current = storeData ?: GradeScalesStoreData(persistentListOf())
                    val updatedScales = current.gradeScales.map { gradeScale ->
                        if (gradeScale.grades.any { it.uuid == gradeId }) {
                            deleted = true
                            val updatedGrades = gradeScale.grades.filterNot { it.uuid == gradeId }
                            gradeScale.copy(grades = updatedGrades.toPersistentList())
                        } else {
                            gradeScale
                        }
                    }.toPersistentList()
                    if (deleted) current.copy(gradeScales = updatedScales) else current
                }
                deleted
            }
        }.filter { deleted -> deleted }.map {}
}
