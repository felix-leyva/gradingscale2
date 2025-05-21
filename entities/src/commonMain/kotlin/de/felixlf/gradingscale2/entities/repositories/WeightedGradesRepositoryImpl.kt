package de.felixlf.gradingscale2.entities.repositories

import arrow.core.Option
import de.felixlf.gradingscale2.entities.daos.WeightedGradeDao
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade
import kotlinx.coroutines.flow.Flow

class WeightedGradesRepositoryImpl(private val weightedGradeDao: WeightedGradeDao) : WeightedGradesRepository {
    override fun getAllWeightedGrades(): Flow<List<WeightedGrade>> = weightedGradeDao.getAllWeightedGrades()

    override suspend fun upsertWeightedGrade(weightedGrade: WeightedGrade): Option<Long> =
        weightedGradeDao.upsertWeightedGrade(weightedGrade)

    override suspend fun deleteWeightedGrade(weightedGradeId: String): Option<Unit> = weightedGradeDao.deleteWeightedGrade(weightedGradeId)
}
