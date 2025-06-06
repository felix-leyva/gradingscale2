package de.felixlf.gradingscale2.daosimpl

import arrow.core.Option
import arrow.core.raise.option
import de.felixlf.gradingscale2.entities.daos.GradesDao
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.store.GradeScaleStoreProvider
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

    override suspend fun upsertGrade(grade: Grade): Option<Long> = option {
        gradeScaleStoreProvider.gradeScalesStore.update { gradeScales ->
            val updatedGrades = gradeScales?.gradeScales?.map { gradeScale ->
                if (gradeScale.id == grade.idOfGradeScale) {
                    val updatedGrades = gradeScale.grades.filterNot { it.uuid == grade.uuid } + grade
                    gradeScale.copy(grades = updatedGrades.toPersistentList())
                } else {
                    gradeScale
                }
            }?.toPersistentList()
            updatedGrades?.let(gradeScales::copy)
        }
        1
    }

    override suspend fun deleteGrade(gradeId: String): Option<Unit> = option {
        gradeScaleStoreProvider.gradeScalesStore.update { gradeScales ->
            val updatedGrades = gradeScales?.gradeScales?.map { gradeScale ->
                if (gradeScale.grades.any { it.uuid == gradeId }) {
                    val updatedGrades = gradeScale.grades.filterNot { it.uuid == gradeId }
                    gradeScale.copy(grades = updatedGrades.toPersistentList())
                } else {
                    gradeScale
                }
            }?.toPersistentList()
            updatedGrades?.let(gradeScales::copy)
        }
    }
}
