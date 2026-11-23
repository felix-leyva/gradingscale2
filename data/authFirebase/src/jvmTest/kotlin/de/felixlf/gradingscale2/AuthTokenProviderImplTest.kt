package de.felixlf.gradingscale2

import app.cash.turbine.test
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthTokenProviderImplTest {

    private class FakeFirebaseUserGateway(
        var cachedToken: String? = "cached-token",
        var freshToken: String? = "fresh-token",
        var onGetToken: ((forceRefresh: Boolean) -> Unit)? = null,
    ) : FirebaseUserGateway {
        override suspend fun getIdToken(forceRefresh: Boolean): String? {
            onGetToken?.invoke(forceRefresh)
            return if (forceRefresh) freshToken else cachedToken
        }
    }

    private class FakeFirebaseAuthGateway(
        initialUser: FirebaseUserGateway? = null,
    ) : FirebaseAuthGateway {
        override var currentUser: FirebaseUserGateway? = initialUser
        private val _idTokenChanged = MutableSharedFlow<FirebaseUserGateway?>(replay = 1)
        var signInAnonymouslyCalls: Int = 0
        var signInAnonymouslyHandler: suspend () -> FirebaseUserGateway? = { currentUser }

        init {
            _idTokenChanged.tryEmit(initialUser)
        }

        override val idTokenChanged: Flow<FirebaseUserGateway?> = _idTokenChanged

        suspend fun emitIdTokenOnly(user: FirebaseUserGateway?) {
            currentUser = user
            _idTokenChanged.emit(user)
        }

        override suspend fun signInAnonymously(): FirebaseUserGateway? {
            signInAnonymouslyCalls++
            return signInAnonymouslyHandler().also { signedInUser ->
                if (signedInUser != null) {
                    currentUser = signedInUser
                    _idTokenChanged.emit(signedInUser)
                }
            }
        }
    }

    @Test
    fun refreshTokenUpdatesTokenFlowWithNewToken() = runTest {
        val user = FakeFirebaseUserGateway(cachedToken = "expired-token", freshToken = "fresh-token")
        val fakeAuth = FakeFirebaseAuthGateway(initialUser = user)
        val provider = AuthTokenProviderImpl(auth = fakeAuth, scope = backgroundScope)

        provider.getTokenFlow().test {
            // Wait until initial token is observed (either null then expired-token or directly expired-token)
            var current = awaitItem()
            if (current == null) current = awaitItem()
            assertEquals("expired-token", current)

            val refreshed = provider.refreshToken()
            assertTrue(refreshed.isSuccess)
            assertEquals("fresh-token", refreshed.getOrNull())
            advanceUntilIdle()
            assertEquals("fresh-token", provider.getTokenFlow().value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun idTokenChangedRotationUpdatesTokenFlow() = runTest {
        val userV1 = FakeFirebaseUserGateway(cachedToken = "token-v1")
        val userV2 = FakeFirebaseUserGateway(cachedToken = "rotated-token")
        val fakeAuth = FakeFirebaseAuthGateway(initialUser = userV1)
        val provider = AuthTokenProviderImpl(auth = fakeAuth, scope = backgroundScope)

        provider.getTokenFlow().test {
            var current = awaitItem()
            if (current == null) current = awaitItem()
            assertEquals("token-v1", current)

            fakeAuth.emitIdTokenOnly(userV2)
            assertEquals("rotated-token", awaitItem())
            assertEquals("rotated-token", provider.getTokenFlow().value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun refreshTokenRecoversFromOfflineStartupWhenCurrentUserIsNull() = runTest {
        val fakeAuth = FakeFirebaseAuthGateway(initialUser = null)
        var isOffline = true
        fakeAuth.signInAnonymouslyHandler = {
            if (isOffline) {
                throw IllegalStateException("offline")
            } else {
                FakeFirebaseUserGateway(cachedToken = "recovered-cached", freshToken = "recovered-token")
            }
        }
        val provider = AuthTokenProviderImpl(auth = fakeAuth, scope = backgroundScope)

        provider.getTokenFlow().test {
            assertEquals(null, awaitItem())
            advanceUntilIdle()
            assertEquals(null, provider.getTokenFlow().value)

            isOffline = false
            val refreshed = provider.refreshToken()
            advanceUntilIdle()

            assertTrue(refreshed.isSuccess)
            assertEquals("recovered-token", refreshed.getOrNull())
            assertEquals("recovered-token", provider.getTokenFlow().value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun nonNetworkExceptionDuringTokenFetchDoesNotTerminateTokenFlow() = runTest {
        val failingUser = FakeFirebaseUserGateway(
            onGetToken = { throw IllegalStateException("auth error") },
        )
        val healthyUser = FakeFirebaseUserGateway(cachedToken = "later-token")
        val fakeAuth = FakeFirebaseAuthGateway(initialUser = failingUser)
        val provider = AuthTokenProviderImpl(auth = fakeAuth, scope = backgroundScope)

        provider.getTokenFlow().test {
            assertEquals(null, awaitItem())
            advanceUntilIdle()

            fakeAuth.emitIdTokenOnly(healthyUser)
            assertEquals("later-token", awaitItem())
            assertEquals("later-token", provider.getTokenFlow().value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun refreshTokenRethrowsCancellationException() = runTest {
        val user = FakeFirebaseUserGateway(
            onGetToken = { throw CancellationException("cancelled") },
        )
        val fakeAuth = FakeFirebaseAuthGateway(initialUser = user)
        val provider = AuthTokenProviderImpl(auth = fakeAuth, scope = backgroundScope)

        assertFailsWith<CancellationException> {
            provider.refreshToken()
        }
    }

    @Test
    fun doesNotCallSignInAnonymouslyWhenCurrentUserAlreadyExists() = runTest {
        val existingUser = FakeFirebaseUserGateway(cachedToken = "existing-token")
        val fakeAuth = FakeFirebaseAuthGateway(initialUser = existingUser)
        val provider = AuthTokenProviderImpl(auth = fakeAuth, scope = backgroundScope)

        provider.getTokenFlow().test {
            var current = awaitItem()
            if (current == null) current = awaitItem()
            advanceUntilIdle()
            assertEquals("existing-token", current)
            assertEquals(0, fakeAuth.signInAnonymouslyCalls)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
