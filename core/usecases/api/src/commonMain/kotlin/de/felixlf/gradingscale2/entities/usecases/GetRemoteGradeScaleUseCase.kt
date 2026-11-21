package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Either
import de.felixlf.gradingscale2.entities.models.remote.CountryAndName
import de.felixlf.gradingscale2.entities.models.remote.GradeScaleDTO
import de.felixlf.gradingscale2.entities.models.remote.RemoteError

/**
 * Get remote grade scale.
 */
fun interface GetRemoteGradeScaleUseCase {
    suspend operator fun invoke(countryAndName: CountryAndName): Either<RemoteError, GradeScaleDTO>
}
