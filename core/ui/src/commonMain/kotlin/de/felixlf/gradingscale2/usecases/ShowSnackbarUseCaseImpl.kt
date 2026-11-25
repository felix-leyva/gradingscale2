package de.felixlf.gradingscale2.usecases

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import de.felixlf.gradingscale2.entities.usecases.ShowSnackbarUseCase
import de.felixlf.gradingscale2.entities.util.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

internal class ShowSnackbarUseCaseImpl(
    private val snackbarHostState: SnackbarHostState,
    dispatcherProvider: DispatcherProvider,
) : ShowSnackbarUseCase {
    private val snackbarScope = CoroutineScope(SupervisorJob() + dispatcherProvider.immediate)

    override suspend fun invoke(
        message: StringResource,
        actionLabel: StringResource?,
        duration: ShowSnackbarUseCase.SnackbarDuration?,
    ): ShowSnackbarUseCase.SnackbarResult {
        val resolvedDuration = when (duration) {
            ShowSnackbarUseCase.SnackbarDuration.Long -> SnackbarDuration.Long
            else -> SnackbarDuration.Short
        }
        return showDetachedAsync(
            resolveMessage = { getString(message) },
            resolveActionLabel = { actionLabel?.let { getString(it) } },
            duration = resolvedDuration,
        )
    }

    internal suspend fun showDetached(
        message: String,
        actionLabel: String?,
        duration: SnackbarDuration,
    ): ShowSnackbarUseCase.SnackbarResult = showDetachedAsync(
        resolveMessage = { message },
        resolveActionLabel = { actionLabel },
        duration = duration,
    )

    private suspend fun showDetachedAsync(
        resolveMessage: suspend () -> String,
        resolveActionLabel: suspend () -> String?,
        duration: SnackbarDuration,
    ): ShowSnackbarUseCase.SnackbarResult {
        // Launch on an independent app-lived scope so transient dialog/screen dismissal cancels only awaiting.
        val deferred = snackbarScope.async {
            snackbarHostState.showSnackbar(
                message = resolveMessage(),
                actionLabel = resolveActionLabel(),
                duration = duration,
            )
        }
        return when (deferred.await()) {
            SnackbarResult.Dismissed -> ShowSnackbarUseCase.SnackbarResult.Dismissed
            SnackbarResult.ActionPerformed -> ShowSnackbarUseCase.SnackbarResult.ActionPerformed
        }
    }
}
