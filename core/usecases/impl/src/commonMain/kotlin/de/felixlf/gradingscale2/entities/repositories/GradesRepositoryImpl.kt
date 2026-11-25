package de.felixlf.gradingscale2.entities.repositories

import arrow.core.Option
import de.felixlf.gradingscale2.entities.daos.GradesDao
import de.felixlf.gradingscale2.entities.models.Grade
import kotlinx.coroutines.flow.Flow

internal class GradesRepositoryImpl(
    private val gradesDao: GradesDao,
) : GradesRepository {
    override fun getAllGradesFromGradeScale(gradeScaleId: String): Flow<List<Grade>> =
        gradesDao.getAllGradesFromGradeScale(gradeScaleId)

    override fun getGradeById(gradeId: String): Flow<Grade?> =
        gradesDao.getGradeById(gradeId)

    override suspend fun upsertGrade(grade: Grade): Option<Long> {
        return gradesDao.upsertGrade(grade)
    }

    override suspend fun deleteGrade(gradeId: String): Option<Unit> {
        return gradesDao.deleteGrade(gradeId)
    }
}
