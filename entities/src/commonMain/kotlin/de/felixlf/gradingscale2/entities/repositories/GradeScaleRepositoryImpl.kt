package de.felixlf.gradingscale2.entities.repositories

import de.felixlf.gradingscale2.entities.daos.GradeScaleDao
import de.felixlf.gradingscale2.entities.models.GradeScale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

class GradeScaleRepositoryImpl(
    private val gradeScaleDao: GradeScaleDao,
) : GradeScaleRepository {
    override fun getGradeScaleById(id: String): Flow<GradeScale?> = gradeScaleDao.getGradeScaleById(id)

    override fun getGradeScales(): Flow<ImmutableList<GradeScale>> = gradeScaleDao.getGradeScales()

    override suspend fun upsertGradeScale(gradeScale: GradeScale): Result<Unit> = gradeScaleDao.upsertGradeScale(gradeScale)

    override suspend fun deleteGradeScale(gradeScaleId: String): Result<Unit> = gradeScaleDao.deleteGradeScale(gradeScaleId)
}
