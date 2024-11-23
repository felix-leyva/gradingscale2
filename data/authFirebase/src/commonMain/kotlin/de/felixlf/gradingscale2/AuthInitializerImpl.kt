package de.felixlf.gradingscale2

fun interface AuthInitializer {
    operator fun invoke()
}

internal expect class AuthInitializerImpl() : AuthInitializer

internal class CommonAuthInitializerImpl : AuthInitializer {
    override operator fun invoke() {
        // Do nothing: GSM is initialized by the gradle plugin, except in the desktop target
    }
}
