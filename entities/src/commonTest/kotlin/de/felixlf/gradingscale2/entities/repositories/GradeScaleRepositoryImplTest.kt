package de.felixlf.gradingscale2.entities.repositories

import app.cash.turbine.test
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.util.GradeScaleGenerator
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GradeScaleRepositoryImplTest {
    private val dao: MockGradeScaleDao = MockGradeScaleDao()
    private lateinit var sut: GradeScaleRepositoryImpl

    @BeforeTest
    fun setup() {
        dao.success = true
        dao.gradeScales.value = GradeScaleGenerator().gradeScales
    }

    @Test
    fun getGradeScales_returns_a_list_of_the_grade_scales() = runTest {
        // Given
        sut = GradeScaleRepositoryImpl(gradeScaleDao = dao, scope = this)

        val expectedScales = GradeScaleGenerator().gradeScales

        // When / Then
        sut.getGradeScales().test {
            assertEquals(persistentListOf<GradeScale>(), awaitItem())
            assertEquals(expectedScales, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
        coroutineContext.cancelChildren()
    }

    @Test
    fun getGradeScales_returns_a_list_and_then_emits_again_after_list_changes() = runTest {
        // Given
        sut = GradeScaleRepositoryImpl(gradeScaleDao = dao, scope = this)
        val expectedScales = GradeScaleGenerator().gradeScales

        // When / Then
        sut.getGradeScales().test {
            assertEquals(persistentListOf<GradeScale>(), awaitItem())
            assertEquals(expectedScales, awaitItem())

            sut.upsertGradeScale(expectedScales.first().copy(gradeScaleName = "Neew")).getOrThrow()
            assertTrue { awaitItem().map { it.gradeScaleName }.contains("Neew") }
            cancelAndConsumeRemainingEvents()
        }
        coroutineContext.cancelChildren()
    }

    @Test
    fun getGradeScales_returns_empty_list_if_no_grade_scales_exist() = runTest {
        // Given
        sut = GradeScaleRepositoryImpl(gradeScaleDao = dao, scope = this)
        val expectedScales = emptyList<GradeScale>()
        dao.gradeScales.value = persistentListOf()

        // When / Then
        sut.getGradeScales().test {
            assertEquals(expectedScales, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
        coroutineContext.cancelChildren()
    }

    @Test
    fun getGradeScaleById_returns_a_grade_scale_by_id() = runTest {
        // Given
        sut = GradeScaleRepositoryImpl(gradeScaleDao = dao, scope = this)
        val expectedScales = GradeScaleGenerator().gradeScales
        val id = expectedScales.first().id

        // When / Then
        sut.getGradeScaleById(id).test {
            assertEquals(null, awaitItem())
            assertEquals(expectedScales.first(), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
        coroutineContext.cancelChildren()
    }

    @Test
    fun getGradeScaleById_returns_null_if_no_grade_scale_with_id_exists() = runTest {
        // Given
        sut = GradeScaleRepositoryImpl(gradeScaleDao = dao, scope = this)
        val id = "non-existing-id"

        // When / Then
        sut.getGradeScaleById(id).test {
            assertEquals(null, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
        coroutineContext.cancelChildren()
    }

    @Test
    fun upsertGradeScale_inserts_a_new_grade_scale() = runTest {
        // Given
        sut = GradeScaleRepositoryImpl(gradeScaleDao = dao, scope = this)
        val gradeScale = GradeScaleGenerator().gradeScales.first()
        dao.gradeScales.value = persistentListOf()

        sut.getGradeScales().test {
            assertEquals(emptyList(), awaitItem())
            // When
            sut.upsertGradeScale(gradeScale).getOrThrow()
            // Then
            assertEquals(listOf(gradeScale), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
        coroutineContext.cancelChildren()
    }

    @Test
    fun upsertGradeScale_updates_an_existing_grade_scale() = runTest {
        // Given
        sut = GradeScaleRepositoryImpl(gradeScaleDao = dao, scope = this)
        val gradeScales = GradeScaleGenerator().gradeScales
        // Insert into the dao
        sut.getGradeScales().test {
            assertEquals(persistentListOf<GradeScale>(), awaitItem())
            assertEquals(gradeScales, awaitItem())
            // When
            val updated = gradeScales.first().copy(totalPoints = 25.0)
            val newScales = gradeScales.map { if (it.id == updated.id) updated else it }
            sut.upsertGradeScale(updated).getOrThrow()
            assertEquals(newScales, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
        coroutineContext.cancelChildren()
    }

    @Test
    fun deleteGradeScale_deletes_a_grade_scale() = runTest {
        // Given
        sut = GradeScaleRepositoryImpl(gradeScaleDao = dao, scope = this)
        val gradeScales = GradeScaleGenerator().gradeScales
        // Insert into the dao
        sut.getGradeScales().test {
            assertEquals(persistentListOf<GradeScale>(), awaitItem())
            assertEquals(gradeScales, awaitItem())
            // When
            val toDelete = gradeScales.first()
            val newScales = gradeScales.drop(1)
            sut.deleteGradeScale(toDelete.id).getOrThrow()
            assertEquals(newScales, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
        coroutineContext.cancelChildren()
    }

    @Test
    fun deleteGradeScale_does_nothing_if_no_grade_scale_with_id_exists() = runTest {
        // Given
        sut = GradeScaleRepositoryImpl(gradeScaleDao = dao, this)
        val gradeScales = GradeScaleGenerator().gradeScales
        // Insert into the dao
        sut.getGradeScales().test {
            assertEquals(persistentListOf<GradeScale>(), awaitItem())
            assertEquals(gradeScales, awaitItem())
            // When
            val result = sut.deleteGradeScale("non-existing-id")
            assertTrue(result.isFailure)
            cancelAndConsumeRemainingEvents()
        }
        coroutineContext.cancelChildren()
    }
}
