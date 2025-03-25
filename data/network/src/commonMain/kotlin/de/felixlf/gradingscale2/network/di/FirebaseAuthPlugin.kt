package de.felixlf.gradingscale2.network.di

import de.felixlf.gradingscale2.entities.network.AuthTokenProvider
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpStatusCode

internal val FirebaseAuthPlugin =
    createClientPlugin("FirebaseAuthPlugin", ::FirebaseAuthProviderPluginConfig) {
        val provider = pluginConfig.tokenProvider ?: return@createClientPlugin

        on(Send) { request ->
            // Gets the latest id Token, or if not available, tries to get a new one
            provider.getTokenFlow().value ?: provider.refreshToken().getOrNull()
                ?.let(request::addAuthToken)
            val originalCall = proceed(request)

            // In case we receive an unauthorized response, we try to refresh the token
            originalCall.response.run {
                val updatedIdToken = provider.refreshToken().getOrNull()
                if (status == HttpStatusCode.Companion.Unauthorized && updatedIdToken != null) {
                    request.addAuthToken(updatedIdToken)
                    proceed(request)
                } else {
                    originalCall
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

// private fun HttpClient.setupAuth(authTokenProvider: AuthTokenProvider) = apply {
//    plugin(HttpSend).intercept { originalRequest ->
//        val idToken = authTokenProvider.getTokenFlow().value
//            ?: authTokenProvider.refreshToken().getOrNull()
//
//        if (idToken != null) {
//            originalRequest.url {
//                parameters.append("auth", idToken)
//            }
//        }
//
//        execute(originalRequest)
//    }
// }
