package de.felixlf.gradingscale2.entities.daos

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.cash.sqldelight.db.SqlDriver
import de.felixlf.gradingscale2.GradeScaleQueries
import de.felixlf.gradingscale2.entities.models.Grade
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

internal class GradesDaoImpl(
    private val gradeScaleQueries: GradeScaleQueries,
    private val driver: SqlDriver,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val mapper: GradeMapper = GradeMapper(),
) : GradesDao {
    override fun getGradeById(gradeId: String): Flow<Grade?> {
        return gradeScaleQueries.getGradeByUuid(gradeId, mapper::mapToGrade).asFlow()
            .mapToOneOrNull(dispatcher)
    }

    override suspend fun upsertGrade(grade: Grade): Result<Unit> = runCatching {
        gradeScaleQueries.upsertGrade(
            uuid = grade.uuid,
            named_grade = grade.namedGrade,
            percentage = grade.percentage,
            scale_id = grade.idOfGradeScale,
        )
        if (driver.execute(null, "SELECT changes() AS affected_rows", 0).await() == 0L) {
            throw Exception("Failed to upsert grade")
        }
    }

    override suspend fun deleteGrade(gradeId: String): Result<Unit> = runCatching {
        gradeScaleQueries.deleteGradesByUuid(gradeId)
        if (driver.execute(null, "SELECT changes() AS affected_rows", 0).await() == 0L) {
            throw Exception("Failed to delete grade")
        }
    }
}
