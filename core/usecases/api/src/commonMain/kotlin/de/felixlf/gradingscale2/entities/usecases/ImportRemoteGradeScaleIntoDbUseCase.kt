package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Option
import de.felixlf.gradingscale2.entities.models.remote.GradeScaleDTO

/**
 * Use case to import a remote grade scale into the database.
 */
fun interface ImportRemoteGradeScaleIntoDbUseCase {
    /**
     * Imports a remote grade scale into the database.
     *
     * @param remoteGradeScaleDTO The remote grade scale to import.
     * @return The ID of the imported grade scale, or an error message if the import failed.
     */
    suspend operator fun invoke(remoteGradeScaleDTO: GradeScaleDTO): Option<String>
}
