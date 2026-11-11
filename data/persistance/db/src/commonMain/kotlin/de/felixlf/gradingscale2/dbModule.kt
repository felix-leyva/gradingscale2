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
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create
import org.koin.plugin.module.dsl.single

internal expect fun getDbPlatformModule(): Module

class DatabaseSchemaInitializer(
    private val driver: SqlDriver,
) {
    suspend fun initSchema() {
        Database.Schema.create(driver).await()
    }
}

private fun provideDatabase(driver: SqlDriver): Database = Database(driver)

private fun provideSqlDriver(driverFactory: DriverFactory): SqlDriver = driverFactory.createDriver()

private fun provideGradeScaleQueries(database: Database): GradeScaleQueries = database.gradeScaleQueries

private fun provideWeightedGradeQueries(database: Database): WeightedGradeQueries = database.weightedGradeQueries

/**
 * The default implementation of the SQL DAOs.
 * In JS we cannot use SQLDelight's, so our DAOs are implemented using KStore.
 */
internal val sqlDaoModule = module {
    single<DatabaseSchemaInitializer>()
    single { create(::provideDatabase) }
    single { create(::provideSqlDriver) }
    single { create(::provideGradeScaleQueries) }
    single<GradeScaleDaoImpl>() bind GradeScaleDao::class
    single<GradesDaoImpl>() bind GradesDao::class

    single { create(::provideWeightedGradeQueries) }
    single<WeightedGradeDaoImpl>() bind WeightedGradeDao::class
    single<DbToWeightedGradeMapperImpl>() bind DbToWeightedGradeMapper::class
}

val dbModule = module {
    includes(getDbPlatformModule())
}
