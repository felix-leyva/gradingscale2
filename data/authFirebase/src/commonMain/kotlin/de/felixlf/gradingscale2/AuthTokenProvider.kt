package de.felixlf.gradingscale2

import kotlinx.coroutines.flow.StateFlow

interface AuthTokenProvider {
    fun getTokenFlow(): StateFlow<String?>

    suspend fun refreshToken(): Result<String>
}
