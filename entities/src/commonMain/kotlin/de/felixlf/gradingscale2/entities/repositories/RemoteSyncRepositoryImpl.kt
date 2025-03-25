package de.felixlf.gradingscale2.entities.repositories

import arrow.core.Either
import de.felixlf.gradingscale2.entities.models.remote.Country
import de.felixlf.gradingscale2.entities.models.remote.CountryGradingScales
import de.felixlf.gradingscale2.entities.models.remote.GradeScaleDTO
import de.felixlf.gradingscale2.entities.models.remote.RemoteError
import de.felixlf.gradingscale2.entities.network.GradeScaleApi

class RemoteSyncRepositoryImpl(private val apiService: GradeScaleApi) : RemoteSyncRepository {
    override suspend fun countriesAndGrades(): Either<RemoteError, List<CountryGradingScales>> {
        return apiService.countriesAndGrades()
    }

    override suspend fun gradeScaleWithName(country: Country, name: String): Either<RemoteError, GradeScaleDTO> {
        return apiService.gradeScaleWithName(country, name)
    }
}
