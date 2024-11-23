package de.felixlf.gradingscale2.entities.repositories

import de.felixlf.gradingscale2.entities.daos.GradesDao
import de.felixlf.gradingscale2.entities.models.Grade
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class GradesRepositoryImpl(
    private val gradesDao: GradesDao,
    private val scope: CoroutineScope = CoroutineScope(CoroutineName("GradesRepositoryImpl")),
) : GradesRepository {
    override fun getGradeById(gradeId: String): StateFlow<Grade?> {
        return gradesDao.getGradeById(gradeId)
            .stateIn(scope = scope, started = SharingStarted.Lazily, initialValue = null)
    }

    override suspend fun upsertGrade(
        grade: Grade,
        scaleId: String,
    ): Result<Unit> {
        return gradesDao.upsertGrade(grade, scaleId)
    }

    override suspend fun deleteGrade(gradeId: String): Result<Unit> {
        return gradesDao.deleteGrade(gradeId)
    }
}
