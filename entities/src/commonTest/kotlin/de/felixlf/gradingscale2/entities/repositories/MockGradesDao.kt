package de.felixlf.gradingscale2.entities.repositories

import arrow.core.None
import arrow.core.Option
import de.felixlf.gradingscale2.entities.daos.GradesDao
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class MockGradesDao(
    initialGradeScales: ImmutableList<GradeScale> = kotlinx.collections.immutable.persistentListOf(),
) : GradesDao {
    val gradeScales = MutableStateFlow(initialGradeScales)
    var success = true

    override fun getAllGradesFromGradeScale(gradeScaleId: String): Flow<List<Grade>> = gradeScales.map { scales ->
        scales.find { it.id == gradeScaleId }?.grades ?: emptyList()
    }

    override fun getGradeById(gradeId: String): Flow<Grade?> {
        return gradeScales.map { gradeScales ->
            gradeScales.flatMap { it.grades }
                .find { it.uuid == gradeId }
        }
    }

    override suspend fun upsertGrade(grade: Grade): Option<Unit> = when (success) {
        true -> Option.invoke(Unit)
        false -> None

    }

    override suspend fun deleteGrade(gradeId: String): Option<Unit> = when {
        success && gradeScales.value.flatMap { it.grades }.any { it.uuid == gradeId } -> Option.invoke(Unit)
        else -> None
    }
}
