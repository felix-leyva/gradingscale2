package de.felixlf.gradingscale2.db

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import de.felixlf.gradingscale2.Database
import de.felixlf.gradingscale2.DatabaseSchemaInitializer
import de.felixlf.gradingscale2.entities.daos.GradeScaleDaoImpl
import de.felixlf.gradingscale2.entities.daos.GradesDaoImpl
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SqlDaosReliabilityTest {

    @Test
    fun deleteGradeScale_removes_grade_scale_and_its_grades() = runBlocking {
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).use { driver ->
            DatabaseSchemaInitializer(driver).initSchema()
            val db = Database(driver)
            val gradeScaleDao = GradeScaleDaoImpl(db.gradeScaleQueries, Dispatchers.IO)
            val gradesDao = GradesDaoImpl(db.gradeScaleQueries, Dispatchers.IO)

            val scale = GradeScale(
                id = "scale-1",
                gradeScaleName = "Test Scale",
                totalPoints = 100.0,
                grades = persistentListOf(
                    Grade("A", 0.9, "scale-1", "Test Scale", "grade-1"),
                    Grade("B", 0.8, "scale-1", "Test Scale", "grade-2"),
                ),
            )
            assertTrue(gradeScaleDao.upsertGradeScale(scale).isSome())
            assertEquals(1, gradeScaleDao.getGradeScales().first().size)
            assertEquals(2, gradesDao.getAllGradesFromGradeScale("scale-1").first().size)

            assertTrue(gradeScaleDao.deleteGradeScale("scale-1").isSome())

            assertTrue(gradeScaleDao.getGradeScales().first().isEmpty())
            assertTrue(gradesDao.getAllGradesFromGradeScale("scale-1").first().isEmpty())
        }
    }

    @Test
    fun upsertGradeScale_persists_and_reads_totalPoints() = runBlocking {
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).use { driver ->
            DatabaseSchemaInitializer(driver).initSchema()
            val db = Database(driver)
            val gradeScaleDao = GradeScaleDaoImpl(db.gradeScaleQueries, Dispatchers.IO)

            val scale = GradeScale(
                id = "scale-100",
                gradeScaleName = "100 Point Scale",
                totalPoints = 100.0,
                grades = persistentListOf(
                    Grade("A", 0.9, "scale-100", "100 Point Scale", "grade-100"),
                ),
            )
            assertTrue(gradeScaleDao.upsertGradeScale(scale).isSome())

            val allScales = gradeScaleDao.getGradeScales().first()
            assertEquals(100.0, allScales.single().totalPoints)
            assertEquals(100.0, gradeScaleDao.getGradeScaleById("scale-100").first()?.totalPoints)
        }
    }

    @Test
    fun initSchema_migrates_v1_database_adding_total_points_column_with_default_10() = runBlocking {
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).use { driver ->
            driver.execute(
                null,
                "CREATE TABLE grade_scale (id TEXT PRIMARY KEY NOT NULL, grade_scale_name TEXT NOT NULL)",
                0,
            ).await()
            driver.execute(
                null,
                """
                CREATE TABLE grade (
                    uuid TEXT PRIMARY KEY NOT NULL,
                    named_grade TEXT NOT NULL,
                    percentage REAL NOT NULL,
                    scale_id TEXT NOT NULL,
                    FOREIGN KEY (scale_id) REFERENCES grade_scale(id)
                )
                """.trimIndent(),
                0,
            ).await()
            driver.execute(
                null,
                "CREATE TABLE weighted_grade (id TEXT PRIMARY KEY NOT NULL, percentage REAL NOT NULL, weight REAL NOT NULL)",
                0,
            ).await()
            driver.execute(null, "INSERT INTO grade_scale (id, grade_scale_name) VALUES ('v1-scale', 'Legacy Scale')", 0).await()
            driver.execute(
                null,
                "INSERT INTO grade (uuid, named_grade, percentage, scale_id) VALUES ('v1-grade', '1.0', 0.95, 'v1-scale')",
                0,
            ).await()
            driver.execute(null, "PRAGMA user_version = 1", 0).await()

            DatabaseSchemaInitializer(driver).initSchema()

            val migratedVersion = driver.executeQuery(
                identifier = null,
                sql = "PRAGMA user_version",
                mapper = { cursor ->
                    when (val next = cursor.next()) {
                        is QueryResult.Value -> QueryResult.Value(if (next.value) cursor.getLong(0) ?: 0L else 0L)
                        is QueryResult.AsyncValue -> QueryResult.AsyncValue { if (next.await()) cursor.getLong(0) ?: 0L else 0L }
                    }
                },
                parameters = 0,
            ).await()
            assertEquals(2L, Database.Schema.version)
            assertEquals(Database.Schema.version, migratedVersion)

            val gradeScaleDao = GradeScaleDaoImpl(Database(driver).gradeScaleQueries, Dispatchers.IO)
            val migratedScale = gradeScaleDao.getGradeScales().first().single()
            assertEquals("Legacy Scale", migratedScale.gradeScaleName)
            assertEquals(10.0, migratedScale.totalPoints)
        }
    }

    @Test
    fun sql_write_daos_return_None_instead_of_throwing_on_sqlite_errors() = runBlocking {
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).use { driver ->
            // Schema intentionally omitted so SQLite throws SQLITE_ERROR (no such table)
            val db = Database(driver)
            val gradeScaleDao = GradeScaleDaoImpl(db.gradeScaleQueries, Dispatchers.IO)
            val gradesDao = GradesDaoImpl(db.gradeScaleQueries, Dispatchers.IO)

            val sampleGrade = Grade("A", 0.9, "scale-err", "Broken", "grade-err")
            val sampleScale = GradeScale(
                id = "scale-err",
                gradeScaleName = "Broken",
                totalPoints = 10.0,
                grades = persistentListOf(sampleGrade),
            )

            assertTrue(gradeScaleDao.upsertGradeScale(sampleScale).isNone())
            assertTrue(gradeScaleDao.deleteGradeScale("scale-err").isNone())
            assertTrue(gradesDao.upsertGrade(sampleGrade).isNone())
            assertTrue(gradesDao.deleteGrade("grade-err").isNone())
        }
    }
}
