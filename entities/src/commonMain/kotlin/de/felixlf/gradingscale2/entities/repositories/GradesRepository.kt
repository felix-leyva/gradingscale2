package de.felixlf.gradingscale2.entities.repositories

import arrow.core.Option
import de.felixlf.gradingscale2.entities.models.Grade
import kotlinx.coroutines.flow.SharedFlow

internal interface GradesRepository {
    fun getAllGradesFromGradeScale(gradeScaleId: String): SharedFlow<List<Grade>>
    fun getGradeById(gradeId: String): SharedFlow<Grade?>
    suspend fun upsertGrade(grade: Grade): Option<Long>
    suspend fun deleteGrade(gradeId: String): Option<Unit>
}
