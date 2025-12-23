package de.felixlf.gradingscale2.entities

import de.felixlf.gradingscale2.entities.uimodel.AndroidStateProducerImpl
import de.felixlf.gradingscale2.entities.uimodel.StateProducer
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformEntitiesModule = module {
    factoryOf(::AndroidStateProducerImpl).bind(StateProducer::class)
}
