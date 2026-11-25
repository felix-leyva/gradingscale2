package de.felixlf.gradingscale2.network.di

import de.felixlf.gradingscale2.entities.models.remote.GradeScaleDTO
import de.felixlf.gradingscale2.entities.network.AuthTokenProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.pluginOrNull
import io.ktor.client.request.get
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GradingScaleClientTest {

    private val fakeAuthProvider = object : AuthTokenProvider {
        private val flow = MutableStateFlow<String?>("eyJhbGciOiJSUzI1NiJ9.secret.sig")
        override fun getTokenFlow(): StateFlow<String?> = flow
        override suspend fun refreshToken(): Result<String> = Result.success("eyJhbGciOiJSUzI1NiJ9.secret.sig")
    }

    @Test
    fun defaultClientDoesNotInstallLoggingPlugin() {
        val client = provideGradingScaleHttpClient(fakeAuthProvider)
        try {
            assertNull(client.pluginOrNull(Logging))
        } finally {
            client.close()
        }
    }

    @Test
    fun defaultClientInstallsHttpTimeoutPlugin() {
        val client = provideGradingScaleHttpClient(fakeAuthProvider)
        try {
            assertNotNull(client.pluginOrNull(HttpTimeout))
        } finally {
            client.close()
        }
    }

    @Test
    fun redactingLoggerSanitizesAuthTokensWhenLoggingEnabled() = runTest {
        val capturedLogs = mutableListOf<String>()
        val capturingLogger = object : Logger {
            override fun log(message: String) {
                capturedLogs += message
            }
        }
        val engine = MockEngine {
            respond(
                content = """{"country":"Germany","gradeScaleName":"Scale","extraKey":true,"grades":[{"gradeName":"A","percentage":0.9,"extraNested":1}]}""",
                headers = headersOf("Content-Type", "application/json"),
            )
        }

        val client = HttpClient(engine) {
            configureGradingScaleClient(
                authTokenProvider = fakeAuthProvider,
                logLevel = LogLevel.HEADERS,
                logSink = capturingLogger,
            )
        }

        val dto = client.get("https://example.test/gradeScales/Scale.json").body<GradeScaleDTO>()
        client.close()

        assertEquals("Scale", dto.gradeScaleName)
        assertEquals(1, dto.grades.size)
        assertTrue(capturedLogs.isNotEmpty())
        assertTrue(capturedLogs.any { it.contains("auth=[REDACTED]") }, "Expected redacted auth query in $capturedLogs")
        assertFalse(capturedLogs.any { it.contains("eyJhbGciOiJSUzI1NiJ9") }, "JWT leaked in $capturedLogs")
        assertFalse(capturedLogs.any { it.contains("secret.sig") }, "JWT signature leaked in $capturedLogs")
    }
}
