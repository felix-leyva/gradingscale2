package de.felixlf.gradingscale2.entities.daos

import de.felixlf.gradingscale2.entities.models.Grade
import kotlinx.coroutines.flow.Flow

interface GradesDao {
    fun getGradeById(gradeId: String): Flow<Grade?>
    suspend fun upsertGrade(grade: Grade): Result<Unit>
    suspend fun deleteGrade(gradeId: String): Result<Unit>
}
