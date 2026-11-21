package de.felixlf.gradingscale2.entities.usecases

import io.github.aakira.napier.Napier

internal class TrackErrorUseCaseImpl : TrackErrorUseCase {
    override fun invoke(methodCall: String, message: String, code: Int?) {
        // TODO: add an implementation using tracker service
        Napier.e("$methodCall: $message - $code")
    }
}
