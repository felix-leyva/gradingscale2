package de.felixlf.gradingscale2

import de.felixlf.gradingscale2.entities.network.AuthTokenProvider
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseNetworkException
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
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
            .mapLatest(::userToTokenResult)
            .onStart {
                runCatching { userToTokenResult((auth.signInAnonymously().user)) }
                    .onFailure { Napier.e(it.stackTraceToString()) }
                    .onSuccess { emit(it)}
            }
            .catch {
                Napier.e("Error on getting tokenFlow")
                Napier.e(it.message ?: "", it)
                emit(null)
            }
            .stateIn(scope, started = SharingStarted.Lazily, initialValue = null)

    override fun getTokenFlow(): StateFlow<String?> = tokenFlow

    override suspend fun refreshToken(): Result<String> = runCatching {
        auth.currentUser?.getIdToken(true)
            ?: throw Exception("User is not authenticated")
    }

    private suspend fun userToTokenResult(user: FirebaseUser?): String? = try {
        val tokenResult = user?.getIdTokenResult(false)
        when {
            tokenResult == null -> user?.getIdToken(true)
            else -> tokenResult.token
        }
    } catch (fne: FirebaseNetworkException) {
        Napier.e("Failed on conversion of the user token", fne)
        null
    }

}
