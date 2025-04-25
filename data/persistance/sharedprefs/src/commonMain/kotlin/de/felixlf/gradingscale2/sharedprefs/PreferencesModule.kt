package de.felixlf.gradingscale2.sharedprefs

import de.felixlf.gradingscale2.entities.daos.PreferencesDao
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val preferencesModule = module {
    includes(getPlatformPreferencesModule())
    singleOf(::PreferencesDaoImpl).bind<PreferencesDao>()
}

expect fun getPlatformPreferencesModule(): Module
