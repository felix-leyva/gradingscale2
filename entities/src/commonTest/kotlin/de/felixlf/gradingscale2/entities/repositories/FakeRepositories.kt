package de.felixlf.gradingscale2.entities.repositories

import arrow.core.Either
import arrow.core.Option
import arrow.core.left
import arrow.core.raise.option
import arrow.core.right
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.models.remote.CountryGradingScales
import de.felixlf.gradingscale2.entities.models.remote.GradeScaleDTO
import de.felixlf.gradingscale2.entities.models.remote.RemoteError
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.map

/**
 * Fake implementation of GradeScaleRepository for testing.
 */
class FakeGradeScaleRepository : GradeScaleRepository {
    private val gradeScalesFlow = MutableStateFlow<ImmutableList<GradeScale>>(persistentListOf())
    var shouldFail = false

    fun reset() {
        gradeScalesFlow.value = persistentListOf()
        shouldFail = false
    }

    override fun getGradeScaleById(id: String): SharedFlow<GradeScale?> {
        val resultFlow = MutableStateFlow<GradeScale?>(null)

        // Initialize with current state
        val scale = gradeScalesFlow.value.find { it.id == id }
        resultFlow.value = scale

        return resultFlow
    }

    override fun getGradeScales(): SharedFlow<ImmutableList<GradeScale>> {
        return gradeScalesFlow
    }

    override suspend fun upsertGradeScale(gradeScale: GradeScale): Option<String> = option {
        ensure(!shouldFail)

        val existingScale = gradeScalesFlow.value.find { it.id == gradeScale.id }
        val updatedList = if (existingScale != null) {
            // Update existing scale
            gradeScalesFlow.value.map {
                if (it.id == gradeScale.id) gradeScale else it
            }.toImmutableList()
        } else {
            // Add new scale
            (gradeScalesFlow.value + gradeScale).toImmutableList()
        }

        gradeScalesFlow.value = updatedList
        gradeScale.id
    }

    override suspend fun deleteGradeScale(gradeScaleId: String): Option<Unit> = option {
        ensure(!shouldFail)
        ensure(gradeScalesFlow.value.any { it.id == gradeScaleId })

        gradeScalesFlow.value = gradeScalesFlow.value
            .filter { it.id != gradeScaleId }
            .toImmutableList()
    }

    /**
     * Helper method to add a grade scale directly for testing setup.
     */
    fun addGradeScale(gradeScale: GradeScale) {
        gradeScalesFlow.value = (gradeScalesFlow.value + gradeScale).toImmutableList()
    }
}

/**
 * Fake implementation of GradesRepository for testing.
 */
class FakeGradesRepository : GradesRepository {
    private val gradesFlow = MutableStateFlow<ImmutableList<Grade>>(persistentListOf())
    var shouldFail = false

    fun reset() {
        gradesFlow.value = persistentListOf()
        shouldFail = false
    }

    override fun getAllGradesFromGradeScale(gradeScaleId: String): SharedFlow<List<Grade>> {
        val resultFlow = MutableStateFlow<List<Grade>>(emptyList())

        // Initialize with current state
        val grades = gradesFlow.value.filter { it.idOfGradeScale == gradeScaleId }
        resultFlow.value = grades

        return resultFlow
    }

    override fun getGradeById(gradeId: String): SharedFlow<Grade?> {
        val resultFlow = MutableStateFlow<Grade?>(null)

        // Initialize with current state
        val grade = gradesFlow.value.find { it.uuid == gradeId }
        resultFlow.value = grade

        return resultFlow
    }

    fun getGradesByScaleId(gradeScaleId: String): SharedFlow<ImmutableList<Grade>> {
        val resultFlow = MutableStateFlow<ImmutableList<Grade>>(persistentListOf())

        // Initialize with current state
        val grades = gradesFlow.value.filter { it.idOfGradeScale == gradeScaleId }.toImmutableList()
        resultFlow.value = grades

        return resultFlow
    }

    override suspend fun upsertGrade(grade: Grade): Option<Unit> = option {
        ensure(!shouldFail)

        val existingGrade = gradesFlow.value.find { it.uuid == grade.uuid }
        val updatedList = if (existingGrade != null) {
            // Update existing grade
            gradesFlow.value.map {
                if (it.uuid == grade.uuid) grade else it
            }.toImmutableList()
        } else {
            // Add new grade
            (gradesFlow.value + grade).toImmutableList()
        }

        gradesFlow.value = updatedList
    }

    override suspend fun deleteGrade(gradeId: String): Option<Unit> = option {
        ensure(!shouldFail)
        ensure(gradesFlow.value.any { it.uuid == gradeId })

        gradesFlow.value = gradesFlow.value
            .filter { it.uuid != gradeId }
            .toImmutableList()
    }

    /**
     * Helper method to add a grade directly for testing setup.
     */
    fun addGrade(grade: Grade) {
        gradesFlow.value = (gradesFlow.value + grade).toImmutableList()
    }
}

/**
 * Fake implementation of RemoteSyncRepository for testing.
 */
class FakeRemoteSyncRepository : RemoteSyncRepository {
    var remoteGradeScales = mutableListOf<CountryGradingScales>()
    var remoteScales = mutableListOf<GradeScaleDTO>()
    var shouldFail = false

    // Define error instances for testing
    val networkError = RemoteError(500, "Network Error")
    val notFoundError = RemoteError(404, "Not Found")

    fun reset() {
        remoteGradeScales.clear()
        remoteScales.clear()
        shouldFail = false
    }

    override suspend fun countriesAndGrades(): Either<RemoteError, List<CountryGradingScales>> {
        return if (shouldFail) {
            networkError.left()
        } else {
            remoteGradeScales.toList().right()
        }
    }

    override suspend fun gradeScaleWithName(country: String, name: String): Either<RemoteError, GradeScaleDTO> {
        return if (shouldFail) {
            networkError.left()
        } else {
            val gradeScale = remoteScales.find { it.gradeScaleName == name && it.country == country }
            gradeScale?.right() ?: notFoundError.left()
        }
    }

    /**
     * Helper method to add a remote grade scale for testing.
     */
    fun addRemoteGradeScale(gradeScale: GradeScaleDTO) {
        remoteScales.add(gradeScale)
    }

    /**
     * Helper method to add a country grading scale for testing.
     */
    fun addCountryGradingScales(countryGradingScales: CountryGradingScales) {
        remoteGradeScales.add(countryGradingScales)
    }
}
