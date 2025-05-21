package de.felixlf.gradingscale2.entities.repositories

import arrow.core.Option
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade
import kotlinx.coroutines.flow.Flow

interface WeightedGradesRepository {
    fun getAllWeightedGrades(): Flow<List<WeightedGrade>>
    suspend fun upsertWeightedGrade(weightedGrade: WeightedGrade): Option<Long>
    suspend fun deleteWeightedGrade(weightedGradeId: String): Option<Unit>
}
