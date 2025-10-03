@file:OptIn(ExperimentalWasmJsInterop::class)

package de.felixlf.gradingscale2

import de.felixlf.gradingscale2.entities.network.AuthTokenProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class JSAuthTokenProviderImpl(
    private val scope: CoroutineScope,
) : AuthTokenProvider {
    private val tokenFlow = MutableStateFlow<String?>(null)
    private var unsubscribeAuth: (() -> Unit)? = null

    init {
        logToConsole("JSAuthTokenProviderImpl initialized for WasmJS")

        if (isFirebaseInitialized()) {
            logToConsole("Firebase detected, setting up auth listener")
            setupAuthListener()
        } else {
            logToConsole("Firebase not available, using fallback tokens")
            // Fallback for when Firebase is not available
            tokenFlow.value = "wasmjs-fallback-token-${kotlin.random.Random.nextInt(10000)}"
        }
    }

    private fun setupAuthListener() {
        try {
            val auth = getFirebaseAuth()
            if (auth == null) {
                errorToConsole("Failed to get Firebase Auth instance")
                tokenFlow.value = "wasmjs-fallback-token-${kotlin.random.Random.nextInt(10000)}"
                return
            }

            // Listen to auth state changes
            unsubscribeAuth = auth.onAuthStateChanged { user ->
                scope.launch {
                    if (user != null) {
                        try {
                            val token = getTokenFromUser(user)
                            tokenFlow.value = token
                        } catch (e: Throwable) {
                            errorToConsole("Error getting token", e.toString())
                            tokenFlow.value = "wasmjs-error-token-${kotlin.random.Random.nextInt(10000)}"
                        }
                    } else {
                        // User is signed out, try to sign in anonymously
                        try {
                            signInAnonymously(auth)
                        } catch (e: Throwable) {
                            errorToConsole("Error signing in anonymously", e.toString())
                            tokenFlow.value = "wasmjs-auth-failed-${kotlin.random.Random.nextInt(10000)}"
                        }
                    }
                }
            }

            // Sign in anonymously if no current user
            if (auth.currentUser == null) {
                scope.launch {
                    try {
                        signInAnonymously(auth)
                    } catch (e: Throwable) {
                        errorToConsole("Initial anonymous sign-in failed", e.toString())
                        tokenFlow.value = "wasmjs-init-failed-${kotlin.random.Random.nextInt(10000)}"
                    }
                }
            } else {
                // Get token from existing user
                scope.launch {
                    try {
                        val token = getTokenFromUser(auth.currentUser!!)
                        tokenFlow.value = token
                    } catch (e: Throwable) {
                        errorToConsole("Error getting initial token", e.toString())
                        tokenFlow.value = "wasmjs-initial-token-error-${kotlin.random.Random.nextInt(10000)}"
                    }
                }
            }
        } catch (e: Throwable) {
            errorToConsole("Error setting up auth listener", e.toString())
            // Fallback token
            tokenFlow.value = "wasmjs-setup-error-${kotlin.random.Random.nextInt(10000)}"
        }
    }

    private suspend fun signInAnonymously(auth: FirebaseAuth) {
        try {
            val result = auth.signInAnonymously().awaitJs()
            logToConsole("Anonymous sign-in successful for user: ${result.user.uid}")
        } catch (e: Throwable) {
            errorToConsole("Anonymous sign-in failed", e.toString())
            throw e
        }
    }

    private suspend fun getTokenFromUser(user: FirebaseUser): String {
        return try {
            val jsToken = user.getIdToken(false).awaitJs()
            val token = jsToken.toString()
            logToConsole("Successfully retrieved Firebase token")
            token
        } catch (e: Throwable) {
            errorToConsole("Failed to get token from Firebase user", e.toString())
            throw e
        }
    }

    override fun getTokenFlow(): StateFlow<String?> = tokenFlow

    override suspend fun refreshToken(): Result<String> {
        return try {
            if (isFirebaseInitialized()) {
                val auth = getFirebaseAuth()
                val user = auth?.currentUser
                if (user != null) {
                    val token = getTokenFromUser(user)
                    tokenFlow.value = token
                    Result.success(token)
                } else {
                    // Try to sign in anonymously first
                    if (auth != null) {
                        signInAnonymously(auth)
                        // After sign in, try to get token again
                        val newUser = auth.currentUser
                        if (newUser != null) {
                            val token = getTokenFromUser(newUser)
                            tokenFlow.value = token
                            Result.success(token)
                        } else {
                            Result.failure(Exception("Failed to sign in anonymously"))
                        }
                    } else {
                        Result.failure(Exception("Firebase Auth not available"))
                    }
                }
            } else {
                // Fallback token generation
                val newToken = "wasmjs-refresh-fallback-${kotlin.random.Random.nextInt(10000)}"
                tokenFlow.value = newToken
                Result.success(newToken)
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
