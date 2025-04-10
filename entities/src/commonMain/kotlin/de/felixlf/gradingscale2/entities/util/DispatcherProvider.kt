package de.felixlf.gradingscale2.entities.util

import de.felixlf.gradingscale2.entities.uimodel.UIModelScope
import kotlinx.coroutines.CoroutineDispatcher

/**
 * Provides the dispatchers for the application.
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val inmediate: CoroutineDispatcher
    val io: CoroutineDispatcher
    fun newUIScope(): UIModelScope
}
