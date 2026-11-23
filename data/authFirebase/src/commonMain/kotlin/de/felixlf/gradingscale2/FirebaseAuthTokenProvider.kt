package de.felixlf.gradingscale2

import de.felixlf.gradingscale2.entities.network.AuthTokenProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Platform independent Firebase authentication seam.
 *
 * Implementations adapt a concrete Firebase SDK (JS interop, GitLive, ...) so the token provider can
 * be unit tested without a browser or a live Firebase project.
 */
internal interface AuthUserGateway {
    val uid: String

    suspend fun getIdToken(forceRefresh: Boolean): String?
}

internal interface AuthGateway {
    val currentUser: AuthUserGateway?

    /**
     * Observes auth state changes and returns an unsubscribe callback. Implementations are expected
     * to invoke [onUserChanged] immediately with the current user (or `null` when signed out).
     */
    fun observeAuthState(onUserChanged: (AuthUserGateway?) -> Unit): () -> Unit

    suspend fun signInAnonymously(): AuthUserGateway?
}

/**
 * Publishes the Firebase ID token, or `null` while there is no authenticated user.
 *
 * There is deliberately no synthetic/placeholder token: without Firebase (or when Firebase fails)
 * the flow stays `null` and [refreshToken] returns a failed [Result], so callers can decide how to
 * handle an unauthenticated request instead of silently sending a bogus token.
 *
 * [authProvider] is invoked on every start/refresh attempt, so a Firebase instance which only
 * becomes available after this provider was created is still picked up.
 */
internal class FirebaseAuthTokenProvider(
    private val scope: CoroutineScope,
    private val authProvider: () -> AuthGateway?,
    private val log: (String) -> Unit = {},
) : AuthTokenProvider {
    private val authMutex = Mutex()
    private val _tokenFlow = MutableStateFlow<String?>(null)

    /**
     * Guards token publication ordering only, so a slow non-forced fetch never blocks auth calls.
     * Each fetch takes a [fetchSequence] before it starts and may only publish while it is still the
     * newest sequence, so a slow fetch can never overwrite a newer one or a forced refresh.
     */
    private val publicationMutex = Mutex()
    private var fetchSequence = 0L
    private var publishedSequence = 0L

    /** UID of the user the currently published token belongs to, or `null` when none is published. */
    private var publishedUid: String? = null
    private var listeningJob: Job? = null
    private var listenerAttached = false

    init {
        ensureListening()
    }

    override fun getTokenFlow(): StateFlow<String?> {
        // Re-attempts the listener when Firebase was unavailable on a previous attempt.
        ensureListening()
        return _tokenFlow.asStateFlow()
    }

    override suspend fun refreshToken(): Result<String> {
        ensureListening()
        return authMutex.withLock {
            try {
                val auth = authProvider() ?: throw IllegalStateException("Firebase Auth is not available")
                val user = auth.currentUser
                    ?: auth.signInAnonymously()
                    ?: throw IllegalStateException("User is not authenticated")
                // Always force a server side refresh: the cached token is exactly what got rejected.
                val token = user.getIdToken(forceRefresh = true)
                    ?: throw IllegalStateException("Firebase returned an empty ID token")
                // Wins over any listener fetch which may have started before this refresh, but never
                // over a newer auth state: if the session changed while refreshing, the token belongs
                // to a user who is no longer current and must not be published.
                publishForced(token, user.uid)
                Result.success(token)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                // Deliberately Throwable: the Wasm/JS SDK throws kotlin.js.JsException, which is a
                // Throwable but not an Exception, and must not escape the Result contract.
                log("Failed to refresh Firebase ID token: $e")
                Result.failure(e)
            }
        }
    }

    private fun ensureListening() {
        if (listenerAttached) return
        if (listeningJob?.isActive == true) return
        listeningJob = scope.launch { listen() }
    }

    private suspend fun listen() {
        val auth = authProvider()
        if (auth == null) {
            log("Firebase Auth is not available, no token will be published")
            return
        }

        val shouldPublishInitialToken = authMutex.withLock {
            if (listenerAttached) {
                false
            } else {
                auth.observeAuthState { user ->
                    scope.launch { onAuthStateChanged(auth, user) }
                }
                listenerAttached = true
                true
            }
        }
        if (!shouldPublishInitialToken) return

        val sequence = beginFetch()
        val initialUser = auth.currentUser ?: signInIfNeeded(auth)
        if (initialUser == null) {
            // No session can be established: never leave a token from a previous run published.
            publishIfNewest(sequence, uid = null, token = null)
            return
        }
        publishIfNewest(sequence, initialUser.uid, tokenOrNull(initialUser, forceRefresh = false))
    }

    private suspend fun onAuthStateChanged(auth: AuthGateway, user: AuthUserGateway?) {
        val sequence = beginFetch()
        if (user == null) {
            // Clear before re-authenticating: sign-out must not leave a stale token published while
            // [signInIfNeeded] waits for authMutex (for example behind an in-flight forced refresh).
            publishIfNewest(sequence, uid = null, token = null)
        }
        val authenticatedUser = user ?: signInIfNeeded(auth)
        if (authenticatedUser == null) {
            // Signed out and re-authentication failed: a stale token must not outlive the session.
            publishIfNewest(sequence, uid = null, token = null)
            return
        }
        publishIfNewest(sequence, authenticatedUser.uid, tokenOrNull(authenticatedUser, forceRefresh = false))
    }

    /** Takes the publication sequence for a fetch that is about to start. */
    private suspend fun beginFetch(): Long = publicationMutex.withLock { ++fetchSequence }

    /**
     * Publishes a forced refresh with a fresh sequence, so every fetch which started earlier can no
     * longer publish: a forced refresh always wins over in-flight listener fetches.
     *
     * It must not win over a newer auth state, though. If the current user is no longer [uid], the
     * refreshed token belongs to a session that ended (or to a different user) while the refresh was
     * in flight, so it is dropped instead of overwriting the new session's token. When that leaves
     * no current session at all, the previously published token is cleared right away.
     */
    private suspend fun publishForced(token: String, uid: String) {
        publicationMutex.withLock {
            val currentUid = authProvider()?.currentUser?.uid
            if (currentUid != uid) {
                if (currentUid == null && publishedUid != null) {
                    publishedUid = null
                    _tokenFlow.value = null
                }
                return@withLock
            }
            publishedSequence = ++fetchSequence
            publishedUid = uid
            _tokenFlow.value = token
        }
    }

    /**
     * Publishes a fetch result only while it is still the newest fetch for a user who is still the
     * current one. A `null` token for a different [uid] than the published one clears the previous
     * user's token; a `null` token for the same user is a transient fetch failure and keeps the
     * already published token.
     */
    private suspend fun publishIfNewest(sequence: Long, uid: String?, token: String?) {
        publicationMutex.withLock {
            if (authProvider()?.currentUser?.uid != uid) return@withLock
            if (sequence < publishedSequence) return@withLock
            if (token == null && uid != null && uid == publishedUid) return@withLock
            publishedSequence = sequence
            publishedUid = if (token == null) null else uid
            _tokenFlow.value = token
        }
    }

    /**
     * The single path through which an anonymous session is established. Re-checks [currentUser]
     * under the lock so concurrent initialisation (listener + startup) signs in only once.
     */
    private suspend fun signInIfNeeded(auth: AuthGateway): AuthUserGateway? = authMutex.withLock {
        auth.currentUser?.let { return@withLock it }
        try {
            auth.signInAnonymously()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            log("Anonymous sign-in failed: $e")
            null
        }
    }

    private suspend fun tokenOrNull(user: AuthUserGateway, forceRefresh: Boolean): String? =
        try {
            user.getIdToken(forceRefresh)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            log("Failed to obtain Firebase ID token: $e")
            null
        }
}
