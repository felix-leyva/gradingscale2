package de.felixlf.gradingscale2.entities.daos

import arrow.core.Option
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade
import kotlinx.coroutines.flow.Flow

interface WeightedGradeDao {
    fun getAllWeightedGrades(): Flow<List<WeightedGrade>>
    suspend fun upsertWeightedGrade(weightedGrade: WeightedGrade): Option<Unit>
    suspend fun deleteWeightedGrade(weightedGradeId: String): Option<Unit>
}
