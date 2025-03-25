package de.felixlf.gradingscale2.entities.network

/**
 * Initializes the authentication system.
 */
fun interface AuthInitializer {
    operator fun invoke()
}
