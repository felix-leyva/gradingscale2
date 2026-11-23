package de.felixlf.gradingscale2

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for the Firebase auth token state machine.
 *
 * Firebase is faked through [AuthGateway]/[AuthUserGateway] so these tests run without a browser,
 * a network connection or a live Firebase project.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseAuthTokenProviderTest {

    private class FakeAuthUser(
        override val uid: String = "fake-uid",
        var cachedToken: String? = "cached-token",
        var freshToken: String? = "fresh-token",
    ) : AuthUserGateway {
        val tokenRequests = mutableListOf<Boolean>()
        var failure: Throwable? = null

        /** When set, non-forced token reads suspend until released, emulating a slow cache read. */
        var nonForcedGate: CompletableDeferred<Unit>? = null

        /** When set, forced token reads suspend until released, emulating a slow server refresh. */
        var forcedGate: CompletableDeferred<Unit>? = null

        override suspend fun getIdToken(forceRefresh: Boolean): String? {
            tokenRequests += forceRefresh
            if (forceRefresh) forcedGate?.await() else nonForcedGate?.await()
            failure?.let { throw it }
            return if (forceRefresh) freshToken else cachedToken
        }
    }

    private class FakeAuthGateway(
        initialUser: AuthUserGateway? = null,
    ) : AuthGateway {
        override var currentUser: AuthUserGateway? = initialUser
        var signInCalls: Int = 0
        var observeCalls: Int = 0
        var signInHandler: suspend () -> AuthUserGateway? = {
            FakeAuthUser().also { currentUser = it }
        }
        private val listeners = mutableListOf<(AuthUserGateway?) -> Unit>()

        override fun observeAuthState(onUserChanged: (AuthUserGateway?) -> Unit): () -> Unit {
            observeCalls++
            listeners += onUserChanged
            // Firebase fires the callback immediately with the current user (null when signed out).
            onUserChanged(currentUser)
            return { listeners -= onUserChanged }
        }

        override suspend fun signInAnonymously(): AuthUserGateway? {
            signInCalls++
            val user = signInHandler()
            if (user != null) {
                currentUser = user
                listeners.toList().forEach { it(user) }
            }
            return user
        }

        /** Simulates the auth SDK delivering a state change (sign-out, rotation, ...). */
        fun emitAuthState(user: AuthUserGateway?) {
            currentUser = user
            listeners.toList().forEach { it(user) }
        }
    }

    /** Scope driven by the test scheduler so [advanceUntilIdle] runs the provider's coroutines. */
    private fun TestScope.providerScope(): CoroutineScope = CoroutineScope(StandardTestDispatcher(testScheduler))

    @Test
    fun publishesNoTokenWhenFirebaseIsAbsent() = runTest {
        val provider = FirebaseAuthTokenProvider(providerScope(), authProvider = { null })

        advanceUntilIdle()

        assertNull(provider.getTokenFlow().value)
    }

    @Test
    fun refreshTokenFailsWhenFirebaseIsAbsent() = runTest {
        val provider = FirebaseAuthTokenProvider(providerScope(), authProvider = { null })

        val result = provider.refreshToken()
        advanceUntilIdle()

        assertTrue(result.isFailure, "Absent Firebase must not yield a successful synthetic token")
        assertNull(provider.getTokenFlow().value)
    }

    @Test
    fun refreshTokenForcesServerTokenRefresh() = runTest {
        val user = FakeAuthUser(cachedToken = "stale-token", freshToken = "fresh-token")
        val gateway = FakeAuthGateway(initialUser = user)
        val provider = FirebaseAuthTokenProvider(providerScope(), authProvider = { gateway })

        advanceUntilIdle()
        assertEquals("stale-token", provider.getTokenFlow().value)

        val result = provider.refreshToken()
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertEquals("fresh-token", result.getOrNull())
        assertEquals("fresh-token", provider.getTokenFlow().value)
        assertTrue(
            user.tokenRequests.lastOrNull() == true,
            "refreshToken must call getIdToken(forceRefresh = true), got ${user.tokenRequests}",
        )
    }

    @Test
    fun initialAnonymousSignInRunsOnlyOnce() = runTest {
        val gateway = FakeAuthGateway(initialUser = null)
        val provider = FirebaseAuthTokenProvider(providerScope(), authProvider = { gateway })

        provider.getTokenFlow()
        advanceUntilIdle()

        assertEquals(1, gateway.signInCalls, "Anonymous sign-in must be serialized through one path")
    }

    @Test
    fun refreshTokenRethrowsCancellation() = runTest {
        val user = FakeAuthUser().apply { failure = CancellationException("cancelled") }
        val gateway = FakeAuthGateway(initialUser = user)
        val provider = FirebaseAuthTokenProvider(providerScope(), authProvider = { gateway })

        advanceUntilIdle()

        assertFailsWith<CancellationException> {
            provider.refreshToken()
        }
    }

    @Test
    fun publishesNoTokenWhenTokenFetchFails() = runTest {
        val user = FakeAuthUser().apply { failure = IllegalStateException("auth error") }
        val gateway = FakeAuthGateway(initialUser = user)
        val provider = FirebaseAuthTokenProvider(providerScope(), authProvider = { gateway })

        provider.getTokenFlow()
        advanceUntilIdle()

        assertNull(provider.getTokenFlow().value)
    }

    @Test
    fun recoversWhenFirebaseBecomesAvailableLater() = runTest {
        var gateway: AuthGateway? = null
        val provider = FirebaseAuthTokenProvider(providerScope(), authProvider = { gateway })

        provider.getTokenFlow()
        advanceUntilIdle()
        assertNull(provider.getTokenFlow().value)

        gateway = FakeAuthGateway(initialUser = FakeAuthUser(cachedToken = "late-token"))
        provider.getTokenFlow()
        advanceUntilIdle()

        assertEquals("late-token", provider.getTokenFlow().value)
    }

    @Test
    fun repeatedTokenFlowAccessDoesNotRegisterDuplicateAuthListeners() = runTest {
        val gateway = FakeAuthGateway(initialUser = FakeAuthUser())
        val provider = FirebaseAuthTokenProvider(providerScope(), authProvider = { gateway })

        repeat(3) { provider.getTokenFlow() }
        advanceUntilIdle()

        assertEquals(1, gateway.observeCalls)
        assertEquals(0, gateway.signInCalls)
    }

    /**
     * A `kotlin.js.JsException` (Firebase JS SDK failures on Wasm) extends `Throwable` but not
     * `Exception`. refreshToken must still honour its `Result` contract instead of throwing.
     */
    @Test
    fun refreshTokenReturnsFailureForNonExceptionThrowable() = runTest {
        val user = FakeAuthUser()
        val gateway = FakeAuthGateway(initialUser = user)
        val provider = FirebaseAuthTokenProvider(providerScope(), authProvider = { gateway })

        advanceUntilIdle()
        user.failure = Throwable("non-exception failure from JS interop")

        val result = provider.refreshToken()
        advanceUntilIdle()

        assertTrue(result.isFailure, "A non-Exception Throwable must be reported as a failed Result")
    }

    /** A non-Exception Throwable during a listener token fetch must not tear down the listener. */
    @Test
    fun nonExceptionThrowableDuringListenerFetchDoesNotTerminateTokenFlow() = runTest {
        val failingUser = FakeAuthUser().apply { failure = Throwable("non-exception failure") }
        val gateway = FakeAuthGateway(initialUser = failingUser)
        val provider = FirebaseAuthTokenProvider(providerScope(), authProvider = { gateway })

        advanceUntilIdle()
        assertNull(provider.getTokenFlow().value)

        gateway.emitAuthState(FakeAuthUser(cachedToken = "recovered-token"))
        advanceUntilIdle()

        assertEquals("recovered-token", provider.getTokenFlow().value)
    }

    /** Signing out and failing to re-authenticate must not leave the previous token published. */
    @Test
    fun clearsPublishedTokenWhenSignedOutAndReauthenticationFails() = runTest {
        val gateway = FakeAuthGateway(initialUser = FakeAuthUser(cachedToken = "signed-in-token"))
        val provider = FirebaseAuthTokenProvider(providerScope(), authProvider = { gateway })

        advanceUntilIdle()
        assertEquals("signed-in-token", provider.getTokenFlow().value)

        gateway.signInHandler = { null }
        gateway.emitAuthState(null)
        advanceUntilIdle()

        assertNull(
            provider.getTokenFlow().value,
            "A stale token must not outlive the session; no authenticated user can be established",
        )
    }

    /** A slow non-forced listener fetch must not overwrite a newer force-refreshed token. */
    @Test
    fun slowerListenerFetchDoesNotOverwriteForcedRefresh() = runTest {
        val user = FakeAuthUser(cachedToken = "cached-token", freshToken = "fresh-token")
        val gateway = FakeAuthGateway(initialUser = user)
        val provider = FirebaseAuthTokenProvider(providerScope(), authProvider = { gateway })

        advanceUntilIdle()
        assertEquals("cached-token", provider.getTokenFlow().value)

        // A listener event starts a non-forced fetch that will complete much later.
        val gate = CompletableDeferred<Unit>()
        user.nonForcedGate = gate
        gateway.emitAuthState(user)
        advanceUntilIdle()

        // Meanwhile an explicit refresh publishes the fresh token.
        val result = provider.refreshToken()
        advanceUntilIdle()
        assertEquals("fresh-token", result.getOrNull())
        assertEquals("fresh-token", provider.getTokenFlow().value)

        // The stale listener fetch now completes and must be discarded.
        gate.complete(Unit)
        advanceUntilIdle()

        assertEquals(
            "fresh-token",
            provider.getTokenFlow().value,
            "A slower non-forced fetch must not overwrite a newer forced refresh",
        )
    }

    /**
     * A sign-out which fires while [FirebaseAuthTokenProvider.refreshToken] still holds the auth
     * mutex must clear the published token immediately, and the forced refresh must not re-publish
     * the signed-out session's token afterwards.
     */
    @Test
    fun signOutDuringForcedRefreshClearsTokenImmediatelyAndNeverRepublishesIt() = runTest {
        val userA = FakeAuthUser(uid = "uid-a", cachedToken = "token-a", freshToken = "fresh-a")
        val gateway = FakeAuthGateway(initialUser = userA)
        val provider = FirebaseAuthTokenProvider(providerScope(), authProvider = { gateway })

        advanceUntilIdle()
        assertEquals("token-a", provider.getTokenFlow().value)

        // Freeze the forced refresh so it is in flight, holding authMutex, during sign-out.
        val forcedGate = CompletableDeferred<Unit>()
        userA.forcedGate = forcedGate
        val refresh = async { provider.refreshToken() }
        advanceUntilIdle()

        // Sign-out arrives while refreshToken still holds authMutex; re-authentication fails.
        gateway.signInHandler = { null }
        gateway.emitAuthState(null)
        advanceUntilIdle()

        assertNull(
            provider.getTokenFlow().value,
            "Signing out must clear the published token immediately, even while a forced refresh is in flight",
        )

        forcedGate.complete(Unit)
        advanceUntilIdle()

        assertNull(
            provider.getTokenFlow().value,
            "A forced refresh started for the signed-out session must not re-publish its token",
        )
        assertTrue(refresh.isCompleted && refresh.await().isSuccess, "The forced refresh itself still resolves")
    }

    /**
     * When a different user signs in while a forced refresh for the previous user is in flight, the
     * new user's token must win even though its fetch took a lower publication sequence.
     */
    @Test
    fun newUserSignInDuringForcedRefreshIsNotDiscardedBySequenceInvalidation() = runTest {
        val userA = FakeAuthUser(uid = "uid-a", cachedToken = "token-a", freshToken = "fresh-a")
        val gateway = FakeAuthGateway(initialUser = userA)
        val provider = FirebaseAuthTokenProvider(providerScope(), authProvider = { gateway })

        advanceUntilIdle()
        assertEquals("token-a", provider.getTokenFlow().value)

        val forcedGate = CompletableDeferred<Unit>()
        userA.forcedGate = forcedGate
        val refresh = async { provider.refreshToken() }
        advanceUntilIdle()

        // Sign-out followed by a different user signing in, both while the refresh holds authMutex.
        val userB = FakeAuthUser(uid = "uid-b", cachedToken = "token-b")
        gateway.emitAuthState(null)
        gateway.emitAuthState(userB)
        advanceUntilIdle()

        forcedGate.complete(Unit)
        advanceUntilIdle()

        refresh.await()
        assertEquals(
            "token-b",
            provider.getTokenFlow().value,
            "The new user's token must not be discarded by the previous session's forced refresh sequence",
        )
    }

    /**
     * When the signed-in identity changes and the non-forced token fetch for the new user fails, the
     * previous user's token must not remain published.
     */
    @Test
    fun failedTokenFetchAfterIdentityChangeClearsPreviousUsersToken() = runTest {
        val userA = FakeAuthUser(uid = "uid-a", cachedToken = "token-a")
        val gateway = FakeAuthGateway(initialUser = userA)
        val provider = FirebaseAuthTokenProvider(providerScope(), authProvider = { gateway })

        advanceUntilIdle()
        assertEquals("token-a", provider.getTokenFlow().value)

        val userB = FakeAuthUser(uid = "uid-b", cachedToken = "token-b")
            .apply { failure = IllegalStateException("offline token fetch") }
        gateway.emitAuthState(userB)
        advanceUntilIdle()

        assertNull(
            provider.getTokenFlow().value,
            "A failed token fetch for the new identity must clear the previous user's token",
        )
    }
}
