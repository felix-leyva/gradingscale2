package de.felixlf.gradingscale2.entities.repositories

import de.felixlf.gradingscale2.entities.daos.GradeScaleDao
import de.felixlf.gradingscale2.entities.models.GradeScale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn

internal class GradeScaleRepositoryImpl(
    private val gradeScaleDao: GradeScaleDao,
    private val scope: CoroutineScope = CoroutineScope(CoroutineName("GradeScaleRepositoryImpl")),
) : GradeScaleRepository {

    override fun getGradeScaleById(id: String): SharedFlow<GradeScale?> =
        gradeScaleDao.getGradeScaleById(id)
            .shareIn(scope = scope, started = SharingStarted.Lazily, replay = 1)

    override fun getGradeScales(): SharedFlow<ImmutableList<GradeScale>> =
        gradeScaleDao.getGradeScales().shareIn(
            scope = scope,
            started = SharingStarted.Lazily,
            replay = 1,
        )

    override suspend fun upsertGradeScale(gradeScale: GradeScale): Result<String> = runCatching {
        gradeScaleDao.upsertGradeScale(gradeScale).getOrThrow()
        gradeScale.id
    }
    override suspend fun deleteGradeScale(gradeScaleId: String): Result<Unit> =
        gradeScaleDao.deleteGradeScale(gradeScaleId)
}
