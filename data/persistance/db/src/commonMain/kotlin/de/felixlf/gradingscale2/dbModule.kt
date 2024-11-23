package de.felixlf.gradingscale2

import app.cash.sqldelight.db.SqlDriver
import de.felixlf.gradingscale2.entities.daos.GradeScaleDao
import de.felixlf.gradingscale2.entities.daos.GradeScaleDaoImpl
import org.koin.core.module.Module
import org.koin.dsl.module

internal expect fun getDbPlatformModule(): Module

class DatabaseSchemaInitializer(
    private val driver: SqlDriver,
) {
    suspend fun initSchema() {
        Database.Schema.create(driver).await()
    }
}

val dbModule =
    module {
        includes(getDbPlatformModule())
        single { DatabaseSchemaInitializer(get()) }
        single { Database(get()) }
        single<GradeScaleQueries> { get<Database>().gradeScaleQueries }
        single<GradeScaleDao> { GradeScaleDaoImpl(get()) }
    }
