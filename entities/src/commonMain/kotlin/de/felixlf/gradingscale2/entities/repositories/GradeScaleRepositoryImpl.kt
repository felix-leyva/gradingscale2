package de.felixlf.gradingscale2.entities.repositories

import de.felixlf.gradingscale2.entities.daos.GradeScaleDao
import de.felixlf.gradingscale2.entities.models.GradeScale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class GradeScaleRepositoryImpl(
    private val gradeScaleDao: GradeScaleDao,
    private val scope: CoroutineScope = CoroutineScope(CoroutineName("GradeScaleRepositoryImpl")),
) : GradeScaleRepository {

    override fun getGradeScaleById(id: String): StateFlow<GradeScale?> =
        gradeScaleDao.getGradeScaleById(id)
            .stateIn(scope = scope, started = SharingStarted.Lazily, initialValue = null)

    override fun getGradeScales(): StateFlow<ImmutableList<GradeScale>> =
        gradeScaleDao.getGradeScales().stateIn(
            scope = scope, started = SharingStarted.Lazily, initialValue = persistentListOf()
        )

    override suspend fun upsertGradeScale(gradeScale: GradeScale): Result<Unit> =
        gradeScaleDao.upsertGradeScale(gradeScale)

    override suspend fun deleteGradeScale(gradeScaleId: String): Result<Unit> =
        gradeScaleDao.deleteGradeScale(gradeScaleId)
}
