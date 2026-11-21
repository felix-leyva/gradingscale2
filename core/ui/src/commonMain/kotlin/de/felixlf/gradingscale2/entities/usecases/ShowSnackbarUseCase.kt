package de.felixlf.gradingscale2.entities.usecases

import org.jetbrains.compose.resources.StringResource

/**
 * Use case to show a snackbar.
 */
fun interface ShowSnackbarUseCase {
    suspend operator fun invoke(
        message: StringResource,
        actionLabel: StringResource?,
        duration: SnackbarDuration?,
    ): SnackbarResult

    enum class SnackbarDuration {
        Short,
        Long,
    }

    enum class SnackbarResult {
        Dismissed,
        ActionPerformed,
    }
}
