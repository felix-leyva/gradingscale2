package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Either
import de.felixlf.gradingscale2.entities.models.remote.RemoteError

/**
 * Imports a grade scale from a remote source into the local database.
 */
fun interface ImportRemoteGradeScale {
    suspend operator fun invoke(name: String): Either<RemoteError, Int>
}
