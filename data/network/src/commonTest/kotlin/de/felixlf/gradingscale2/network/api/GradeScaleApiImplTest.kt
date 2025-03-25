package de.felixlf.gradingscale2.network.api

import de.felixlf.gradingscale2.entities.network.BaseUrlProvider
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GradeScaleApiImplTest {

    private lateinit var sut: GradeScaleApiImpl
    private lateinit var httpClient: HttpClient

    private val gradeNamesResponseEx =
        "{\"Germany\":[\"Grundschule\",\"KMK-Standard Oberstufe und Abiturprüfungen\",\"IHK\",\"Niedersachsen Französisch und Spanisch\",\"Hamburg STS7-STS10\",\"Hamburg Abitur - Mathe, Chemie, Physik, Biologie, Informatik\",\"Berlin JGH Französisch, Spanisch, Russisch\"],\"International\":[\"10 to 0\"],\"Mexico\":[\"SEP\"],\"USA\":[\"Letter Grades - Public High Schools 2009\",\"Letter Grades - Collegues (non universal)\"]}"

    private val gradeScaleResponse =
        "{\"country\":\"Germany\",\"gradeScaleName\":\"Berlin JGH Französisch, Spanisch, Russisch\",\"grades\":[{\"gradeName\":\"1+ (15)\",\"percentage\":1.0},{\"gradeName\":\"1 (14)\",\"percentage\":0.95},{\"gradeName\":\"1- (13)\",\"percentage\":0.9},{\"gradeName\":\"2+ (12)\",\"percentage\":0.85},{\"gradeName\":\"2 (11)\",\"percentage\":0.8},{\"gradeName\":\"2- (10)\",\"percentage\":0.75},{\"gradeName\":\"3+ (9)\",\"percentage\":0.7},{\"gradeName\":\"3 (8)\",\"percentage\":0.65},{\"gradeName\":\"3- (7)\",\"percentage\":0.6},{\"gradeName\":\"4+ (6)\",\"percentage\":0.55},{\"gradeName\":\"4 (5)\",\"percentage\":0.5},{\"gradeName\":\"4- (4)\",\"percentage\":0.45},{\"gradeName\":\"5+ (3)\",\"percentage\":0.35},{\"gradeName\":\"5 (2)\",\"percentage\":0.2},{\"gradeName\":\"5- (1)\",\"percentage\":0.1},{\"gradeName\":\"6 (0)\",\"percentage\":0.09}]}"

    private val urlProvider = object : BaseUrlProvider {
        override val baseApiUrl: String = "https://something.example/"
    }

    private fun setUpMockEngineResponse(engineConfig: (suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData)?) {
        val mockEngine = engineConfig?.let(MockEngine::invoke)
        httpClient = mockEngine?.let { HttpClient(it) { setupJson() } } ?: HttpClient { setupJson() }
        sut = GradeScaleApiImpl(httpClient = httpClient, baseUrlProvider = urlProvider)
    }

    private fun HttpClientConfig<*>.setupJson() {
        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    isLenient = true
                },
            )
        }
    }

    @Test
    fun `test countriesAndGrades returns a correct list of countries in case request was success`() = runTest {
        // Given
        setUpMockEngineResponse {
            respond(
                content = ByteReadChannel(gradeNamesResponseEx),
                headers = headersOf("Content-Type", "application/json"),
            )
        }
        // When
        val result = sut.countriesAndGrades()

        // Then

        val list = result.getOrNull()
        assertNotNull(list)
        assertEquals(4, list.size)
        assertTrue(list.any { it.country == "Germany" })
        assertTrue(list.any { it.country == "International" })
        assertTrue(list.any { it.country == "Mexico" })
        assertTrue(list.any { it.country == "USA" })

        val germany = list.find { it.country == "Germany" }
        assertNotNull(germany)
        assertEquals(7, germany.gradesScalesNames.size)
    }

    @Test
    fun `test gradeScaleWithName returns a correct grade scale in case request was success`() = runTest {
        // Given
        setUpMockEngineResponse {
            respond(
                content = ByteReadChannel(gradeScaleResponse),
                headers = headersOf("Content-Type", "application/json"),
            )
        }
        // When
        val result = sut.gradeScaleWithName("Germany", "Berlin JGH Französisch, Spanisch, Russisch")

        // Then
        val gradeScale = result.getOrNull()
        assertNotNull(gradeScale)
        assertEquals("Berlin JGH Französisch, Spanisch, Russisch", gradeScale.gradeScaleName)
        assertEquals("Germany", gradeScale.country)
        assertEquals(16, gradeScale.grades.size)
    }
}
