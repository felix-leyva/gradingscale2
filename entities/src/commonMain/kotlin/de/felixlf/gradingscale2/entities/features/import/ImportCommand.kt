package de.felixlf.gradingscale2.entities.features.import

import de.felixlf.gradingscale2.entities.models.remote.Country
import de.felixlf.gradingscale2.entities.models.remote.CountryAndName

sealed interface ImportCommand {
    data class SelectCountry(val country: Country) : ImportCommand
    data class OpenImportDialog(val countryAndName: CountryAndName) : ImportCommand
    data object ImportGradeScale : ImportCommand
}
