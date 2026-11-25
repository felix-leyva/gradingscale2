package de.felixlf.gradingscale2.network.di

import de.felixlf.gradingscale2.entities.network.AuthTokenProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FirebaseAuthPluginTest {

    private class FakeAuthTokenProvider(
        initialToken: String? = "initial-token",
        var refreshResult: Result<String> = Result.success("fresh-token"),
    ) : AuthTokenProvider {
        private val tokenFlow = MutableStateFlow(initialToken)
        var refreshCallCount: Int = 0
            private set

        override fun getTokenFlow(): StateFlow<String?> = tokenFlow

        override suspend fun refreshToken(): Result<String> {
            refreshCallCount++
            return refreshResult
        }
    }

    @Test
    fun doesNotRefreshTokenOnSuccessfulResponse() = runTest {
        val provider = FakeAuthTokenProvider(initialToken = "initial-token")
        val capturedAuthParams = mutableListOf<List<String>>()
        val engine = MockEngine { request ->
            capturedAuthParams += request.url.parameters.getAll("auth").orEmpty()
            respond(content = "{}", status = HttpStatusCode.OK)
        }

        val client = HttpClient(engine) {
            setupAuth(provider)
        }

        val response = client.get("https://example.test/countriesAndGrades.json")
        client.close()

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(0, provider.refreshCallCount)
        assertEquals(listOf(listOf("initial-token")), capturedAuthParams)
    }

    @Test
    fun replacesAuthQueryParameterInsteadOfDuplicatingOn401Retry() = runTest {
        val provider = FakeAuthTokenProvider(
            initialToken = "expired-token",
            refreshResult = Result.success("fresh-token"),
        )
        val capturedAuthParams = mutableListOf<List<String>>()
        var attempt = 0
        val engine = MockEngine { request ->
            capturedAuthParams += request.url.parameters.getAll("auth").orEmpty()
            attempt++
            if (attempt == 1) {
                respond(content = "Unauthorized", status = HttpStatusCode.Unauthorized)
            } else {
                respond(content = "{}", status = HttpStatusCode.OK)
            }
        }

        val client = HttpClient(engine) {
            setupAuth(provider)
        }

        val response = client.get("https://example.test/countriesAndGrades.json")
        client.close()

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(1, provider.refreshCallCount)
        assertEquals(2, capturedAuthParams.size)
        assertEquals(listOf("expired-token"), capturedAuthParams[0])
        assertEquals(listOf("fresh-token"), capturedAuthParams[1])
    }

    @Test
    fun returnsOriginal401WhenRefreshFails() = runTest {
        val provider = FakeAuthTokenProvider(
            initialToken = "expired-token",
            refreshResult = Result.failure(IllegalStateException("offline")),
        )
        var attemptCount = 0
        val engine = MockEngine {
            attemptCount++
            respond(content = "Unauthorized", status = HttpStatusCode.Unauthorized)
        }

        val client = HttpClient(engine) {
            setupAuth(provider)
        }

        val response = client.get("https://example.test/countriesAndGrades.json")
        client.close()

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(1, attemptCount)
        assertEquals(1, provider.refreshCallCount)
    }
}
