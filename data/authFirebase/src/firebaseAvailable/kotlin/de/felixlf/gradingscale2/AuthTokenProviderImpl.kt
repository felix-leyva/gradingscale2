package de.felixlf.gradingscale2

import de.felixlf.gradingscale2.entities.network.AuthTokenProvider
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

internal class AuthTokenProviderImpl(
    private val auth: FirebaseAuth = Firebase.auth,
    scope: CoroutineScope,
) : AuthTokenProvider {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val tokenFlow =
        auth.authStateChanged
            .mapLatest { user ->
                val tokenResult = user?.getIdTokenResult(false)
                auth.signInAnonymously()
                when {
                    tokenResult == null -> user?.getIdToken(true)
                    else -> tokenResult.token
                }
            }.onStart { auth.signInAnonymously() }
            .stateIn(scope, started = SharingStarted.Lazily, initialValue = null)

    override fun getTokenFlow(): StateFlow<String?> = tokenFlow

    override suspend fun refreshToken(): Result<String> =
        auth.currentUser?.getIdToken(true)?.let { Result.success(it) }
            ?: Result.failure(Exception("User is not authenticated"))
}
