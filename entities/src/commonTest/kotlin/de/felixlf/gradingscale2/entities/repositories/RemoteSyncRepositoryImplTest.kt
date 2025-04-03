package de.felixlf.gradingscale2.entities.repositories

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import de.felixlf.gradingscale2.entities.models.remote.CountryGradingScales
import de.felixlf.gradingscale2.entities.models.remote.GradeDTO
import de.felixlf.gradingscale2.entities.models.remote.GradeScaleDTO
import de.felixlf.gradingscale2.entities.models.remote.RemoteError
import de.felixlf.gradingscale2.entities.network.GradeScaleApi
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RemoteSyncRepositoryImplTest {

    private lateinit var repository: RemoteSyncRepositoryImpl
    private lateinit var mockApi: MockGradeScaleApi

    @BeforeTest
    fun setup() {
        mockApi = MockGradeScaleApi()
        repository = RemoteSyncRepositoryImpl(mockApi)
    }

    @Test
    fun `countriesAndGrades delegates to API and returns success result`() = runTest {
        // Given
        val expectedResult = listOf(
            CountryGradingScales("Germany", listOf("German Scale 1", "German Scale 2")),
            CountryGradingScales("France", listOf("French Scale")),
        )
        mockApi.countriesAndGradesResult = expectedResult.right()

        // When
        val result = repository.countriesAndGrades()

        // Then
        assertTrue(result.isRight())
        result.onRight { countries: List<CountryGradingScales> ->
            assertEquals(2, countries.size)

            val germany = countries.find { it.country == "Germany" }
            assertTrue(germany != null)
            assertEquals(2, germany.gradesScalesNames.size)
            assertContains(germany.gradesScalesNames, "German Scale 1")
            assertContains(germany.gradesScalesNames, "German Scale 2")

            val france = countries.find { it.country == "France" }
            assertTrue(france != null)
            assertEquals(1, france.gradesScalesNames.size)
            assertContains(france.gradesScalesNames, "French Scale")
        }
    }

    @Test
    fun `countriesAndGrades delegates to API and returns error result`() = runTest {
        // Given
        val error = RemoteError(500, "Network error")
        mockApi.countriesAndGradesResult = error.left()

        // When
        val result = repository.countriesAndGrades()

        // Then
        assertTrue(result.isLeft())
        result.onLeft { remoteError: RemoteError ->
            assertEquals(500, remoteError.code)
            assertEquals("Network error", remoteError.message)
        }
    }

    @Test
    fun `gradeScaleWithName delegates to API and returns success result`() = runTest {
        // Given
        val expectedResult = GradeScaleDTO(
            gradeScaleName = "German Scale",
            country = "Germany",
            grades = listOf(
                GradeDTO("A", 0.9),
                GradeDTO("B", 0.8),
                GradeDTO("C", 0.7),
            ),
        )
        mockApi.gradeScaleWithNameResult = expectedResult.right()

        // When
        val result = repository.gradeScaleWithName("Germany", "German Scale")

        // Then
        assertTrue(result.isRight())
        result.onRight { gradeScale: GradeScaleDTO ->
            assertEquals("German Scale", gradeScale.gradeScaleName)
            assertEquals("Germany", gradeScale.country)
            assertEquals(3, gradeScale.grades.size)

            val gradeNames = gradeScale.grades.map { it.gradeName }
            assertContains(gradeNames, "A")
            assertContains(gradeNames, "B")
            assertContains(gradeNames, "C")
        }
    }

    @Test
    fun `gradeScaleWithName delegates to API and returns error result`() = runTest {
        // Given
        val error = RemoteError(404, "Not found")
        mockApi.gradeScaleWithNameResult = error.left()

        // When
        val result = repository.gradeScaleWithName("Germany", "Non-existent Scale")

        // Then
        assertTrue(result.isLeft())
        result.onLeft { remoteError: RemoteError ->
            assertEquals(404, remoteError.code)
            assertEquals("Not found", remoteError.message)
        }
    }

    @Test
    fun `API is called with correct parameters`() = runTest {
        // Given
        val country = "Germany"
        val name = "German Scale"

        // When
        repository.gradeScaleWithName(country, name)

        // Then
        assertEquals(country, mockApi.lastCountry)
        assertEquals(name, mockApi.lastScaleName)
    }

    /**
     * Mock implementation of GradeScaleApi for testing.
     */
    private class MockGradeScaleApi : GradeScaleApi {
        var countriesAndGradesResult: Either<RemoteError, List<CountryGradingScales>> =
            emptyList<CountryGradingScales>().right()

        var gradeScaleWithNameResult: Either<RemoteError, GradeScaleDTO> =
            GradeScaleDTO("", "", emptyList()).right()

        var lastCountry: String? = null
        var lastScaleName: String? = null

        override suspend fun countriesAndGrades(): Either<RemoteError, List<CountryGradingScales>> {
            return countriesAndGradesResult
        }

        override suspend fun gradeScaleWithName(country: String, name: String): Either<RemoteError, GradeScaleDTO> {
            lastCountry = country
            lastScaleName = name
            return gradeScaleWithNameResult
        }
    }
}
