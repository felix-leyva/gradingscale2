package de.felixlf.gradingscale2.entities.daos

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import arrow.core.Option
import arrow.core.raise.option
import de.felixlf.gradingscale2.GradeScaleQueries
import de.felixlf.gradingscale2.entities.models.GradeScale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class GradeScaleDaoImpl(
    private val gradeScaleQueries: GradeScaleQueries,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val mapper: JoinGradeScaleMapper = JoinGradeScaleMapper(),
) : GradeScaleDao {
    override fun getGradeScaleById(id: String): Flow<GradeScale?> =
        gradeScaleQueries
            .getGradeScaleWithGrades(
                id = id,
                mapper = mapper::mapToJoinedGradeScaleWithGradeDao,
            ).asFlow()
            .mapToList(dispatcher)
            .map { mapper.mapToGradeScale(it).firstOrNull() }

    override fun getGradeScales(): Flow<ImmutableList<GradeScale>> =
        gradeScaleQueries
            .getAllGradeScalesWithGrades(mapper::mapToJoinedGradeScaleWithGradeDao)
            .asFlow()
            .mapToList(dispatcher)
            .map(mapper::mapToGradeScale)

    override suspend fun upsertGradeScale(gradeScale: GradeScale): Option<Unit> =
        option {
            gradeScaleQueries.transactionWithResult {
                gradeScaleQueries.upsertGradeScale(
                    id = gradeScale.id,
                    grade_scale_name = gradeScale.gradeScaleName,
                )
                gradeScale.sortedPointedGrades.forEach { grade ->
                    gradeScaleQueries.upsertGrade(
                        uuid = grade.uuid,
                        named_grade = grade.namedGrade,
                        percentage = grade.percentage,
                        scale_id = gradeScale.id,
                    )
                }
            }
        }

    override suspend fun deleteGradeScale(gradeScaleId: String): Option<Unit> =
        option {
            gradeScaleQueries.deleteGradesByUuid(gradeScaleId)
        }
}
