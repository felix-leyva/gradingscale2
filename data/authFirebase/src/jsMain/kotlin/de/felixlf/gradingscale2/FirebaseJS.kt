package de.felixlf.gradingscale2

import kotlin.js.Promise

// External declarations for Firebase JS SDK loaded via CDN
external interface FirebaseAuthInstance {
    fun signInAnonymously(): Promise<UserCredential>
    fun onAuthStateChanged(callback: (User?) -> Unit): () -> Unit
    val currentUser: User?
}

external interface UserCredential {
    val user: User?
}

external interface User {
    val uid: String
    fun getIdToken(forceRefresh: Boolean = definedExternally): Promise<String>
}

external interface FirebaseAnalyticsInstance {
    fun logEvent(eventName: String, parameters: dynamic = definedExternally)
}

// Helper functions to access Firebase from global scope (loaded via CDN in index.html)
fun getFirebaseAuth(): FirebaseAuthInstance = js("firebase.auth()")
fun getFirebaseAnalytics(): FirebaseAnalyticsInstance = js("firebase.analytics()")
fun isFirebaseInitialized(): Boolean = js("typeof firebase !== 'undefined' && firebase.apps && firebase.apps.length > 0") as Boolean
