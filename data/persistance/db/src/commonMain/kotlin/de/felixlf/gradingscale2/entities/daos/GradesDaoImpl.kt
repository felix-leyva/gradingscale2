package de.felixlf.gradingscale2.entities.daos

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.cash.sqldelight.db.SqlDriver
import arrow.core.Option
import arrow.core.raise.option
import de.felixlf.gradingscale2.GradeScaleQueries
import de.felixlf.gradingscale2.entities.models.Grade
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

internal class GradesDaoImpl(
    private val gradeScaleQueries: GradeScaleQueries,
    private val driver: SqlDriver,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val mapper: GradeMapper = GradeMapper(),
) : GradesDao {
    override fun getAllGradesFromGradeScale(gradeScaleId: String): Flow<List<Grade>> {
        return gradeScaleQueries.getAllGradesByGradeScaleId(gradeScaleId, mapper::mapToGrade).asFlow()
            .mapToList(dispatcher)
            .flowOn(dispatcher)
    }

    override fun getGradeById(gradeId: String): Flow<Grade?> {
        return gradeScaleQueries.getGradeByUuid(gradeId, mapper::mapToGrade).asFlow()
            .mapToOneOrNull(dispatcher)
    }

    override suspend fun upsertGrade(grade: Grade): Option<Unit> = option {
        gradeScaleQueries.upsertGrade(
            uuid = grade.uuid,
            named_grade = grade.namedGrade,
            percentage = grade.percentage,
            scale_id = grade.idOfGradeScale,
        )
        //        TODO: Select changes() is not being supported anymore
        //        ensure(driver.execute(null, "SELECT changes() AS affected_rows", 0).await() == 0L)
    }

    override suspend fun deleteGrade(gradeId: String): Option<Unit> = option {
        gradeScaleQueries.deleteGradesByUuid(gradeId)
//        ensure(driver.execute(null, "SELECT changes() AS affected_rows", 0).await() == 0L)
    }
}
