package de.felixlf.gradingscale2.entities.repositories

import de.felixlf.gradingscale2.entities.daos.GradesDao
import de.felixlf.gradingscale2.entities.models.Grade
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn

internal class GradesRepositoryImpl(
    private val gradesDao: GradesDao,
    private val scope: CoroutineScope = CoroutineScope(CoroutineName("GradesRepositoryImpl")),
) : GradesRepository {
    override fun getGradeById(gradeId: String): SharedFlow<Grade?> {
        return gradesDao.getGradeById(gradeId)
            .shareIn(scope = scope, started = SharingStarted.Lazily, replay = 1)
    }

    override suspend fun upsertGrade(grade: Grade): Result<Unit> {
        return gradesDao.upsertGrade(grade)
    }

    override suspend fun deleteGrade(gradeId: String): Result<Unit> {
        return gradesDao.deleteGrade(gradeId)
    }
}
