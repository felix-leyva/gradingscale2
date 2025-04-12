package de.felixlf.gradingscale2.entities.daos.weightedgrade

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import arrow.core.Option
import de.felixlf.gradingscale2.WeightedGradeQueries
import de.felixlf.gradingscale2.entities.daos.WeightedGradeDao
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade
import de.felixlf.gradingscale2.entities.util.DispatcherProvider
import kotlinx.coroutines.flow.Flow

internal class WeightedGradeDaoImpl(
    private val dbToWeightedGradeMapper: DbToWeightedGradeMapper,
    private val queries: WeightedGradeQueries,
    private val dispatcherProvider: DispatcherProvider,
) : WeightedGradeDao {
    override fun getAllWeightedGrades(): Flow<List<WeightedGrade>> {
        return queries.getAllWeightedGrades(dbToWeightedGradeMapper::invoke)
            .asFlow()
            .mapToList(dispatcherProvider.io)
    }

    override suspend fun upsertWeightedGrade(weightedGrade: WeightedGrade) = Option.catch {
        queries.transactionWithResult {
            queries.upsertWeightedGrade(
                id = weightedGrade.uuid,
                percentage = weightedGrade.percentage,
                weight = weightedGrade.weight,
            )
        }
    }

    override suspend fun deleteWeightedGrade(weightedGradeId: String): Option<Unit> = Option.catch {
        queries.transactionWithResult {
            queries.deleteWeightedGradeById(weightedGradeId)
        }
    }
}
