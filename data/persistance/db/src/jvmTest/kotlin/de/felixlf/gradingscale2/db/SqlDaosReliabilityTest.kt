package de.felixlf.gradingscale2.db

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import de.felixlf.gradingscale2.Database
import de.felixlf.gradingscale2.DatabaseSchemaInitializer
import de.felixlf.gradingscale2.entities.daos.GradeScaleDaoImpl
import de.felixlf.gradingscale2.entities.daos.GradesDaoImpl
import de.felixlf.gradingscale2.entities.daos.weightedgrade.DbToWeightedGradeMapperImpl
import de.felixlf.gradingscale2.entities.daos.weightedgrade.WeightedGradeDaoImpl
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade
import de.felixlf.gradingscale2.entities.util.DispatcherProvider
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Properties
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SqlDaosReliabilityTest {

    private val testDispatchers = object : DispatcherProvider {
        override val main: CoroutineDispatcher = Dispatchers.Unconfined
        override val immediate: CoroutineDispatcher = Dispatchers.Unconfined
        override val io: CoroutineDispatcher = Dispatchers.IO
    }

    @Test
    fun deleteGradeScale_removes_grade_scale_and_its_grades() = runBlocking {
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).use { driver ->
            DatabaseSchemaInitializer(driver).initSchema()
            val db = Database(driver)
            val gradeScaleDao = GradeScaleDaoImpl(db.gradeScaleQueries, testDispatchers)
            val gradesDao = GradesDaoImpl(db.gradeScaleQueries, testDispatchers)

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
            val gradeScaleDao = GradeScaleDaoImpl(db.gradeScaleQueries, testDispatchers)

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
    fun initSchema_migrates_v1_database_adding_total_points_and_cascade_foreign_key() = runBlocking {
        val fkProps = Properties().apply { put("foreign_keys", "true") }
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY, fkProps).use { driver ->
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
            assertEquals(3L, Database.Schema.version)
            assertEquals(Database.Schema.version, migratedVersion)

            val db = Database(driver)
            val gradeScaleDao = GradeScaleDaoImpl(db.gradeScaleQueries, testDispatchers)
            val gradesDao = GradesDaoImpl(db.gradeScaleQueries, testDispatchers)
            val migratedScale = gradeScaleDao.getGradeScales().first().single()
            assertEquals("Legacy Scale", migratedScale.gradeScaleName)
            assertEquals(10.0, migratedScale.totalPoints)

            // Verify ON DELETE CASCADE works after v1 -> v3 migration
            db.gradeScaleQueries.deleteGradeScaleById("v1-scale")
            assertTrue(gradesDao.getAllGradesFromGradeScale("v1-scale").first().isEmpty())
        }
    }

    @Test
    fun sql_write_daos_return_None_instead_of_throwing_on_sqlite_errors() = runBlocking {
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).use { driver ->
            // Schema intentionally omitted so SQLite throws SQLITE_ERROR (no such table)
            val db = Database(driver)
            val gradeScaleDao = GradeScaleDaoImpl(db.gradeScaleQueries, testDispatchers)
            val gradesDao = GradesDaoImpl(db.gradeScaleQueries, testDispatchers)

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

    @Test
    fun empty_grade_scale_is_preserved_by_getGradeScales_and_getGradeScaleById_A3_04() = runBlocking {
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).use { driver ->
            DatabaseSchemaInitializer(driver).initSchema()
            val db = Database(driver)
            val gradeScaleDao = GradeScaleDaoImpl(db.gradeScaleQueries, testDispatchers)

            val emptyScale = GradeScale(
                id = "empty-scale",
                gradeScaleName = "Empty Scale",
                totalPoints = 25.0,
                grades = persistentListOf(),
            )
            assertTrue(gradeScaleDao.upsertGradeScale(emptyScale).isSome())

            val byId = gradeScaleDao.getGradeScaleById("empty-scale").first()
            assertNotNull(byId, "A3-04: Empty grade scale must not be dropped by getGradeScaleById")
            assertEquals("Empty Scale", byId.gradeScaleName)
            assertEquals(25.0, byId.totalPoints)
            assertTrue(byId.grades.isEmpty())

            val allScales = gradeScaleDao.getGradeScales().first()
            assertEquals(1, allScales.size, "A3-04: Empty grade scale must not be dropped by getGradeScales")
            assertEquals("empty-scale", allScales.single().id)
            assertTrue(allScales.single().grades.isEmpty())
        }
    }

    @Test
    fun upsertGradeScale_deletes_removed_grades_from_existing_scale_A3_05() = runBlocking {
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).use { driver ->
            DatabaseSchemaInitializer(driver).initSchema()
            val db = Database(driver)
            val gradeScaleDao = GradeScaleDaoImpl(db.gradeScaleQueries, testDispatchers)
            val gradesDao = GradesDaoImpl(db.gradeScaleQueries, testDispatchers)

            val g1 = Grade("A", 0.9, "scale-1", "Scale 1", "g-1")
            val g2 = Grade("B", 0.8, "scale-1", "Scale 1", "g-2")
            val g3 = Grade("C", 0.7, "scale-1", "Scale 1", "g-3")
            val initial = GradeScale("scale-1", "Scale 1", 10.0, persistentListOf(g1, g2, g3))
            assertTrue(gradeScaleDao.upsertGradeScale(initial).isSome())
            assertEquals(3, gradeScaleDao.getGradeScaleById("scale-1").first()?.grades?.size)

            val updated = initial.copy(grades = persistentListOf(g1, g2))
            assertTrue(gradeScaleDao.upsertGradeScale(updated).isSome())

            val persisted = gradeScaleDao.getGradeScaleById("scale-1").first()
            assertNotNull(persisted)
            assertEquals(listOf("g-1", "g-2"), persisted.grades.map { it.uuid }.sorted())
            assertEquals(listOf("g-1", "g-2"), gradesDao.getAllGradesFromGradeScale("scale-1").first().map { it.uuid }.sorted())
        }
    }

    @Test
    fun foreign_keys_and_cascade_protect_grades_and_update_does_not_cascade_wipe_A3_06() = runBlocking {
        val fkProps = Properties().apply { put("foreign_keys", "true") }
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY, fkProps).use { driver ->
            DatabaseSchemaInitializer(driver).initSchema()
            val db = Database(driver)
            val gradeScaleDao = GradeScaleDaoImpl(db.gradeScaleQueries, testDispatchers)
            val gradesDao = GradesDaoImpl(db.gradeScaleQueries, testDispatchers)

            // 1. Inserting an orphan grade for a non-existent scale_id must fail and return None
            val orphan = Grade("Orphan", 0.5, "missing-scale", "Missing", "orphan-uuid")
            assertTrue(gradesDao.upsertGrade(orphan).isNone(), "A3-06: Orphan grade insert must fail when foreign_keys=ON")

            // 2. Upserting a scale and then updating its name/totalPoints in-place must NOT cascade-delete child grades
            val g1 = Grade("1", 0.9, "scale-fk", "Original", "g-fk-1")
            val scale = GradeScale("scale-fk", "Original", 10.0, persistentListOf(g1))
            assertTrue(gradeScaleDao.upsertGradeScale(scale).isSome())

            // Direct scale row update without re-inserting grades must NOT cascade-delete child grades
            db.gradeScaleQueries.updateGradeScale("Renamed", 50.0, "scale-fk")
            val afterRename = gradeScaleDao.getGradeScaleById("scale-fk").first()
            assertNotNull(afterRename)
            assertEquals("Renamed", afterRename.gradeScaleName)
            assertEquals(50.0, afterRename.totalPoints)
            assertEquals(1, afterRename.grades.size)

            // 3. Raw delete on grade_scale must ON DELETE CASCADE child grades automatically
            db.gradeScaleQueries.deleteGradeScaleById("scale-fk")
            assertTrue(gradesDao.getAllGradesFromGradeScale("scale-fk").first().isEmpty(), "A3-06: Deleting grade_scale must cascade-delete child grades")
        }
    }

    @Test
    fun delete_methods_return_None_when_target_id_does_not_exist_A3_11() = runBlocking {
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).use { driver ->
            DatabaseSchemaInitializer(driver).initSchema()
            val db = Database(driver)
            val gradeScaleDao = GradeScaleDaoImpl(db.gradeScaleQueries, testDispatchers)
            val gradesDao = GradesDaoImpl(db.gradeScaleQueries, testDispatchers)
            val weightedDao = WeightedGradeDaoImpl(DbToWeightedGradeMapperImpl(), db.weightedGradeQueries, testDispatchers)

            assertTrue(gradeScaleDao.deleteGradeScale("missing-scale").isNone(), "A3-11: Deleting missing scale must return None")
            assertTrue(gradesDao.deleteGrade("missing-grade").isNone(), "A3-11: Deleting missing grade must return None")
            assertTrue(weightedDao.deleteWeightedGrade("missing-wg").isNone(), "A3-11: Deleting missing weighted grade must return None")
        }
    }

    @Test
    fun sqlite_write_operations_switch_to_io_dispatcher_A3_13() = runBlocking {
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).use { driver ->
            DatabaseSchemaInitializer(driver).initSchema()
            val db = Database(driver)
            val countingIo = CountingDispatcher(Dispatchers.IO)
            val provider = object : DispatcherProvider {
                override val main: CoroutineDispatcher = Dispatchers.Unconfined
                override val immediate: CoroutineDispatcher = Dispatchers.Unconfined
                override val io: CoroutineDispatcher = countingIo
            }
            val gradeScaleDao = GradeScaleDaoImpl(db.gradeScaleQueries, provider)
            val gradesDao = GradesDaoImpl(db.gradeScaleQueries, provider)
            val weightedDao = WeightedGradeDaoImpl(DbToWeightedGradeMapperImpl(), db.weightedGradeQueries, provider)

            val scale = GradeScale("s-io", "IO Scale", 10.0, persistentListOf())
            assertTrue(gradeScaleDao.upsertGradeScale(scale).isSome())
            assertEquals(1L, gradesDao.upsertGrade(Grade("A", 0.9, "s-io", "IO Scale", "g-io")).getOrNull())
            assertTrue(gradesDao.deleteGrade("g-io").isSome())
            assertTrue(gradeScaleDao.deleteGradeScale("s-io").isSome())

            val wg = WeightedGrade(0.9, 1.0, "wg-io-1")
            assertEquals(1L, weightedDao.upsertWeightedGrade(wg).getOrNull())
            assertTrue(weightedDao.deleteWeightedGrade("wg-io-1").isSome())
            assertTrue(countingIo.dispatchCount.get() >= 6, "A3-13: All 6 SQLite write operations must dispatch onto dispatcherProvider.io")
        }
    }

    private class CountingDispatcher(
        private val delegate: CoroutineDispatcher,
    ) : CoroutineDispatcher() {
        val dispatchCount = AtomicInteger(0)

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            dispatchCount.incrementAndGet()
            delegate.dispatch(context, block)
        }
    }
}
