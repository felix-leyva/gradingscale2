package de.felixlf.gradingscale2.utils

import de.felixlf.gradingscale2.entities.util.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val immediate: CoroutineDispatcher = Dispatchers.Main.immediate
    override val io: CoroutineDispatcher = Dispatchers.Default
}
