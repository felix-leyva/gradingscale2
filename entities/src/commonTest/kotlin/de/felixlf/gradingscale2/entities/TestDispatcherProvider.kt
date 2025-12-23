package de.felixlf.gradingscale2.entities

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.MonotonicFrameClock
import de.felixlf.gradingscale2.entities.util.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher

/**
 * Test implementation of [de.felixlf.gradingscale2.entities.util.DispatcherProvider] that uses a [kotlinx.coroutines.test.TestDispatcher] for all dispatchers.
 * It also provides a [androidx.compose.runtime.BroadcastFrameClock] for the main dispatcher, which is useful for testing molecules.
 */
class TestDispatcherProvider(
    val testDispatcher: TestDispatcher = StandardTestDispatcher(),
    val clock: MonotonicFrameClock = BroadcastFrameClock(),
) : DispatcherProvider {
    override val main: CoroutineDispatcher = testDispatcher
    override val immediate: CoroutineDispatcher = testDispatcher
    override val io: CoroutineDispatcher = testDispatcher
}
