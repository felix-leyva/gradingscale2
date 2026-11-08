@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2

import app.cash.sqldelight.db.SqlDriver
import de.felixlf.gradingscale2.db.DriverFactory
import de.felixlf.gradingscale2.entities.daos.GradeScaleDao
import de.felixlf.gradingscale2.entities.daos.GradeScaleDaoImpl
import de.felixlf.gradingscale2.entities.daos.GradesDao
import de.felixlf.gradingscale2.entities.daos.GradesDaoImpl
import de.felixlf.gradingscale2.entities.daos.WeightedGradeDao
import de.felixlf.gradingscale2.entities.daos.weightedgrade.DbToWeightedGradeMapper
import de.felixlf.gradingscale2.entities.daos.weightedgrade.DbToWeightedGradeMapperImpl
import de.felixlf.gradingscale2.entities.daos.weightedgrade.WeightedGradeDaoImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal expect fun getDbPlatformModule(): Module

class DatabaseSchemaInitializer(
    private val driver: SqlDriver,
) {
    suspend fun initSchema() {
        Database.Schema.create(driver).await()
    }
}

private fun provideSqlDriver(driverFactory: DriverFactory): SqlDriver = driverFactory.createDriver()

private fun provideGradeScaleQueries(database: Database): GradeScaleQueries = database.gradeScaleQueries

private fun provideWeightedGradeQueries(database: Database): WeightedGradeQueries = database.weightedGradeQueries

/**
 * The default implementation of the SQL DAOs.
 * In JS we cannot use SQLDelight's, so our DAOs are implemented using KStore.
 */
internal val sqlDaoModule = module {
    single { DatabaseSchemaInitializer(get()) }
    single { Database(get()) }
    singleOf(::provideSqlDriver)
    singleOf(::provideGradeScaleQueries)
    single<GradeScaleDao> { GradeScaleDaoImpl(get()) }
    single<GradesDao> { GradesDaoImpl(gradeScaleQueries = get()) }

    singleOf(::provideWeightedGradeQueries)
    singleOf(::WeightedGradeDaoImpl).bind<WeightedGradeDao>()
    singleOf(::DbToWeightedGradeMapperImpl).bind<DbToWeightedGradeMapper>()
}

val dbModule = module {
    includes(getDbPlatformModule())
}
