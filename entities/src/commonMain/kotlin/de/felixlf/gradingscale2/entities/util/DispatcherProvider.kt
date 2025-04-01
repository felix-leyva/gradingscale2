package de.felixlf.gradingscale2.entities.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

/**
 * Provides the dispatchers for the application.
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher

    val inmediate: CoroutineDispatcher
    val io: CoroutineDispatcher
    fun newUIScope(): CoroutineScope
}
