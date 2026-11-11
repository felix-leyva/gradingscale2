package de.felixlf.gradingscale2.sharedprefs

import de.felixlf.gradingscale2.entities.daos.PreferencesDao
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val preferencesModule = module {
    includes(getPlatformPreferencesModule())
    single<PreferencesDaoImpl>() bind PreferencesDao::class
}

expect fun getPlatformPreferencesModule(): Module
