package de.felixlf.gradingscale2.usecases

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import de.felixlf.gradingscale2.entities.usecases.ShowSnackbarUseCase
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

internal class ShowSnackbarUseCaseImpl(
    private val snackbarHostState: SnackbarHostState,
) : ShowSnackbarUseCase {
    override suspend fun invoke(
        message: StringResource,
        actionLabel: StringResource?,
        duration: ShowSnackbarUseCase.SnackbarDuration?,
    ): ShowSnackbarUseCase.SnackbarResult {
        val duration = when (duration) {
            ShowSnackbarUseCase.SnackbarDuration.Long -> SnackbarDuration.Long
            else -> SnackbarDuration.Short
        }
        val result = snackbarHostState.showSnackbar(
            message = getString(message),
            actionLabel = actionLabel?.let { getString(it) },
            duration = duration,
        )
        return when (result) {
            SnackbarResult.Dismissed -> ShowSnackbarUseCase.SnackbarResult.Dismissed
            SnackbarResult.ActionPerformed -> ShowSnackbarUseCase.SnackbarResult.ActionPerformed
        }
    }
}
