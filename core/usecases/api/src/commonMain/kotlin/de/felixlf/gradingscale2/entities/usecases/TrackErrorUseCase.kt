package de.felixlf.gradingscale2.entities.usecases

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
