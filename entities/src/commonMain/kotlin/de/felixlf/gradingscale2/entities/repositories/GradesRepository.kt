package de.felixlf.gradingscale2.entities.repositories

import de.felixlf.gradingscale2.entities.models.Grade
import kotlinx.coroutines.flow.StateFlow

interface GradesRepository {
    fun getGradeById(gradeId: String): StateFlow<Grade?>
    suspend fun upsertGrade(grade: Grade, scaleId: String): Result<Unit>
    suspend fun deleteGrade(gradeId: String): Result<Unit>
}
