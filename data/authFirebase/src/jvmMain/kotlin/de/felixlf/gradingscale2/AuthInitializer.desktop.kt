@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2

import android.app.Application
import com.google.firebase.FirebasePlatform
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.auth.AuthResult
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.initialize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal actual class AuthInitializerImpl : AuthInitializer {
    actual override operator fun invoke() {
        FirebasePlatform.initializeFirebasePlatform(
            object : FirebasePlatform() {
                val storage = mutableMapOf<String, String>()

                override fun store(
                    key: String,
                    value: String,
                ) = storage.set(key, value)

                override fun retrieve(key: String) = storage[key]

                override fun clear(key: String) {
                    storage.remove(key)
                }

                override fun log(msg: String) = println(msg)
            },
        )

        val firebaseOptions =
            FirebaseOptions(
                projectId = "gradingscale-9380c",
                applicationId = "1:950128411615:android:2fd0fa5f1848b055193a6a",
                apiKey = "AIzaSyDVIRALTJYVFyGJTZmNDJ9Qk2x-Rx3gl8A",
            )
        Firebase.initialize(Application(), firebaseOptions)
        CoroutineScope(Dispatchers.IO).launch {
            testAuth()
        }
    }
}

private fun AuthResult.readable() =
    user?.apply {
        "User: $displayName, $email, $phoneNumber, $photoURL }"
    } ?: "null"

private suspend fun testAuth(): String {
    val result = Firebase.auth.signInAnonymously()
    val tok =
        Firebase.auth.authStateChanged
            .map { it?.getIdToken(false) }
            .first()
    println("tok: $tok")
//    val readable = result.readable()
//    println("Sign in result: $readable")
    val token = result.user?.getIdTokenResult(false)
    println("Token = \n${token?.token},\n ${token?.claims},\n ${token?.signInProvider}")
    return token?.token ?: ""
}