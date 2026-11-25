@file:OptIn(ExperimentalWasmJsInterop::class)

package de.felixlf.gradingscale2

import de.felixlf.gradingscale2.entities.network.AuthTokenProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * WasmJS adapter over the Firebase JS SDK loaded via CDN.
 *
 * The token state machine lives in [FirebaseAuthTokenProvider]; this class only adapts the global
 * `firebase` object. When Firebase is not loaded the adapter returns `null`, so the provider
 * publishes no token instead of a synthetic placeholder.
 */
class JSAuthTokenProviderImpl(
    scope: CoroutineScope,
) : AuthTokenProvider {
    private val delegate = FirebaseAuthTokenProvider(scope, ::wasmFirebaseAuth)

    override fun getTokenFlow(): StateFlow<String?> = delegate.getTokenFlow()

    override suspend fun refreshToken(): Result<String> = delegate.refreshToken()
}

private fun wasmFirebaseAuth(): AuthGateway? {
    if (!isFirebaseInitialized()) return null
    val auth = getFirebaseAuth() ?: return null
    return JsFirebaseAuthGateway(auth)
}

private class JsFirebaseAuthGateway(
    private val auth: FirebaseAuth,
) : AuthGateway {
    override val currentUser: AuthUserGateway?
        get() = auth.currentUser?.let(::JsFirebaseUser)

    override fun observeAuthState(onUserChanged: (AuthUserGateway?) -> Unit): () -> Unit =
        auth.onAuthStateChanged { user -> onUserChanged(user?.let(::JsFirebaseUser)) }

    override suspend fun signInAnonymously(): AuthUserGateway? =
        JsFirebaseUser(auth.signInAnonymously().awaitJs().user)
}

private class JsFirebaseUser(
    private val user: FirebaseUser,
) : AuthUserGateway {
    override val uid: String get() = user.uid

    override suspend fun getIdToken(forceRefresh: Boolean): String? =
        user.getIdToken(forceRefresh).awaitJs().toString()
}
