package de.felixlf.gradingscale2.network.di

import de.felixlf.gradingscale2.entities.network.BaseUrlProvider
import de.felixlf.gradingscale2.entities.network.GradeScaleApi
import de.felixlf.gradingscale2.network.api.GradeScaleApiImpl
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create
import org.koin.plugin.module.dsl.single

val networkModule = module {
    single { create(::provideGradingScaleHttpClient) }
    single<GradeScaleApiImpl>() bind GradeScaleApi::class
    single<BaseUrlProviderImpl>() bind BaseUrlProvider::class
}
