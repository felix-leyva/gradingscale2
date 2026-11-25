package de.felixlf.gradingscale2.daosimpl

import arrow.core.Option
import de.felixlf.gradingscale2.entities.daos.WeightedGradeDao
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade
import de.felixlf.gradingscale2.store.WeightedGradesStore
import kotlinx.coroutines.flow.Flow

class WeightedGradeDaoStoreImpl(
    private val weightedGradesStore: WeightedGradesStore,
) : WeightedGradeDao {
    override fun getAllWeightedGrades(): Flow<List<WeightedGrade>> {
        return weightedGradesStore.getAllWeightedGrades()
    }

    override suspend fun upsertWeightedGrade(weightedGrade: WeightedGrade): Option<Long> =
        Option.catch {
            weightedGradesStore.upsertWeightedGrade(weightedGrade)
            1L
        }

    override suspend fun deleteWeightedGrade(weightedGradeId: String): Option<Unit> =
        Option.catch {
            weightedGradesStore.deleteWeightedGrade(weightedGradeId)
        }.filter { deleted -> deleted }.map {}
}
