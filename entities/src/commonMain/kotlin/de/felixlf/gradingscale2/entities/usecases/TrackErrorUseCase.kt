package de.felixlf.gradingscale2.entities.usecases

import io.github.aakira.napier.Napier

/**
 * Tracks an error to the analytics service
 */
fun interface TrackErrorUseCase {
    /**
     * Tracks an error to the analytics service
     * @param methodCall reference of the calling method
     * @param message error message from the API or method
     * @param code optional error code from the API
     */
    operator fun invoke(methodCall: String, message: String, code: Int?)
}

internal class TrackErrorUseCaseImpl : TrackErrorUseCase {
    override fun invoke(methodCall: String, message: String, code: Int?) {
        // TODO: add an implementation using tracker service
        Napier.e("$methodCall: $message - $code")
    }
}
