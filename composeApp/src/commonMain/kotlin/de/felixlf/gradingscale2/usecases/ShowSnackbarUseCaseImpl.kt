package de.felixlf.gradingscale2.usecases

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import de.felixlf.gradingscale2.entities.usecases.ShowSnackbarUseCase

internal class ShowSnackbarUseCaseImpl(
    private val snackbarHostState: SnackbarHostState,
) : ShowSnackbarUseCase {
    override suspend fun invoke(
        message: String,
        actionLabel: String?,
        duration: ShowSnackbarUseCase.SnackbarDuration?,
    ): ShowSnackbarUseCase.SnackbarResult {
        val duration = when (duration) {
            ShowSnackbarUseCase.SnackbarDuration.Long -> SnackbarDuration.Long
            else -> SnackbarDuration.Short
        }
        val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = duration,
        )
        return when (result) {
            SnackbarResult.Dismissed -> ShowSnackbarUseCase.SnackbarResult.Dismissed
            SnackbarResult.ActionPerformed -> ShowSnackbarUseCase.SnackbarResult.ActionPerformed
        }
    }
}
