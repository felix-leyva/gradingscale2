package de.felixlf.gradingscale2.entities.repositories

import app.cash.turbine.test
import arrow.core.Option
import arrow.core.Some
import de.felixlf.gradingscale2.entities.daos.GradeScaleDao
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.util.MockGradeScalesGenerator
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GradeScaleRepositoryImplTest {
    private val dao: MockGradeScaleDao = MockGradeScaleDao()
    private lateinit var sut: GradeScaleRepositoryImpl

    @BeforeTest
    fun setup() {
        dao.success = true
        dao.gradeScales.value = MockGradeScalesGenerator().gradeScales
    }

    @Test
    fun getGradeScales_returns_a_list_of_the_grade_scales() = runTest {
        // Given
        sut = GradeScaleRepositoryImpl(gradeScaleDao = dao, scope = backgroundScope)

        val expectedScales = MockGradeScalesGenerator().gradeScales

        // When / Then
        sut.gradeScales.test {
            assertEquals(expectedScales, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun getGradeScales_returns_a_list_and_then_emits_again_after_list_changes() = runTest {
        // Given
        sut = GradeScaleRepositoryImpl(gradeScaleDao = dao, scope = backgroundScope)
        val expectedScales = MockGradeScalesGenerator().gradeScales

        // When / Then
        sut.gradeScales.test {
            assertEquals(expectedScales, awaitItem())

            sut.upsertGradeScale(expectedScales.first().copy(gradeScaleName = "Neew"))
            assertTrue { awaitItem().map { it.gradeScaleName }.contains("Neew") }
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun getGradeScales_returns_empty_list_if_no_grade_scales_exist() = runTest {
        // Given
        sut = GradeScaleRepositoryImpl(gradeScaleDao = dao, scope = backgroundScope)
        val expectedScales = emptyList<GradeScale>()
        dao.gradeScales.value = persistentListOf()

        // When / Then
        sut.gradeScales.test {
            assertEquals(expectedScales, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun getGradeScaleById_returns_a_grade_scale_by_id() = runTest {
        // Given
        sut = GradeScaleRepositoryImpl(gradeScaleDao = dao, scope = backgroundScope)
        val expectedScales = MockGradeScalesGenerator().gradeScales
        val id = expectedScales.first().id

        // When / Then
        sut.getGradeScaleById(id).test {
            assertEquals(expectedScales.first(), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun getGradeScaleById_returns_null_if_no_grade_scale_with_id_exists() = runTest {
        // Given
        sut = GradeScaleRepositoryImpl(gradeScaleDao = dao, scope = backgroundScope)
        val id = "non-existing-id"

        // When / Then
        sut.getGradeScaleById(id).test {
            assertEquals(null, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun upsertGradeScale_inserts_a_new_grade_scale() = runTest {
        // Given
        sut = GradeScaleRepositoryImpl(gradeScaleDao = dao, scope = backgroundScope)
        val gradeScale = MockGradeScalesGenerator().gradeScales.first()
        dao.gradeScales.value = persistentListOf()

        sut.gradeScales.test {
            assertEquals(emptyList(), awaitItem())
            // When
            sut.upsertGradeScale(gradeScale)
            // Then
            assertEquals(listOf(gradeScale), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun upsertGradeScale_updates_an_existing_grade_scale() = runTest {
        // Given
        sut = GradeScaleRepositoryImpl(gradeScaleDao = dao, scope = backgroundScope)
        val gradeScales = MockGradeScalesGenerator().gradeScales
        // Insert into the dao
        sut.gradeScales.test {
            assertEquals(gradeScales, awaitItem())
            // When
            val updated = gradeScales.first().copy(totalPoints = 25.0)
            val newScales = gradeScales.map { if (it.id == updated.id) updated else it }
            sut.upsertGradeScale(updated)
            assertEquals(newScales, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun deleteGradeScale_deletes_a_grade_scale() = runTest {
        // Given
        sut = GradeScaleRepositoryImpl(gradeScaleDao = dao, scope = backgroundScope)
        val gradeScales = MockGradeScalesGenerator().gradeScales
        // Insert into the dao
        sut.gradeScales.test {
            assertEquals(gradeScales, awaitItem())
            // When
            val toDelete = gradeScales.first()
            val newScales = gradeScales.drop(1)
            sut.deleteGradeScale(toDelete.id)
            assertEquals(newScales, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun deleteGradeScale_does_nothing_if_no_grade_scale_with_id_exists() = runTest {
        // Given
        sut = GradeScaleRepositoryImpl(gradeScaleDao = dao, scope = backgroundScope)
        val gradeScales = MockGradeScalesGenerator().gradeScales
        // Insert into the dao
        sut.gradeScales.test {
            assertEquals(gradeScales, awaitItem())
            // When
            val result = sut.deleteGradeScale("non-existing-id")
            assertTrue(result.isNone())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun gradeScales_and_getGradeScaleById_do_not_leak_active_collectors_after_collection_cancels() = runTest {
        val totalStarts = MutableStateFlow(0)
        val activeCollectors = MutableStateFlow(0)
        val expectedScales = MockGradeScalesGenerator().gradeScales
        val scalesState = MutableStateFlow(expectedScales)
        val countingDao = object : GradeScaleDao {
            override fun getGradeScaleById(id: String): Flow<GradeScale?> = scalesState
                .map { scales -> scales.find { it.id == id } }
                .onStart { activeCollectors.update { it + 1 } }
                .onCompletion { activeCollectors.update { it - 1 } }

            override fun getGradeScales(): Flow<ImmutableList<GradeScale>> = scalesState
                .onStart {
                    totalStarts.update { it + 1 }
                    activeCollectors.update { it + 1 }
                }
                .onCompletion { activeCollectors.update { it - 1 } }

            override suspend fun upsertGradeScale(gradeScale: GradeScale): Option<Unit> = Some(Unit)

            override suspend fun deleteGradeScale(gradeScaleId: String): Option<Unit> = Some(Unit)
        }
        val repository = GradeScaleRepositoryImpl(gradeScaleDao = countingDao, scope = backgroundScope)

        repeat(5) {
            repository.gradeScales.first()
            repository.getGradeScaleById(expectedScales.first().id).first()
        }
        assertEquals(1, totalStarts.value)

        advanceTimeBy(5_001L)
        runCurrent()
        assertEquals(0, activeCollectors.value)
    }

    @Test
    fun gradeScales_recovers_after_transient_upstream_dao_error() = runTest {
        val expectedScales = MockGradeScalesGenerator().gradeScales
        var calls = 0
        val activeCollectors = MutableStateFlow(0)
        val flakyDao = object : GradeScaleDao {
            override fun getGradeScaleById(id: String): Flow<GradeScale?> = flow {
                error("Expected getGradeScaleById to derive from gradeScales")
            }

            override fun getGradeScales(): Flow<ImmutableList<GradeScale>> = flow {
                activeCollectors.update { it + 1 }
                try {
                    if (calls++ == 0) {
                        throw IllegalStateException("Transient DB error")
                    }
                    emit(expectedScales)
                    awaitCancellation()
                } finally {
                    activeCollectors.update { it - 1 }
                }
            }

            override suspend fun upsertGradeScale(gradeScale: GradeScale): Option<Unit> = Some(Unit)

            override suspend fun deleteGradeScale(gradeScaleId: String): Option<Unit> = Some(Unit)
        }
        val repository = GradeScaleRepositoryImpl(gradeScaleDao = flakyDao, scope = backgroundScope)

        val firstResult = repository.gradeScales.first()
        assertEquals(expectedScales, firstResult)
        assertEquals(2, calls)

        advanceTimeBy(5_001L)
        runCurrent()
        assertEquals(0, activeCollectors.value)

        val secondResult = repository.gradeScales.first()
        assertEquals(expectedScales, secondResult)
        assertEquals(3, calls)

        advanceTimeBy(5_001L)
        runCurrent()
        assertEquals(0, activeCollectors.value)
    }

    @Test
    fun concurrent_collectors_of_gradeScales_and_getGradeScaleById_share_a_single_upstream_dao_collection() = runTest {
        val initialScales = MockGradeScalesGenerator().gradeScales
        val targetScale = initialScales.first()
        val totalStarts = MutableStateFlow(0)
        val activeCollectors = MutableStateFlow(0)
        val byIdDaoCalls = MutableStateFlow(0)
        val scalesState = MutableStateFlow(initialScales)
        val countingDao = object : GradeScaleDao {
            override fun getGradeScaleById(id: String): Flow<GradeScale?> {
                byIdDaoCalls.update { it + 1 }
                return scalesState.map { scales -> scales.find { it.id == id } }
            }

            override fun getGradeScales(): Flow<ImmutableList<GradeScale>> = scalesState
                .onStart {
                    totalStarts.update { it + 1 }
                    activeCollectors.update { it + 1 }
                }
                .onCompletion { activeCollectors.update { it - 1 } }

            override suspend fun upsertGradeScale(gradeScale: GradeScale): Option<Unit> = Some(Unit)

            override suspend fun deleteGradeScale(gradeScaleId: String): Option<Unit> = Some(Unit)
        }
        val repository = GradeScaleRepositoryImpl(gradeScaleDao = countingDao, scope = backgroundScope)

        val updates1 = Channel<ImmutableList<GradeScale>>(Channel.UNLIMITED)
        val updates2 = Channel<ImmutableList<GradeScale>>(Channel.UNLIMITED)
        val byIdUpdates = Channel<GradeScale?>(Channel.UNLIMITED)

        val job1 = launch { repository.gradeScales.collect { updates1.send(it) } }
        val job2 = launch { repository.gradeScales.collect { updates2.send(it) } }
        val job3 = launch { repository.getGradeScaleById(targetScale.id).collect { byIdUpdates.send(it) } }

        assertEquals(initialScales, updates1.receive())
        assertEquals(initialScales, updates2.receive())
        assertEquals(targetScale, byIdUpdates.receive())
        assertEquals(1, totalStarts.value)
        assertEquals(1, activeCollectors.value)
        assertEquals(0, byIdDaoCalls.value)

        val updatedTarget = targetScale.copy(gradeScaleName = "Shared Live Update")
        val updatedScales = persistentListOf(updatedTarget)
        scalesState.value = updatedScales

        assertEquals(updatedScales, updates1.receive())
        assertEquals(updatedScales, updates2.receive())
        assertEquals(updatedTarget, byIdUpdates.receive())
        assertEquals(1, totalStarts.value)
        assertEquals(1, activeCollectors.value)

        job1.cancelAndJoin()
        job2.cancelAndJoin()
        advanceTimeBy(5_001L)
        runCurrent()
        assertEquals(1, activeCollectors.value)

        job3.cancelAndJoin()
        runCurrent()
        assertEquals(1, activeCollectors.value)

        advanceTimeBy(5_001L)
        runCurrent()
        assertEquals(0, activeCollectors.value)
    }
}
