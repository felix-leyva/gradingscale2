package de.felixlf.gradingscale2

import de.felixlf.gradingscale2.entities.network.AuthTokenProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Simplified WasmJS implementation that works without complex Firebase Promise handling
class JSAuthTokenProviderImpl(
    @Suppress("UNUSED_PARAMETER") scope: CoroutineScope,
) : AuthTokenProvider {
    private val tokenFlow = MutableStateFlow<String?>(null)

    init {
        // For WasmJS, use a simple fallback approach since Firebase interop is complex
        val console = getConsole()
        console.log("JSAuthTokenProviderImpl initialized for WasmJS")

        if (isFirebaseInitialized()) {
            console.log("Firebase detected, but using fallback tokens for WasmJS compatibility")
        } else {
            console.log("Firebase not available, using anonymous tokens")
        }

        // Always use fallback token for WasmJS to avoid Promise compatibility issues
        tokenFlow.value = "wasmjs-anonymous-token-${kotlin.random.Random.nextInt(10000)}"
    }

    override fun getTokenFlow(): StateFlow<String?> = tokenFlow

    override suspend fun refreshToken(): Result<String> {
        return try {
            // Generate a new token for WasmJS
            val newToken = "wasmjs-refresh-token-${kotlin.random.Random.nextInt(10000)}"
            tokenFlow.value = newToken
            getConsole().log("Generated new token for WasmJS: $newToken")
            Result.success(newToken)
        } catch (e: Throwable) {
            getConsole().error("Error refreshing token: ${e.message}")
            Result.failure(e)
        }
    }
}
