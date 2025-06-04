package de.felixlf.gradingscale2.entities.repositories

import arrow.core.Option
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
    override fun getAllGradesFromGradeScale(gradeScaleId: String): SharedFlow<List<Grade>> {
        return gradesDao.getAllGradesFromGradeScale(gradeScaleId)
            .shareIn(scope = scope, started = SharingStarted.Lazily, replay = 1)
    }

    override fun getGradeById(gradeId: String): SharedFlow<Grade?> {
        return gradesDao.getGradeById(gradeId)
            .shareIn(scope = scope, started = SharingStarted.Lazily, replay = 1)
    }

    override suspend fun upsertGrade(grade: Grade): Option<Long> {
        return gradesDao.upsertGrade(grade)
    }

    override suspend fun deleteGrade(gradeId: String): Option<Unit> {
        return gradesDao.deleteGrade(gradeId)
    }
}
