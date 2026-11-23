package de.felixlf.gradingscale2.network.di

import de.felixlf.gradingscale2.entities.network.AuthTokenProvider
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Plugin which automatically adds the Firebase Auth ID Token to the request as a query argument.
 * See: https://firebase.google.com/docs/database/rest/auth#authenticate_with_an_id_token
 */
internal val FirebaseAuthPlugin =
    createClientPlugin("FirebaseAuthPlugin", ::FirebaseAuthProviderPluginConfig) {
        val provider = pluginConfig.tokenProvider ?: return@createClientPlugin

        on(Send) { request ->
            // Gets the latest id Token
            withTimeoutOrNull(pluginConfig.tokenTimeoutMs) { provider.getTokenFlow().first { it != null } }
                ?.let(request::setAuthToken)

            val originalCall = proceed(request)

            // In case we receive an unauthorized response, refresh the token and retry once
            if (originalCall.response.status == HttpStatusCode.Unauthorized) {
                val updatedIdToken = provider.refreshToken().getOrNull()
                if (updatedIdToken != null) {
                    request.setAuthToken(updatedIdToken)
                    proceed(request)
                } else {
                    originalCall
                }
            } else {
                originalCall
            }
        }
    }

internal class FirebaseAuthProviderPluginConfig {
    var tokenProvider: AuthTokenProvider? = null
    var tokenTimeoutMs: Long = 5_000L
}

/**
 * Sets or replaces the Firebase authentication token query parameter on the request.
 * See: https://firebase.google.com/docs/database/rest/auth#authenticate_with_an_id_token
 */
private fun HttpRequestBuilder.setAuthToken(idToken: String) = url {
    parameters["auth"] = idToken
}
