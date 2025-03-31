package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Either
import de.felixlf.gradingscale2.entities.models.remote.CountryAndName
import de.felixlf.gradingscale2.entities.models.remote.GradeScaleDTO
import de.felixlf.gradingscale2.entities.models.remote.RemoteError
import de.felixlf.gradingscale2.entities.repositories.RemoteSyncRepository

/**
 * Get remote grade scale.
 */
fun interface GetRemoteGradeScaleUseCase {
    suspend operator fun invoke(countryAndName: CountryAndName): Either<RemoteError, GradeScaleDTO>
}

internal class GetRemoteGradeScaleUseCaseImpl(private val repo: RemoteSyncRepository) : GetRemoteGradeScaleUseCase {
    override suspend fun invoke(
        countryAndName: CountryAndName,
    ): Either<RemoteError, GradeScaleDTO> = with(countryAndName) { repo.gradeScaleWithName(country, name) }
}
