package de.felixlf.gradingscale2.entities.daos

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import arrow.core.Option
import de.felixlf.gradingscale2.GradeScaleQueries
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.util.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

internal class GradesDaoImpl(
    private val gradeScaleQueries: GradeScaleQueries,
    private val dispatcherProvider: DispatcherProvider,
    private val mapper: GradeMapper = GradeMapper(),
) : GradesDao {
    override fun getAllGradesFromGradeScale(gradeScaleId: String): Flow<List<Grade>> {
        return gradeScaleQueries.getAllGradesByGradeScaleId(gradeScaleId, mapper::mapToGrade).asFlow()
            .mapToList(dispatcherProvider.io)
            .flowOn(dispatcherProvider.io)
    }

    override fun getGradeById(gradeId: String): Flow<Grade?> {
        return gradeScaleQueries.getGradeByUuid(gradeId, mapper::mapToGrade).asFlow()
            .mapToOneOrNull(dispatcherProvider.io)
    }

    override suspend fun upsertGrade(grade: Grade): Option<Long> =
        withContext(dispatcherProvider.io) {
            Option.catch {
                gradeScaleQueries.upsertGrade(
                    uuid = grade.uuid,
                    named_grade = grade.namedGrade,
                    percentage = grade.percentage,
                    scale_id = grade.idOfGradeScale,
                )
            }
        }

    override suspend fun deleteGrade(gradeId: String): Option<Unit> =
        withContext(dispatcherProvider.io) {
            Option.catch {
                gradeScaleQueries.deleteGradesByUuid(gradeId)
            }.filter { deletedRows -> deletedRows > 0L }.map {}
        }
}
