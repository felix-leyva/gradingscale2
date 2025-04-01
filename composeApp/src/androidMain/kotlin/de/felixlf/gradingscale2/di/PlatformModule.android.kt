package de.felixlf.gradingscale2.di

import de.felixlf.gradingscale2.entities.util.DispatcherProvider
import de.felixlf.gradingscale2.util.AndroidDispatcherProvider
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual fun getApplicationModule() =
    module {
        singleOf(::AndroidDispatcherProvider).bind<DispatcherProvider>()
    }
