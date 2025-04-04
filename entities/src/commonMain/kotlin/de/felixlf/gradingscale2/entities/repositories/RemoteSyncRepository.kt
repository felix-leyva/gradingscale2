package de.felixlf.gradingscale2.entities.repositories

import arrow.core.Either
import de.felixlf.gradingscale2.entities.models.remote.Country
import de.felixlf.gradingscale2.entities.models.remote.CountryGradingScales
import de.felixlf.gradingscale2.entities.models.remote.GradeScaleDTO
import de.felixlf.gradingscale2.entities.models.remote.RemoteError

/**
 * Provides methods for requesting data from a remote source
 */
interface RemoteSyncRepository {

    /**
     * Returns a list of countries and their corresponding grade scales.
     */
    suspend fun countriesAndGrades(): Either<RemoteError, List<CountryGradingScales>>

    /**
     * Returns a [GradeScaleDTO] with the given name.
     */
    suspend fun gradeScaleWithName(country: Country, name: String): Either<RemoteError, GradeScaleDTO>
}
