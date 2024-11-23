package de.felixlf.gradingscale2

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
