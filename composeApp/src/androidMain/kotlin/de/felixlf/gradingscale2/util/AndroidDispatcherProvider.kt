package de.felixlf.gradingscale2.util

import app.cash.molecule.AndroidUiDispatcher
import de.felixlf.gradingscale2.entities.util.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AndroidDispatcherProvider : DispatcherProvider {
    override val main = Dispatchers.Main
    override val inmediate: CoroutineDispatcher = Dispatchers.Main.immediate
    override val io = Dispatchers.IO
    override fun newUIScope(): CoroutineScope = CoroutineScope(
        AndroidUiDispatcher.Main + SupervisorJob(),
    )
}
