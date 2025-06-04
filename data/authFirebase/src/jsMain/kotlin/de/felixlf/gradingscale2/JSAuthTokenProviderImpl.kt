package de.felixlf.gradingscale2

import de.felixlf.gradingscale2.entities.network.AuthTokenProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class JSAuthTokenProviderImpl(
    private val scope: CoroutineScope,
) : AuthTokenProvider {
    private val tokenFlow = MutableStateFlow<String?>(null)
    private var unsubscribeAuth: (() -> Unit)? = null

    init {
        if (isFirebaseInitialized()) {
            setupAuthListener()
        } else {
            // Fallback for when Firebase is not available
            // Set synchronously to avoid race conditions in tests
            tokenFlow.value = "js-anonymous-token-${kotlin.random.Random.nextInt(10000)}"
        }
    }

    private fun setupAuthListener() {
        try {
            val auth = getFirebaseAuth()

            // Listen to auth state changes
            unsubscribeAuth = auth.onAuthStateChanged { user ->
                scope.launch {
                    if (user != null) {
                        try {
                            val token = user.getIdToken(false).await()
                            tokenFlow.value = token
                        } catch (e: Throwable) {
                            console.error("Error getting token", e)
                            tokenFlow.value = null
                        }
                    } else {
                        // User is signed out, try to sign in anonymously
                        try {
                            auth.signInAnonymously().await()
                        } catch (e: Throwable) {
                            console.error("Error signing in anonymously", e)
                        }
                    }
                }
            }

            // Sign in anonymously if no current user
            if (auth.currentUser == null) {
                scope.launch {
                    try {
                        auth.signInAnonymously().await()
                    } catch (e: Throwable) {
                        console.error("Initial anonymous sign-in failed", e)
                    }
                }
            }
        } catch (e: Throwable) {
            console.error("Error setting up auth listener", e)
        }
    }

    override fun getTokenFlow(): StateFlow<String?> = tokenFlow

    override suspend fun refreshToken(): Result<String> {
        return try {
            if (isFirebaseInitialized()) {
                val auth = getFirebaseAuth()
                val user = auth.currentUser
                if (user != null) {
                    val token = user.getIdToken(true).await()
                    tokenFlow.value = token
                    Result.success(token)
                } else {
                    Result.failure(Exception("No authenticated user"))
                }
            } else {
                // Fallback token generation
                val newToken = "js-anonymous-token-${kotlin.random.Random.nextInt(10000)}"
                tokenFlow.value = newToken
                Result.success(newToken)
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
