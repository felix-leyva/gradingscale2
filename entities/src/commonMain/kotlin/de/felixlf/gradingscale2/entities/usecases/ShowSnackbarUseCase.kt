package de.felixlf.gradingscale2.entities.usecases

/**
 * Use case to show a snackbar.
 */
fun interface ShowSnackbarUseCase {
    suspend operator fun invoke(
        message: String,
        actionLabel: String?,
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
