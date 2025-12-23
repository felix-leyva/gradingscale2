package de.felixlf.gradingscale2.util

import de.felixlf.gradingscale2.entities.util.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class AndroidDispatcherProvider : DispatcherProvider {
    override val main = Dispatchers.Main
    override val immediate: CoroutineDispatcher = Dispatchers.Main.immediate
    override val io = Dispatchers.IO
}
