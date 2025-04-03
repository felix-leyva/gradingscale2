package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Either
import de.felixlf.gradingscale2.entities.models.remote.CountryAndName
import de.felixlf.gradingscale2.entities.models.remote.GradeDTO
import de.felixlf.gradingscale2.entities.models.remote.GradeScaleDTO
import de.felixlf.gradingscale2.entities.models.remote.RemoteError
import de.felixlf.gradingscale2.entities.repositories.FakeRemoteSyncRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetRemoteGradeScaleUseCaseTest {

    private lateinit var fakeRepository: FakeRemoteSyncRepository
    private lateinit var useCase: GetRemoteGradeScaleUseCaseImpl

    @BeforeTest
    fun setup() {
        fakeRepository = FakeRemoteSyncRepository()
        useCase = GetRemoteGradeScaleUseCaseImpl(fakeRepository)

        // Add test data
        val germanScale = GradeScaleDTO(
            gradeScaleName = "German Scale",
            country = "Germany",
            grades = listOf(
                GradeDTO("1.0", 0.95),
                GradeDTO("2.0", 0.80),
                GradeDTO("3.0", 0.65),
            ),
        )
        fakeRepository.addRemoteGradeScale(germanScale)
    }

    @Test
    fun `test get existing remote grade scale`() = runTest {
        // Given
        val countryAndName = CountryAndName("Germany", "German Scale")

        // When
        val result: Either<RemoteError, GradeScaleDTO> = useCase(countryAndName)

        // Then
        assertTrue(result.isRight())
        result.onRight { scale: GradeScaleDTO ->
            assertEquals("German Scale", scale.gradeScaleName)
            assertEquals("Germany", scale.country)
            assertEquals(3, scale.grades.size)

            val gradeNames = scale.grades.map { it.gradeName }
            assertTrue(gradeNames.contains("1.0"))
            assertTrue(gradeNames.contains("2.0"))
            assertTrue(gradeNames.contains("3.0"))
        }
    }

    @Test
    fun `test get non-existing remote grade scale`() = runTest {
        // Given
        val countryAndName = CountryAndName("France", "French Scale")

        // When
        val result: Either<RemoteError, GradeScaleDTO> = useCase(countryAndName)

        // Then
        assertTrue(result.isLeft())
        result.onLeft { error: RemoteError ->
            assertEquals(404, error.code)
            assertEquals("Not Found", error.message)
        }
    }

    @Test
    fun `test get remote grade scale when error occurs`() = runTest {
        // Given
        fakeRepository.shouldFail = true
        val countryAndName = CountryAndName("Germany", "German Scale")

        // When
        val result: Either<RemoteError, GradeScaleDTO> = useCase(countryAndName)

        // Then
        assertTrue(result.isLeft())
        result.onLeft { error: RemoteError ->
            assertEquals(500, error.code)
            assertEquals("Network Error", error.message)
        }
    }
}
