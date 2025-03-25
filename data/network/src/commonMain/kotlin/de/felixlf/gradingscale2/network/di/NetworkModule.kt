package de.felixlf.gradingscale2.network.di

import de.felixlf.gradingscale2.entities.network.BaseUrlProvider
import de.felixlf.gradingscale2.entities.network.GradeScaleApi
import de.felixlf.gradingscale2.network.api.GradeScaleApiImpl
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val networkModule = module {
    singleOf(::provideGradingScaleHttpClient).bind<HttpClient>()
    singleOf(::GradeScaleApiImpl).bind<GradeScaleApi>()
    singleOf(::BaseUrlProviderImpl).bind<BaseUrlProvider>()
}
