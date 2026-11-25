@file:Suppress("ktlint:standard:filename")

package de.felixlf.gradingscale2

import android.app.Application
import com.google.firebase.FirebasePlatform
import de.felixlf.gradingscale2.entities.network.AuthInitializer
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import io.github.aakira.napier.Napier
import java.util.concurrent.ConcurrentHashMap

internal actual class AuthInitializerImpl : AuthInitializer {
    actual override operator fun invoke() {
        val firebaseOptions =
            FirebaseOptions(
                projectId = BuildResources.FIREBASE_PROJECT_ID,
                applicationId = BuildResources.FIREBASE_APP_ID,
                apiKey = BuildResources.FIREBASE_API_KEY,
            )
        initializeDesktopFirebase(firebaseOptions)
    }
}

internal class InMemoryFirebasePlatform : FirebasePlatform() {
    private val storage = ConcurrentHashMap<String, String>()

    override fun store(
        key: String,
        value: String,
    ) {
        storage[key] = value
    }

    override fun retrieve(key: String): String? = storage[key]

    override fun clear(key: String) {
        storage.remove(key)
    }

    override fun log(msg: String) {
        Napier.d(msg)
    }
}

private val desktopFirebaseInitLock = Any()
private val desktopFirebasePlatform = InMemoryFirebasePlatform()

internal fun initializeDesktopFirebase(options: FirebaseOptions) {
    synchronized(desktopFirebaseInitLock) {
        val application = Application()
        if (Firebase.apps(application).isNotEmpty()) return
        FirebasePlatform.initializeFirebasePlatform(desktopFirebasePlatform)
        Firebase.initialize(application, options)
    }
}
