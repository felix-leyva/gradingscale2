package de.felixlf.gradingscale2

import de.felixlf.gradingscale2.entities.network.AuthTokenProvider
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal interface FirebaseUserGateway {
    suspend fun getIdToken(forceRefresh: Boolean): String?
}

internal interface FirebaseAuthGateway {
    val currentUser: FirebaseUserGateway?
    val idTokenChanged: Flow<FirebaseUserGateway?>

    suspend fun signInAnonymously(): FirebaseUserGateway?
}

private class GitLiveFirebaseUserGateway(
    private val user: FirebaseUser,
) : FirebaseUserGateway {
    override suspend fun getIdToken(forceRefresh: Boolean): String? = user.getIdToken(forceRefresh)
}

private class GitLiveFirebaseAuthGateway(
    private val auth: FirebaseAuth,
) : FirebaseAuthGateway {
    override val currentUser: FirebaseUserGateway?
        get() = auth.currentUser?.let(::GitLiveFirebaseUserGateway)

    override val idTokenChanged: Flow<FirebaseUserGateway?>
        get() = auth.idTokenChanged.map { it?.let(::GitLiveFirebaseUserGateway) }

    override suspend fun signInAnonymously(): FirebaseUserGateway? =
        auth.signInAnonymously().user?.let(::GitLiveFirebaseUserGateway)
}

internal class AuthTokenProviderImpl(
    private val auth: FirebaseAuthGateway,
    private val scope: CoroutineScope,
) : AuthTokenProvider {
    constructor(
        auth: FirebaseAuth = Firebase.auth,
        scope: CoroutineScope,
    ) : this(GitLiveFirebaseAuthGateway(auth), scope)

    private val authMutex = Mutex()
    private val _tokenFlow = MutableStateFlow<String?>(null)
    private val tokenFlow: StateFlow<String?> = _tokenFlow.asStateFlow()

    private val collectionJob: Job by lazy {
        scope.launch {
            val initialUser = auth.currentUser ?: ensureAnonymousSignIn()
            if (initialUser != null) {
                userToTokenResult(initialUser)?.let { _tokenFlow.value = it }
            }

            auth.idTokenChanged
                .catch { error ->
                    if (error is CancellationException) throw error
                    Napier.e("Error observing Firebase idTokenChanged flow", error)
                }
                .collectLatest { user ->
                    if (user != null) {
                        userToTokenResult(user)?.let { token ->
                            _tokenFlow.value = token
                        }
                    } else if (auth.currentUser == null) {
                        _tokenFlow.value = null
                    }
                }
        }
    }

    private fun ensureStarted(): Job = collectionJob

    override fun getTokenFlow(): StateFlow<String?> {
        ensureStarted()
        return tokenFlow
    }

    override suspend fun refreshToken(): Result<String> {
        ensureStarted()
        return authMutex.withLock {
            try {
                val user = auth.currentUser ?: auth.signInAnonymously()
                val refreshedToken = user?.getIdToken(true)
                    ?: throw IllegalStateException("User is not authenticated")
                _tokenFlow.value = refreshedToken
                Result.success(refreshedToken)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Napier.e("Failed to refresh Firebase ID token", e)
                Result.failure(e)
            }
        }
    }

    private suspend fun ensureAnonymousSignIn(): FirebaseUserGateway? = authMutex.withLock {
        val existingUser = auth.currentUser
        if (existingUser != null) return@withLock existingUser
        return@withLock try {
            auth.signInAnonymously()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Napier.e("Failed initial anonymous Firebase sign-in", e)
            null
        }
    }

    private suspend fun userToTokenResult(user: FirebaseUserGateway): String? = try {
        user.getIdToken(false) ?: user.getIdToken(true)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Napier.e("Failed on conversion of the user token", e)
        null
    }
}
