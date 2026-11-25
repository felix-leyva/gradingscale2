package de.felixlf.gradingscale2.db

import app.cash.sqldelight.db.QueryResult
import de.felixlf.gradingscale2.Database
import de.felixlf.gradingscale2.DatabaseSchemaInitializer
import de.felixlf.gradingscale2.entities.daos.GradeScaleDaoImpl
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
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JVMDriverFactoryPersistenceTest {

    private lateinit var tempDir: Path
    private lateinit var dbDir: Path
    private var previousDbDir: String? = null

    private val testDispatchers = object : DispatcherProvider {
        override val main: CoroutineDispatcher = Dispatchers.Unconfined
        override val immediate: CoroutineDispatcher = Dispatchers.Unconfined
        override val io: CoroutineDispatcher = Dispatchers.IO
    }

    @BeforeTest
    fun setUp() {
        previousDbDir = System.getProperty("gradingscale.db.dir")
        tempDir = Files.createTempDirectory("gradingscale-db-test")
        dbDir = tempDir.resolve("nested dir with spaces/db")
        System.setProperty("gradingscale.db.dir", dbDir.toAbsolutePath().toString())
    }

    @AfterTest
    fun tearDown() {
        val prev = previousDbDir
        if (prev == null) {
            System.clearProperty("gradingscale.db.dir")
        } else {
            System.setProperty("gradingscale.db.dir", prev)
        }
        tempDir.toFile().deleteRecursively()
    }

    @Test
    fun persistsGradeScalesAndWeightedGradesAcrossDriverRestart() = runBlocking {
        val factory = JVMDriverFactory()
        val expectedScale = GradeScale(
            id = "scale-1",
            gradeScaleName = "German Scale",
            totalPoints = 10.0,
            grades = persistentListOf(
                Grade(
                    namedGrade = "1.0",
                    percentage = 0.95,
                    idOfGradeScale = "scale-1",
                    nameOfScale = "German Scale",
                    uuid = "grade-1",
                ),
            ),
        )
        val expectedWeightedGrade = WeightedGrade(
            percentage = 0.9,
            weight = 2.0,
            uuid = "wg-1",
        )

        factory.createDriver().use { driver1 ->
            DatabaseSchemaInitializer(driver1).initSchema()
            val db1 = Database(driver1)
            val gradeScaleDao1 = GradeScaleDaoImpl(db1.gradeScaleQueries, testDispatchers)
            val weightedDao1 = WeightedGradeDaoImpl(
                dbToWeightedGradeMapper = DbToWeightedGradeMapperImpl(),
                queries = db1.weightedGradeQueries,
                dispatcherProvider = testDispatchers,
            )

            assertTrue(gradeScaleDao1.upsertGradeScale(expectedScale).isSome())
            assertTrue(weightedDao1.upsertWeightedGrade(expectedWeightedGrade).isSome())
        }

        val (persistedScales, persistedWeighted) = factory.createDriver().use { driver2 ->
            DatabaseSchemaInitializer(driver2).initSchema()
            val userVersion = driver2.executeQuery(
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
            assertEquals(Database.Schema.version, userVersion)

            val db2 = Database(driver2)
            val gradeScaleDao2 = GradeScaleDaoImpl(db2.gradeScaleQueries, testDispatchers)
            val weightedDao2 = WeightedGradeDaoImpl(
                dbToWeightedGradeMapper = DbToWeightedGradeMapperImpl(),
                queries = db2.weightedGradeQueries,
                dispatcherProvider = testDispatchers,
            )

            gradeScaleDao2.getGradeScales().first() to weightedDao2.getAllWeightedGrades().first()
        }

        assertTrue(dbDir.resolve("gradingscale.db").toFile().exists())
        assertEquals(1, persistedScales.size)
        assertEquals("German Scale", persistedScales.first().gradeScaleName)
        assertEquals("1.0", persistedScales.first().grades.first().namedGrade)
        assertEquals(listOf(expectedWeightedGrade), persistedWeighted)
    }

    @Test
    fun initSchemaIsIdempotentOnExistingDatabase() = runBlocking {
        val factory = JVMDriverFactory()
        val expectedWeightedGrade = WeightedGrade(
            percentage = 0.8,
            weight = 1.5,
            uuid = "wg-persist",
        )

        factory.createDriver().use { driver1 ->
            val initializer = DatabaseSchemaInitializer(driver1)
            initializer.initSchema()
            initializer.initSchema()

            val db1 = Database(driver1)
            val weightedDao1 = WeightedGradeDaoImpl(
                dbToWeightedGradeMapper = DbToWeightedGradeMapperImpl(),
                queries = db1.weightedGradeQueries,
                dispatcherProvider = testDispatchers,
            )
            assertTrue(weightedDao1.upsertWeightedGrade(expectedWeightedGrade).isSome())
        }

        val loaded = factory.createDriver().use { driver2 ->
            DatabaseSchemaInitializer(driver2).initSchema()
            val db2 = Database(driver2)
            val weightedDao2 = WeightedGradeDaoImpl(
                dbToWeightedGradeMapper = DbToWeightedGradeMapperImpl(),
                queries = db2.weightedGradeQueries,
                dispatcherProvider = testDispatchers,
            )
            weightedDao2.getAllWeightedGrades().first()
        }

        assertEquals(listOf(expectedWeightedGrade), loaded)
    }

    @Test
    fun jvmDriverFactory_enables_foreign_key_constraints_by_default_A3_06() = runBlocking {
        val factory = JVMDriverFactory()
        factory.createDriver().use { driver ->
            val fkEnabled = driver.executeQuery(
                identifier = null,
                sql = "PRAGMA foreign_keys",
                mapper = { cursor ->
                    when (val next = cursor.next()) {
                        is QueryResult.Value -> QueryResult.Value(if (next.value) cursor.getLong(0) ?: 0L else 0L)
                        is QueryResult.AsyncValue -> QueryResult.AsyncValue { if (next.await()) cursor.getLong(0) ?: 0L else 0L }
                    }
                },
                parameters = 0,
            ).await()
            assertEquals(1L, fkEnabled, "A3-06: JVMDriverFactory must enable PRAGMA foreign_keys = ON")
        }
    }
}
