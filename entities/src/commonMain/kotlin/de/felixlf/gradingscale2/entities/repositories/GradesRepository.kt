package de.felixlf.gradingscale2.entities.repositories

import de.felixlf.gradingscale2.entities.models.Grade
import kotlinx.coroutines.flow.SharedFlow

internal interface GradesRepository {
    fun getGradeById(gradeId: String): SharedFlow<Grade?>
    suspend fun upsertGrade(grade: Grade, scaleId: String): Result<Unit>
    suspend fun deleteGrade(gradeId: String): Result<Unit>
}
