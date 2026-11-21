package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Either
import de.felixlf.gradingscale2.entities.models.remote.CountryGradingScales
import de.felixlf.gradingscale2.entities.models.remote.RemoteError
import kotlinx.collections.immutable.ImmutableList

/**
 * Get remote grade scales.
 */
fun interface GetRemoteGradeScalesUseCase {
    suspend operator fun invoke(): Either<RemoteError, ImmutableList<CountryGradingScales>>
}
