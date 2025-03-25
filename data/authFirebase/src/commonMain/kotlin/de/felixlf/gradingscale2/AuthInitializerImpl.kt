package de.felixlf.gradingscale2

import de.felixlf.gradingscale2.entities.network.AuthInitializer

internal expect class AuthInitializerImpl() : AuthInitializer {
    override fun invoke()
}

internal class CommonAuthInitializerImpl : AuthInitializer {
    override operator fun invoke() {
        // Do nothing: GSM is initialized by the gradle plugin, except in the desktop target
    }
}
