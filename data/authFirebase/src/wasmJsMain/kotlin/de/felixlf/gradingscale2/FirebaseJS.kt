@file:OptIn(ExperimentalWasmJsInterop::class)

package de.felixlf.gradingscale2

import kotlinx.coroutines.await
import kotlin.js.Promise as JsPromise

// External declarations for Firebase JS SDK loaded via CDN
external interface FirebaseApp : JsAny {
    val name: String
    val options: JsAny
}

external interface FirebaseAuth : JsAny {
    fun signInAnonymously(): JsPromise<UserCredential>
    fun onAuthStateChanged(callback: (FirebaseUser?) -> Unit): () -> Unit
    val currentUser: FirebaseUser?
}

external interface UserCredential : JsAny {
    val user: FirebaseUser
}

external interface FirebaseUser : JsAny {
    val uid: String
    fun getIdToken(forceRefresh: Boolean = definedExternally): JsPromise<JsString>
}

external interface FirebaseAnalytics : JsAny {
    fun logEvent(eventName: String, eventParams: JsAny? = definedExternally)
}

// Package-level functions required for js() usage in Kotlin/WASM
fun getFirebaseApp(): FirebaseApp? = js(
    "(typeof firebase !== 'undefined' && firebase.apps && firebase.apps.length > 0) ? firebase.apps[0] : null",
)
fun getFirebaseAuth(): FirebaseAuth? = js("(typeof firebase !== 'undefined' && firebase.auth) ? firebase.auth() : null")
fun getFirebaseAnalytics(): FirebaseAnalytics? = js("(typeof firebase !== 'undefined' && firebase.analytics) ? firebase.analytics() : null")
fun isFirebaseInitialized(): Boolean = js("!!(typeof firebase !== 'undefined' && firebase.apps && firebase.apps.length > 0)")

// Helper functions to create and manipulate JavaScript objects
fun createJsObject(): JsAny = js("({})")
fun setJsProperty(obj: JsAny, key: String, value: String): Unit = js("obj[key] = value")
fun setJsNumberProperty(obj: JsAny, key: String, value: Int): Unit = js("obj[key] = value")
fun setJsBooleanProperty(obj: JsAny, key: String, value: Boolean): Unit = js("obj[key] = value")

// Console access - using external declaration
external val console: Console

external interface Console : JsAny {
    fun log(vararg messages: JsAny?)
    fun error(vararg messages: JsAny?)
    fun warn(vararg messages: JsAny?)
}

// Logging helpers
fun logToConsole(message: String) {
    console.log(message.toJsString())
}

fun errorToConsole(message: String, error: String? = null) {
    if (error != null) {
        console.error(message.toJsString(), error.toJsString())
    } else {
        console.error(message.toJsString())
    }
}

// Extension function for Promise handling
suspend fun <T : JsAny?> JsPromise<T>.awaitJs(): T = this.await()

// Helper to catch JS exceptions
inline fun <T> catchJs(block: () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: Throwable) {
        Result.failure(e)
    }
}
