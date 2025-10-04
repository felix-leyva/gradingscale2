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
            withTimeoutOrNull(5000) { provider.getTokenFlow().first { it != null } }?.let(request::addAuthToken)

            val originalCall = proceed(request)

            // In case we receive an unauthorized response, we try to refresh the token
            originalCall.response.run {
                val updatedIdToken = provider.refreshToken().getOrNull()
                when {
                    status == HttpStatusCode.Unauthorized && updatedIdToken != null -> {
                        request.addAuthToken(updatedIdToken)
                        proceed(request)
                    }

                    else -> originalCall
                }
            }
        }
    }

internal class FirebaseAuthProviderPluginConfig {
    var tokenProvider: AuthTokenProvider? = null
}

/**
 * Adds the authentication token to the request.
 * as indicated by: https://firebase.google.com/docs/database/rest/auth#authenticate_with_an_id_token
 */
private fun HttpRequestBuilder.addAuthToken(idToken: String) = url {
    parameters.append("auth", idToken)
}
