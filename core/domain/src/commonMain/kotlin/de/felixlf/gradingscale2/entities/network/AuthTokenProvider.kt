package de.felixlf.gradingscale2.entities.network

import kotlinx.coroutines.flow.StateFlow

/**
 * Provides an authentication token.
 */
interface AuthTokenProvider {

    /**
     * Returns a flow of the authentication token.
     */
    fun getTokenFlow(): StateFlow<String?>

    /**
     * Refreshes the authentication token.
     */
    suspend fun refreshToken(): Result<String>
}
