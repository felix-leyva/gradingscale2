package de.felixlf.gradingscale2.feature.importer.di

import de.felixlf.gradingscale2.entities.features.import.ImportUIModelWithEvents
import de.felixlf.gradingscale2.feature.importer.api.ImporterScreenEntry
import de.felixlf.gradingscale2.features.import.ImportScreen
import de.felixlf.gradingscale2.features.import.ImportViewModelWithEvents
import org.koin.dsl.module
import org.koin.plugin.module.dsl.factory
import org.koin.plugin.module.dsl.viewModel

val importerFeatureModule = module {
    factory<ImportUIModelWithEvents>()
    viewModel<ImportViewModelWithEvents>()
    single<ImporterScreenEntry> {
        ImporterScreenEntry { modifier ->
            ImportScreen(modifier = modifier)
        }
    }
}
