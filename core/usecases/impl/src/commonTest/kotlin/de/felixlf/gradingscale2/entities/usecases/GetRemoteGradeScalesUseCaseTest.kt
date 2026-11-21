package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Either
import de.felixlf.gradingscale2.entities.models.remote.CountryGradingScales
import de.felixlf.gradingscale2.entities.models.remote.RemoteError
import de.felixlf.gradingscale2.entities.repositories.FakeRemoteSyncRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetRemoteGradeScalesUseCaseTest {

    private lateinit var fakeRepository: FakeRemoteSyncRepository
    private lateinit var useCase: GetRemoteGradeScalesUseCaseImpl

    @BeforeTest
    fun setup() {
        fakeRepository = FakeRemoteSyncRepository()
        useCase = GetRemoteGradeScalesUseCaseImpl(fakeRepository)
    }

    @Test
    fun `test get remote grade scales when successful`() = runTest {
        // Given
        val countryGradingScales1 = CountryGradingScales(
            country = "Germany",
            gradesScalesNames = listOf("German Scale 1", "German Scale 2"),
        )
        val countryGradingScales2 = CountryGradingScales(
            country = "France",
            gradesScalesNames = listOf("French Scale"),
        )
        fakeRepository.addCountryGradingScales(countryGradingScales1)
        fakeRepository.addCountryGradingScales(countryGradingScales2)

        // When
        val result: Either<RemoteError, ImmutableList<CountryGradingScales>> = useCase()

        // Then
        assertTrue(result.isRight())
        result.onRight { scales: ImmutableList<CountryGradingScales> ->
            assertEquals(2, scales.size)

            val germany = scales.find { it.country == "Germany" }
            assertTrue(germany != null)
            assertEquals(2, germany?.gradesScalesNames?.size)
            assertTrue(germany?.gradesScalesNames?.contains("German Scale 1") == true)
            assertTrue(germany?.gradesScalesNames?.contains("German Scale 2") == true)

            val france = scales.find { it.country == "France" }
            assertTrue(france != null)
            assertEquals(1, france?.gradesScalesNames?.size)
            assertTrue(france?.gradesScalesNames?.contains("French Scale") == true)
        }
    }

    @Test
    fun `test get remote grade scales when empty`() = runTest {
        // Given - empty repository

        // When
        val result: Either<RemoteError, ImmutableList<CountryGradingScales>> = useCase()

        // Then
        assertTrue(result.isRight())
        result.onRight { scales: ImmutableList<CountryGradingScales> ->
            assertEquals(0, scales.size)
        }
    }

    @Test
    fun `test get remote grade scales when error occurs`() = runTest {
        // Given
        fakeRepository.shouldFail = true

        // When
        val result: Either<RemoteError, ImmutableList<CountryGradingScales>> = useCase()

        // Then
        assertTrue(result.isLeft())
        result.onLeft { error: RemoteError ->
            assertEquals(500, error.code)
            assertEquals("Network Error", error.message)
        }
    }
}
