package de.felixlf.gradingscale2.network.di

import de.felixlf.gradingscale2.entities.network.AuthTokenProvider
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal const val DEFAULT_REQUEST_TIMEOUT_MS = 15_000L
internal const val DEFAULT_CONNECT_TIMEOUT_MS = 10_000L
internal const val DEFAULT_SOCKET_TIMEOUT_MS = 15_000L

internal val gradingScaleJson: Json = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
}

private val authParamRegex = Regex("([?&]auth=)[^&\\s]+", RegexOption.IGNORE_CASE)

internal fun redactAuthQueryParams(message: String): String =
    authParamRegex.replace(message, "$1[REDACTED]")

internal class RedactingLogger(
    private val delegate: Logger,
) : Logger {
    override fun log(message: String) {
        delegate.log(redactAuthQueryParams(message))
    }
}

internal fun provideGradingScaleHttpClient(authTokenProvider: AuthTokenProvider): HttpClient = HttpClient {
    configureGradingScaleClient(authTokenProvider)
}

internal fun HttpClientConfig<*>.configureGradingScaleClient(
    authTokenProvider: AuthTokenProvider,
    logLevel: LogLevel = LogLevel.NONE,
    logSink: Logger = Logger.DEFAULT,
) {
    install(HttpCache)
    setupTimeout()
    setupLogging(logLevel = logLevel, logSink = logSink)
    setupContentNegotiation()
    setupAuth(authTokenProvider)
}

private fun HttpClientConfig<*>.setupTimeout() {
    install(HttpTimeout) {
        requestTimeoutMillis = DEFAULT_REQUEST_TIMEOUT_MS
        connectTimeoutMillis = DEFAULT_CONNECT_TIMEOUT_MS
        socketTimeoutMillis = DEFAULT_SOCKET_TIMEOUT_MS
    }
}

private fun HttpClientConfig<*>.setupContentNegotiation() {
    install(ContentNegotiation) {
        json(gradingScaleJson)
    }
}

private fun HttpClientConfig<*>.setupLogging(
    logLevel: LogLevel,
    logSink: Logger,
) {
    if (logLevel == LogLevel.NONE) return
    install(Logging) {
        logger = RedactingLogger(logSink)
        level = logLevel
        sanitizeHeader { header -> header.equals(HttpHeaders.Authorization, ignoreCase = true) }
    }
}

internal fun HttpClientConfig<*>.setupAuth(authTokenProvider: AuthTokenProvider) {
    install(FirebaseAuthPlugin) {
        tokenProvider = authTokenProvider
    }
}
