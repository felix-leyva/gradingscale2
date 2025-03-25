package de.felixlf.gradingscale2.entities.models.remote

/**
 * Data class containing countries and a list of grade scales.
 * @property country The country.
 * @property gradesScalesNames The list of grade scales names.
 */
data class CountryGradingScales(
    val country: Country,
    val gradesScalesNames: List<String>,
)
