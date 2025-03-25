package de.felixlf.gradingscale2.network.di

import de.felixlf.gradingscale2.entities.network.AuthTokenProvider
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal fun provideGradingScaleHttpClient(authTokenProvider: AuthTokenProvider): HttpClient = HttpClient {
    install(HttpCache)
    setupLogging()
    setupContentNegotiation()
    setupAuth(authTokenProvider)
}

private fun HttpClientConfig<*>.setupContentNegotiation() {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
            },
        )
    }
}

private fun HttpClientConfig<*>.setupLogging() {
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.HEADERS
    }
}

internal fun HttpClientConfig<*>.setupAuth(authTokenProvider: AuthTokenProvider) {
    install(FirebaseAuthPlugin) {
        tokenProvider = authTokenProvider
    }
}
