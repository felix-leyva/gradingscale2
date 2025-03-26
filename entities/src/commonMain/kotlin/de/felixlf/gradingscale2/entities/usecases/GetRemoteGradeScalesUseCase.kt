package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Either
import de.felixlf.gradingscale2.entities.models.remote.CountryGradingScales
import de.felixlf.gradingscale2.entities.models.remote.RemoteError
import de.felixlf.gradingscale2.entities.repositories.RemoteSyncRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Get remote grade scales.
 */
fun interface GetRemoteGradeScalesUseCase {
    suspend operator fun invoke(): Either<RemoteError, ImmutableList<CountryGradingScales>>
}

internal class GetRemoteGradeScalesUseCaseImpl(private val repository: RemoteSyncRepository) : GetRemoteGradeScalesUseCase {
    override suspend fun invoke(): Either<RemoteError, ImmutableList<CountryGradingScales>> =
        repository.countriesAndGrades().map { it.toImmutableList() }
}
