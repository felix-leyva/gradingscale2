package de.felixlf.gradingscale2.entities

import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Helper function to run a test with a molecule test scope.
 * This function will automatically cancel all children coroutines after the test has finished.
 */
fun moleculeTest(
    context: CoroutineContext = EmptyCoroutineContext,
    timeout: Duration = 60.seconds,
    testBody: suspend TestScope.() -> Unit,
) = runTest(
    context = context,
    timeout = timeout,
) {
    testBody()
    coroutineContext.cancelChildren()
}
