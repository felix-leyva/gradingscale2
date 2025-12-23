package de.felixlf.gradingscale2.entities.uimodel

import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionMode
import de.felixlf.gradingscale2.entities.util.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * The default [StateProducer] implementation for every platform except android
 */
class AndroidStateProducerImpl : StateProducer {
    override val scope: CoroutineScope = CoroutineScope(AndroidUiDispatcher.Main + SupervisorJob())
    override val recompositionMode: RecompositionMode = RecompositionMode.ContextClock
}
