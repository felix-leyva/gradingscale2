package de.felixlf.gradingscale2.entities.uimodel

import app.cash.molecule.RecompositionMode
import de.felixlf.gradingscale2.entities.util.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * The default [StateProducer] implementation for every platform except android
 */
class DefaultStateProducerImpl(
    dispatcherProvider: DispatcherProvider,
) : StateProducer {
    override val scope: CoroutineScope = CoroutineScope(dispatcherProvider.immediate + SupervisorJob())

    override val recompositionMode: RecompositionMode = RecompositionMode.Immediate
}
