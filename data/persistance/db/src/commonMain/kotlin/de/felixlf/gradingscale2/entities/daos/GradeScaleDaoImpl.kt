package de.felixlf.gradingscale2.entities.daos

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import arrow.core.Option
import de.felixlf.gradingscale2.GradeScaleQueries
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.util.DispatcherProvider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class GradeScaleDaoImpl(
    private val gradeScaleQueries: GradeScaleQueries,
    private val dispatcherProvider: DispatcherProvider,
    private val mapper: JoinGradeScaleMapper = JoinGradeScaleMapper(),
) : GradeScaleDao {
    override fun getGradeScaleById(id: String): Flow<GradeScale?> =
        gradeScaleQueries
            .getGradeScaleWithGrades(
                id = id,
                mapper = mapper::mapToJoinedGradeScaleWithGradeDao,
            ).asFlow()
            .mapToList(dispatcherProvider.io)
            .map { mapper.mapToGradeScale(it).firstOrNull() }

    override fun getGradeScales(): Flow<ImmutableList<GradeScale>> =
        gradeScaleQueries
            .getAllGradeScalesWithGrades(mapper::mapToJoinedGradeScaleWithGradeDao)
            .asFlow()
            .mapToList(dispatcherProvider.io)
            .map(mapper::mapToGradeScale)

    override suspend fun upsertGradeScale(gradeScale: GradeScale): Option<Unit> =
        withContext(dispatcherProvider.io) {
            Option.catch {
                gradeScaleQueries.transactionWithResult {
                    val updatedRows = gradeScaleQueries.updateGradeScale(
                        grade_scale_name = gradeScale.gradeScaleName,
                        total_points = gradeScale.totalPoints,
                        id = gradeScale.id,
                    )
                    if (updatedRows == 0L) {
                        gradeScaleQueries.insertGradeScale(
                            id = gradeScale.id,
                            grade_scale_name = gradeScale.gradeScaleName,
                            total_points = gradeScale.totalPoints,
                        )
                    }
                    val keptUuids = gradeScale.sortedPointedGrades.map { it.uuid }
                    if (keptUuids.isEmpty()) {
                        gradeScaleQueries.deleteGradesByScaleId(gradeScale.id)
                    } else {
                        gradeScaleQueries.deleteGradesByScaleIdExcludingUuids(
                            scale_id = gradeScale.id,
                            uuid = keptUuids,
                        )
                    }
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
        }

    override suspend fun deleteGradeScale(gradeScaleId: String): Option<Unit> =
        withContext(dispatcherProvider.io) {
            Option.catch {
                gradeScaleQueries.transactionWithResult {
                    gradeScaleQueries.deleteGradesByScaleId(gradeScaleId)
                    gradeScaleQueries.deleteGradeScaleById(gradeScaleId)
                }
            }.filter { deletedRows -> deletedRows > 0L }.map {}
        }
}
