package de.felixlf.gradingscale2.entities.features.import

import de.felixlf.gradingscale2.entities.models.remote.Country
import de.felixlf.gradingscale2.entities.models.remote.CountryGradingScales
import de.felixlf.gradingscale2.entities.models.remote.GradeScaleDTO
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ImportUIState(
    val countryGradingScales: ImmutableList<CountryGradingScales>,
    val displayedGradeScaleDTO: GradeScaleDTO?,
    val selectedCountry: Country?,
    val error: String?,
    val isLoading: Boolean,
) {
    val shownCountryGradingScales = selectedCountry?.let { country ->
        persistentListOf(countryGradingScales.find { it.country == country })
    } ?: countryGradingScales

}
