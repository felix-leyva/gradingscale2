package de.felixlf.gradingscale2.daosimpl

import arrow.core.some
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade
import de.felixlf.gradingscale2.entities.util.DispatcherProvider
import de.felixlf.gradingscale2.store.GradeScaleStoreProvider
import de.felixlf.gradingscale2.store.GradeScalesStoreData
import de.felixlf.gradingscale2.store.WeightedGradesStoreData
import de.felixlf.gradingscale2.store.WeightedGradesStoreProvider
import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.storeOf
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WebDaosReliabilityTest {

    private val testDispatchers = object : DispatcherProvider {
        override val main: CoroutineDispatcher = Dispatchers.Unconfined
        override val immediate: CoroutineDispatcher = Dispatchers.Unconfined
        override val io: CoroutineDispatcher = Dispatchers.Unconfined
    }

    private class CountingInMemoryCodec<T : Any>(
        var current: T? = null,
        var failEncode: Boolean = false,
    ) : Codec<T> {
        var encodeCalls = 0

        override suspend fun encode(value: T?) {
            if (failEncode) throw RuntimeException("QuotaExceededError")
            encodeCalls++
            current = value
        }

        override suspend fun decode(): T? = current
    }

    @Test
    fun gradesDaoStoreImpl_deduplicates_uuid_across_scales_syncs_nameOfScale_and_returns_None_for_missing_scale_A3_07() = runTest {
        val scaleA = GradeScale(
            id = "scale-a",
            gradeScaleName = "Scale A",
            totalPoints = 10.0,
            grades = persistentListOf(
                Grade(namedGrade = "A", percentage = 0.9, idOfGradeScale = "scale-a", nameOfScale = "Scale A", uuid = "g-1"),
            ),
        )
        val scaleB = GradeScale(
            id = "scale-b",
            gradeScaleName = "Scale B",
            totalPoints = 20.0,
            grades = persistentListOf(),
        )
        val codec = CountingInMemoryCodec(GradeScalesStoreData(persistentListOf(scaleA, scaleB)))
        val store = storeOf(default = GradeScalesStoreData(persistentListOf()), codec = codec)
        val provider = GradeScaleStoreProvider(store, testDispatchers)
        val gradesDao = GradesDaoStoreImpl(provider)

        // 1. Moving g-1 from scale-a to scale-b with stale nameOfScale must remove it from scale-a and sync nameOfScale to "Scale B"
        val moved = Grade(namedGrade = "A+", percentage = 0.95, idOfGradeScale = "scale-b", nameOfScale = "Stale Name", uuid = "g-1")
        assertEquals(1L.some(), gradesDao.upsertGrade(moved))

        assertTrue(gradesDao.getAllGradesFromGradeScale("scale-a").first().isEmpty(), "A3-07: Old scale must not keep duplicate uuid")
        val scaleBGrades = gradesDao.getAllGradesFromGradeScale("scale-b").first()
        assertEquals(1, scaleBGrades.size)
        assertEquals("Scale B", scaleBGrades.single().nameOfScale, "A3-07: Grade.nameOfScale must be synced to target scale name")
        assertEquals("scale-b", gradesDao.getGradeById("g-1").first()?.idOfGradeScale)

        // 2. Upserting a grade for a non-existent scale must return None and not write to storage
        val encodeCountBeforeMissing = codec.encodeCalls
        val orphan = Grade(namedGrade = "X", percentage = 0.5, idOfGradeScale = "nonexistent-scale", nameOfScale = "", uuid = "g-orphan")
        assertTrue(gradesDao.upsertGrade(orphan).isNone(), "A3-07: Upserting into missing scale must return None")
        assertEquals(encodeCountBeforeMissing, codec.encodeCalls, "A3-11: Missing scale upsert must not write to storage")
    }

    @Test
    fun web_write_daos_return_None_instead_of_throwing_when_storage_encode_throws_A3_08() = runTest {
        val scaleCodec = CountingInMemoryCodec(
            current = GradeScalesStoreData(
                persistentListOf(GradeScale("s-1", "Scale 1", 10.0, persistentListOf(Grade("A", 0.9, "s-1", "Scale 1", "g-1")))),
            ),
            failEncode = true,
        )
        val scaleStore = storeOf(default = GradeScalesStoreData(persistentListOf()), codec = scaleCodec)
        val scaleProvider = GradeScaleStoreProvider(scaleStore, testDispatchers)
        val scaleDao = GradeScaleDaoStoreImpl(scaleProvider)
        val gradesDao = GradesDaoStoreImpl(scaleProvider)

        val wgCodec = CountingInMemoryCodec(
            current = WeightedGradesStoreData(persistentListOf(WeightedGrade(0.9, 1.0, "wg-1"))),
            failEncode = true,
        )
        val wgStore = storeOf(default = WeightedGradesStoreData(persistentListOf()), codec = wgCodec)
        val wgDao = WeightedGradeDaoStoreImpl(WeightedGradesStoreProvider(wgStore, testDispatchers))

        assertTrue(scaleDao.upsertGradeScale(GradeScale("s-2", "Scale 2", 10.0, persistentListOf())).isNone())
        assertTrue(scaleDao.deleteGradeScale("s-1").isNone())
        assertTrue(gradesDao.upsertGrade(Grade("B", 0.8, "s-1", "Scale 1", "g-2")).isNone())
        assertTrue(gradesDao.deleteGrade("g-1").isNone())
        assertTrue(wgDao.upsertWeightedGrade(WeightedGrade(0.8, 1.0, "wg-2")).isNone())
        assertTrue(wgDao.deleteWeightedGrade("wg-1").isNone())
    }

    @Test
    fun web_daos_match_return_value_parity_and_skip_storage_writes_on_noop_deletes_A3_11() = runTest {
        val scaleCodec = CountingInMemoryCodec(GradeScalesStoreData(persistentListOf()))
        val scaleStore = storeOf(default = GradeScalesStoreData(persistentListOf()), codec = scaleCodec)
        val scaleProvider = GradeScaleStoreProvider(scaleStore, testDispatchers)
        val scaleDao = GradeScaleDaoStoreImpl(scaleProvider)
        val gradesDao = GradesDaoStoreImpl(scaleProvider)

        val wgCodec = CountingInMemoryCodec(WeightedGradesStoreData(persistentListOf()))
        val wgStore = storeOf(default = WeightedGradesStoreData(persistentListOf()), codec = wgCodec)
        val wgDao = WeightedGradeDaoStoreImpl(WeightedGradesStoreProvider(wgStore, testDispatchers))

        // 1. WeightedGradeDaoStoreImpl.upsertWeightedGrade must return Some(1L) (matching SQLite DAO)
        assertEquals(1L.some(), wgDao.upsertWeightedGrade(WeightedGrade(0.85, 1.0, "wg-1")))

        // 2. Deleting non-existent IDs must return None AND perform zero storage encode writes
        val scaleEncodesBefore = scaleCodec.encodeCalls
        val wgEncodesBefore = wgCodec.encodeCalls

        assertTrue(scaleDao.deleteGradeScale("missing-scale").isNone())
        assertTrue(gradesDao.deleteGrade("missing-grade").isNone())
        assertTrue(wgDao.deleteWeightedGrade("missing-wg").isNone())

        assertEquals(scaleEncodesBefore, scaleCodec.encodeCalls, "A3-11: No-op scale/grade delete must not encode to storage")
        assertEquals(wgEncodesBefore, wgCodec.encodeCalls, "A3-11: No-op weighted grade delete must not encode to storage")
    }

    @Test
    fun gradeScaleDaoStoreImpl_coalesces_null_store_data_instead_of_dropping_write_A3_12() = runTest {
        val scaleCodec = CountingInMemoryCodec<GradeScalesStoreData>(current = null)
        // default = null simulates cleared/deleted KStore state
        val scaleStore = storeOf<GradeScalesStoreData>(default = null, codec = scaleCodec)
        val scaleProvider = GradeScaleStoreProvider(scaleStore, testDispatchers)
        val scaleDao = GradeScaleDaoStoreImpl(scaleProvider)

        val newScale = GradeScale(
            id = "s-null",
            gradeScaleName = "Recovered Scale",
            totalPoints = 15.0,
            grades = persistentListOf(Grade("1", 0.9, "wrong-id", "Wrong Name", "g-null")),
        )
        assertTrue(scaleDao.upsertGradeScale(newScale).isSome(), "A3-12: upsertGradeScale must recover when KStore value is null")
        val persisted = scaleDao.getGradeScaleById("s-null").first()
        assertEquals("Recovered Scale", persisted?.gradeScaleName)
        assertEquals("s-null", persisted?.grades?.single()?.idOfGradeScale)
        assertEquals("Recovered Scale", persisted?.grades?.single()?.nameOfScale)
    }
}
