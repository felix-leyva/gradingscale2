package de.felixlf.gradingscale2.entities.daos

import arrow.core.Option
import de.felixlf.gradingscale2.entities.models.Grade
import kotlinx.coroutines.flow.Flow

interface GradesDao {
    fun getAllGradesFromGradeScale(gradeScaleId: String): Flow<List<Grade>>
    fun getGradeById(gradeId: String): Flow<Grade?>
    suspend fun upsertGrade(grade: Grade): Option<Unit>
    suspend fun deleteGrade(gradeId: String): Option<Unit>
}
