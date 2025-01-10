package de.felixlf.gradingscale2.daosimpl

import de.felixlf.gradingscale2.entities.daos.GradesDao
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.store.GradeScaleStoreProvider
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class GradesDaoStoreImpl(private val gradeScaleStoreProvider: GradeScaleStoreProvider) : GradesDao {
    override fun getGradeById(gradeId: String): Flow<Grade?> {
        return gradeScaleStoreProvider.flow.map { gradeScales ->
            gradeScales.flatMap { it.grades }
                .find { it.uuid == gradeId }
        }
    }

    override suspend fun upsertGrade(grade: Grade): Result<Unit> = runCatching {
        gradeScaleStoreProvider.gradeScalesStore.update { gradeScales ->
            gradeScales?.map { gradeScale ->
                if (gradeScale.id == grade.idOfGradeScale) {
                    val updatedGrades = gradeScale.grades.filterNot { it.uuid == grade.uuid } + grade
                    gradeScale.copy(grades = updatedGrades.toImmutableList())
                } else {
                    gradeScale
                }
            }?.toImmutableList()
        }
    }

    override suspend fun deleteGrade(gradeId: String): Result<Unit> = runCatching {
        gradeScaleStoreProvider.gradeScalesStore.update { gradeScales ->
            gradeScales?.map { gradeScale ->
                if (gradeScale.grades.any { it.uuid == gradeId }) {
                    val updatedGrades = gradeScale.grades.filterNot { it.uuid == gradeId }
                    gradeScale.copy(grades = updatedGrades.toImmutableList())
                } else {
                    gradeScale
                }
            }?.toImmutableList()
        }
    }
}
