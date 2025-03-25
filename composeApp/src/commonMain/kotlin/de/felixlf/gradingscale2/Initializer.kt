package de.felixlf.gradingscale2

import de.felixlf.gradingscale2.entities.network.AuthInitializer

fun interface Initializer {
    operator fun invoke()
}

internal class InitializerImpl(
    private val authInitializer: AuthInitializer,
) : Initializer {
    override fun invoke() {
        authInitializer()
    }
}
