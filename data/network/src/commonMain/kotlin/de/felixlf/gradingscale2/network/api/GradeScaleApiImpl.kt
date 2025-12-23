package de.felixlf.gradingscale2.network.api

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.either
import de.felixlf.gradingscale2.entities.models.remote.Country
import de.felixlf.gradingscale2.entities.models.remote.CountryGradingScales
import de.felixlf.gradingscale2.entities.models.remote.GradeScaleDTO
import de.felixlf.gradingscale2.entities.models.remote.RemoteError
import de.felixlf.gradingscale2.entities.network.BaseUrlProvider
import de.felixlf.gradingscale2.entities.network.GradeScaleApi
import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.request.request
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

internal class GradeScaleApiImpl(
    private val httpClient: HttpClient,
    private val baseUrlProvider: BaseUrlProvider,
) : GradeScaleApi {

    override suspend fun countriesAndGrades(): Either<RemoteError, List<CountryGradingScales>> = either {
        val httpResponse = runCatching { httpClient.request("${baseUrlProvider.baseApiUrl}/countriesAndGrades.json")}
            .getOrElse { raise(RemoteError(400, "")) }

        when {
            httpResponse.status.value in 200..299 -> {
                val body = safeGet { httpResponse.body<JsonObject>() }
                body.entries.map { (countryName, gradeScales) ->
                    CountryGradingScales(
                        country = countryName,
                        gradesScalesNames = gradeScales.jsonArray.mapNotNull { it.jsonPrimitive.contentOrNull },
                    )
                }
            }

            else -> raise(RemoteError(httpResponse.status.value, httpResponse.status.description))
        }
    }

    override suspend fun gradeScaleWithName(country: Country, name: String): Either<RemoteError, GradeScaleDTO> = either {
        val gradeScaleAddress = "${country}_$name"
        val httpResponse = httpClient.request("${baseUrlProvider.baseApiUrl}/gradeScales/$gradeScaleAddress.json")
        when {
            httpResponse.status.value in 200..299 -> safeGet { httpResponse.body<GradeScaleDTO>() }

            else -> raise(RemoteError(httpResponse.status.value, httpResponse.status.description))
        }
    }

    /**
     * Helper function to safely get the body of the response. In case of a NoTransformationFoundException, a RemoteError is raised.
     * @param block The block to execute.
     * @return The body of the response.
     */
    private inline fun <T> Raise<RemoteError>.safeGet(block: () -> T): T {
        return catch(block) { e: NoTransformationFoundException -> raise(RemoteError(0, e.message ?: "No transformation found")) }
    }
}
