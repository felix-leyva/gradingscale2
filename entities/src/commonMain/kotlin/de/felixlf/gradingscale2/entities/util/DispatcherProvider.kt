package de.felixlf.gradingscale2.entities.util

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Provides the dispatchers for the application.
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val immediate: CoroutineDispatcher
    val io: CoroutineDispatcher
}
