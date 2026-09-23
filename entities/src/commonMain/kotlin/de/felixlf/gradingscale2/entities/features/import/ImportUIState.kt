package de.felixlf.gradingscale2.entities.features.import

import androidx.compose.runtime.Stable
import de.felixlf.gradingscale2.entities.models.remote.Country
import de.felixlf.gradingscale2.entities.models.remote.CountryGradingScales
import de.felixlf.gradingscale2.entities.models.remote.GradeScaleDTO
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.StringResource

@Stable
data class ImportUIState(
    val countryGradingScales: ImmutableList<CountryGradingScales>,
    val displayedGradeScaleDTO: GradeScaleDTO?,
    val selectedCountry: Country?,
    val error: StringResource?,
    val isLoading: Boolean,
) {
    val shownCountryGradingScales = selectedCountry?.let { country ->
        countryGradingScales.filter { it.country == country }.toImmutableList()
    } ?: countryGradingScales

    val uniqueCountryNames = countryGradingScales
        .map { it.country }
        .distinct()
        .sorted()
        .toImmutableList()
}
