package de.felixlf.gradingscale2.utils

import de.felixlf.gradingscale2.entities.uimodel.UIModelScope
import de.felixlf.gradingscale2.entities.util.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val inmediate: CoroutineDispatcher = Dispatchers.Main.immediate
    override val io: CoroutineDispatcher = Dispatchers.Default

    override fun newUIScope(): UIModelScope {
        return CoroutineScope(inmediate + SupervisorJob())
    }
}
