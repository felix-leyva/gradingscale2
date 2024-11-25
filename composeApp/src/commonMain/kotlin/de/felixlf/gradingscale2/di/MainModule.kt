package de.felixlf.gradingscale2.di

import de.felixlf.gradingscale2.Initializer
import de.felixlf.gradingscale2.InitializerImpl
import de.felixlf.gradingscale2.dbModule
import de.felixlf.gradingscale2.entities.entitiesModule
import de.felixlf.gradingscale2.network.di.networkModule
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val mainModule =
    module {
        includes(
            getApplicationModule(),
            authModule,
            dbModule,
            networkModule,
            entitiesModule,
        )
        singleOf(::InitializerImpl).bind<Initializer>()
    }
