package de.felixlf.gradingscale2.entities.usecases

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class FakeTrackerUseCase : TrackErrorUseCase {
    val reportedErrors = MutableStateFlow<TrackError?>(null)
    override fun invoke(methodCall: String, message: String, code: Int?) {
        reportedErrors.update { TrackError(methodCall, message, code) }
    }

    internal class TrackError(
        val methodCall: String,
        val message: String,
        val code: Int?,
    )
}
