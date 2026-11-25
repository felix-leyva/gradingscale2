package de.felixlf.gradingscale2.network.api

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import de.felixlf.gradingscale2.entities.models.remote.Country
import de.felixlf.gradingscale2.entities.models.remote.CountryGradingScales
import de.felixlf.gradingscale2.entities.models.remote.GradeScaleDTO
import de.felixlf.gradingscale2.entities.models.remote.RemoteError
import de.felixlf.gradingscale2.entities.network.BaseUrlProvider
import de.felixlf.gradingscale2.entities.network.GradeScaleApi
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.http.appendPathSegments
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

internal class GradeScaleApiImpl(
    private val httpClient: HttpClient,
    private val baseUrlProvider: BaseUrlProvider,
) : GradeScaleApi {

    override suspend fun countriesAndGrades(): Either<RemoteError, List<CountryGradingScales>> = either {
        val httpResponse = safeCall {
            httpClient.request(normalizedBaseUrl()) {
                url { appendPathSegments("countriesAndGrades.json") }
            }
        }

        when (httpResponse.status.value) {
            in 200..299 -> {
                safeCall {
                    val body = httpResponse.body<JsonObject>()
                    body.entries.map { (countryName, gradeScales) ->
                        CountryGradingScales(
                            country = countryName,
                            gradesScalesNames = gradeScales.jsonArray.mapNotNull { it.jsonPrimitive.contentOrNull },
                        )
                    }
                }
            }

            else -> raise(RemoteError(httpResponse.status.value, httpResponse.status.description))
        }
    }

    override suspend fun gradeScaleWithName(country: Country, name: String): Either<RemoteError, GradeScaleDTO> = either {
        val gradeScaleAddress = "${country}_$name"
        val httpResponse = safeCall {
            httpClient.request(normalizedBaseUrl()) {
                url {
                    appendPathSegments(
                        "gradeScales",
                        "$gradeScaleAddress.json",
                        encodeSlash = true,
                    )
                }
            }
        }

        when (httpResponse.status.value) {
            in 200..299 -> safeCall { httpResponse.body<GradeScaleDTO>() }

            else -> raise(RemoteError(httpResponse.status.value, httpResponse.status.description))
        }
    }

    private fun normalizedBaseUrl(): String = baseUrlProvider.baseApiUrl.trimEnd('/')

    private inline fun <T> Raise<RemoteError>.safeCall(block: () -> T): T = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        raise(RemoteError(0, e.message ?: e::class.simpleName ?: "Unknown error"))
    }
}
