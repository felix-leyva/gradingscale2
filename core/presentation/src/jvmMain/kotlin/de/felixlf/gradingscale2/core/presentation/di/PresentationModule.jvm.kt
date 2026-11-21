package de.felixlf.gradingscale2.core.presentation.di

import de.felixlf.gradingscale2.entities.uimodel.DefaultStateProducerImpl
import de.felixlf.gradingscale2.entities.uimodel.StateProducer
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.factory

actual val platformPresentationModule = module {
    factory<DefaultStateProducerImpl>() bind StateProducer::class
}
