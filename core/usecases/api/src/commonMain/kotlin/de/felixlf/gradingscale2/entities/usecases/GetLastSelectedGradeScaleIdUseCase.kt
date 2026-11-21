package de.felixlf.gradingscale2.entities.usecases

/**
 * Use case to get the last selected grade scale ID.
 * Retrieves the ID of the last selected grade scale from the preferences repository.
 */
fun interface GetLastSelectedGradeScaleIdUseCase {
    suspend operator fun invoke(): String?
}
