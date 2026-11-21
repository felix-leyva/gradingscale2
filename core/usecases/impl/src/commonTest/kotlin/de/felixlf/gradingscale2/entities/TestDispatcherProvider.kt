package de.felixlf.gradingscale2.entities

import de.felixlf.gradingscale2.entities.util.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher

class TestDispatcherProvider(
    val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) : DispatcherProvider {
    override val main: CoroutineDispatcher = testDispatcher
    override val immediate: CoroutineDispatcher = testDispatcher
    override val io: CoroutineDispatcher = testDispatcher
}
