package de.felixlf.gradingscale2.uimodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Converts a [StateFlow] of type [T] to a state exposing the value of type [T] in a composable function and represents its latest value.
 * Every time there would be new value posted into the Flow the returned State will be updated causing recomposition of every value usage.
 * @param context [CoroutineContext] to use for collecting.
 */
@Suppress("StateFlowValueCalledInComposition")
@Composable
inline fun <T> StateFlow<T>.asState(
    context: CoroutineContext = EmptyCoroutineContext,
): T = collectAsState(value, context).value

/**
 * Converts a [Flow] of type [T] to a state exposing the value of type [T] in a composable function and represents its latest value.
 * Every time there would be new value posted into the Flow the returned State will be updated causing recomposition of every value usage.
 * @param initial the value of the state will have until the first flow value is emitted.
 * @param context [CoroutineContext] to use for collecting.
 */
@Composable
inline fun <T : R, R> Flow<T>.asState(
    initial: R,
    context: CoroutineContext = EmptyCoroutineContext,
): R = collectAsState(initial, context).value
