package de.felixlf.gradingscale2.network.api

import de.felixlf.gradingscale2.entities.network.BaseUrlProvider
import de.felixlf.gradingscale2.network.di.gradingScaleJson
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
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
            json(gradingScaleJson)
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
        var capturedUrl = ""
        setUpMockEngineResponse { request ->
            capturedUrl = request.url.toString()
            respond(
                content = ByteReadChannel(gradeScaleResponse),
                headers = headersOf("Content-Type", "application/json"),
            )
        }
        // When
        val result = sut.gradeScaleWithName("Germany", "Berlin JGH Französisch, Spanisch, Russisch")

        // Then
        assertEquals(
            "https://something.example/gradeScales/Germany_Berlin%20JGH%20Franz%C3%B6sisch,%20Spanisch,%20Russisch.json",
            capturedUrl,
        )
        val gradeScale = result.getOrNull()
        assertNotNull(gradeScale)
        assertEquals("Berlin JGH Französisch, Spanisch, Russisch", gradeScale.gradeScaleName)
        assertEquals("Germany", gradeScale.country)
        assertEquals(16, gradeScale.grades.size)
    }

    @Test
    fun `test countriesAndGrades returns RemoteError when response status is error or non-2xx`() = runTest {
        setUpMockEngineResponse {
            respond(
                content = ByteReadChannel("Not Found"),
                status = io.ktor.http.HttpStatusCode.NotFound,
                headers = headersOf("Content-Type", "text/plain"),
            )
        }

        val result = sut.countriesAndGrades()
        assertTrue(result.isLeft())
        val error = result.leftOrNull()
        assertNotNull(error)
        assertEquals(404, error.code)
        assertEquals("Not Found", error.message)
    }

    @Test
    fun `test countriesAndGrades returns RemoteError when network throws exception`() = runTest {
        setUpMockEngineResponse {
            throw RuntimeException("Network unreachable")
        }

        val result = sut.countriesAndGrades()
        assertTrue(result.isLeft())
        val error = result.leftOrNull()
        assertNotNull(error)
        assertEquals(0, error.code)
        assertEquals("Network unreachable", error.message)
    }

    @Test
    fun `test gradeScaleWithName returns RemoteError when status is 404 or 500`() = runTest {
        setUpMockEngineResponse {
            respond(
                content = ByteReadChannel("Internal Server Error"),
                status = io.ktor.http.HttpStatusCode.InternalServerError,
                headers = headersOf("Content-Type", "text/plain"),
            )
        }

        val result = sut.gradeScaleWithName("Germany", "NonExistent")
        assertTrue(result.isLeft())
        val error = result.leftOrNull()
        assertNotNull(error)
        assertEquals(500, error.code)
        assertEquals("Internal Server Error", error.message)
    }

    @Test
    fun `test boundary status codes for 200 to 299 range`() = runTest {
        // Status 199 (below 200) should raise RemoteError
        setUpMockEngineResponse {
            respond(
                content = ByteReadChannel(""),
                status = io.ktor.http.HttpStatusCode.fromValue(199),
            )
        }
        val result199 = sut.countriesAndGrades()
        assertTrue(result199.isLeft())
        assertEquals(199, result199.leftOrNull()?.code)

        // Status 300 (above 299) should raise RemoteError
        setUpMockEngineResponse {
            respond(
                content = ByteReadChannel(""),
                status = io.ktor.http.HttpStatusCode.MultipleChoices,
            )
        }
        val result300 = sut.countriesAndGrades()
        assertTrue(result300.isLeft())
        assertEquals(300, result300.leftOrNull()?.code)

        // Status 300 on gradeScaleWithName should raise RemoteError
        val resultGradeScale300 = sut.gradeScaleWithName("Germany", "Scale")
        assertTrue(resultGradeScale300.isLeft())
        assertEquals(300, resultGradeScale300.leftOrNull()?.code)
    }

    @Test
    fun `countriesAndGrades rethrows CancellationException instead of returning RemoteError`() = runTest {
        setUpMockEngineResponse {
            throw CancellationException("cancelled")
        }

        assertFailsWith<CancellationException> {
            sut.countriesAndGrades()
        }
    }

    @Test
    fun `gradeScaleWithName returns RemoteError when network throws exception`() = runTest {
        setUpMockEngineResponse {
            throw RuntimeException("Network unreachable")
        }

        val result = sut.gradeScaleWithName("Germany", "Scale")
        assertTrue(result.isLeft())
        val error = result.leftOrNull()
        assertNotNull(error)
        assertEquals(0, error.code)
        assertTrue(error.message.contains("Network unreachable"))
    }

    @Test
    fun `gradeScaleWithName rethrows CancellationException`() = runTest {
        setUpMockEngineResponse {
            throw CancellationException("cancelled")
        }

        assertFailsWith<CancellationException> {
            sut.gradeScaleWithName("Germany", "Scale")
        }
    }

    @Test
    fun `countriesAndGrades returns RemoteError on null body HTML body and non-array country values`() = runTest {
        val payloads = listOf(
            "null",
            "<html>error</html>",
            """{"Germany":"not-an-array"}""",
        )

        for (payload in payloads) {
            setUpMockEngineResponse {
                respond(
                    content = ByteReadChannel(payload),
                    headers = headersOf("Content-Type", "application/json"),
                )
            }

            val result = sut.countriesAndGrades()
            assertTrue(result.isLeft(), "Expected Left for payload: $payload")
        }
    }

    @Test
    fun `gradeScaleWithName returns RemoteError on null body and malformed JSON`() = runTest {
        val payloads = listOf(
            "null",
            "<html>captive portal</html>",
        )

        for (payload in payloads) {
            setUpMockEngineResponse {
                respond(
                    content = ByteReadChannel(payload),
                    headers = headersOf("Content-Type", "application/json"),
                )
            }

            val result = sut.gradeScaleWithName("Germany", "Scale")
            assertTrue(result.isLeft(), "Expected Left for payload: $payload")
        }
    }

    @Test
    fun `gradeScaleWithName normalizes trailing slash and encodes special URL characters in path segments`() = runTest {
        var capturedUrl = ""
        setUpMockEngineResponse { request ->
            capturedUrl = request.url.toString()
            respond(
                content = ByteReadChannel(gradeScaleResponse),
                headers = headersOf("Content-Type", "application/json"),
            )
        }

        val result = sut.gradeScaleWithName("Germany", "A/B?x=1#frag%20")
        assertTrue(result.isRight())
        assertFalse(capturedUrl.contains("//gradeScales/"), "Unexpected double slash in $capturedUrl")
        assertTrue(
            capturedUrl.startsWith("https://something.example/gradeScales/"),
            "Expected normalized base path in $capturedUrl",
        )
        assertTrue(
            capturedUrl.endsWith(".json"),
            "Expected unfragmented .json suffix in $capturedUrl",
        )
        assertFalse(capturedUrl.contains("?x=1"), "Query delimiter was not encoded in $capturedUrl")
        assertFalse(capturedUrl.contains("#frag"), "Fragment delimiter was not encoded in $capturedUrl")
    }

    @Test
    fun `countriesAndGrades normalizes trailing slash in baseApiUrl`() = runTest {
        var capturedUrl = ""
        setUpMockEngineResponse { request ->
            capturedUrl = request.url.toString()
            respond(
                content = ByteReadChannel(gradeNamesResponseEx),
                headers = headersOf("Content-Type", "application/json"),
            )
        }

        val result = sut.countriesAndGrades()
        assertTrue(result.isRight())
        assertEquals("https://something.example/countriesAndGrades.json", capturedUrl)
    }

    @Test
    fun `gradeScaleWithName ignores unknown JSON keys`() = runTest {
        val payloadWithExtraKeys =
            """{"country":"Germany","gradeScaleName":"Custom","extraRootField":123,"grades":[{"gradeName":"A","percentage":0.95,"extraGradeField":"ignored"}]}"""
        setUpMockEngineResponse {
            respond(
                content = ByteReadChannel(payloadWithExtraKeys),
                headers = headersOf("Content-Type", "application/json"),
            )
        }

        val result = sut.gradeScaleWithName("Germany", "Custom")
        assertTrue(result.isRight())
        val dto = result.getOrNull()
        assertNotNull(dto)
        assertEquals("Custom", dto.gradeScaleName)
        assertEquals(1, dto.grades.size)
    }
}

