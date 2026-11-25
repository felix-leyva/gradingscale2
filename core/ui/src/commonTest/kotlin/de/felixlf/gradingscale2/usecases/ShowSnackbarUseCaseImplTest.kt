package de.felixlf.gradingscale2.usecases

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import de.felixlf.gradingscale2.entities.util.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class ShowSnackbarUseCaseImplTest {

    @Test
    fun showDetachedSurvivesCallerCancellationSoDialogDismissDoesNotCancelSnackbar() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val dispatcherProvider = object : DispatcherProvider {
            override val main: CoroutineDispatcher = dispatcher
            override val immediate: CoroutineDispatcher = dispatcher
            override val io: CoroutineDispatcher = dispatcher
        }
        val hostState = SnackbarHostState()
        val useCase = ShowSnackbarUseCaseImpl(hostState, dispatcherProvider)

        val callerJob = launch {
            useCase.showDetached(
                message = "Copied!",
                actionLabel = null,
                duration = SnackbarDuration.Short,
            )
        }
        runCurrent()

        val initialSnackbar = hostState.currentSnackbarData
        assertNotNull(initialSnackbar)
        assertEquals("Copied!", initialSnackbar.visuals.message)

        // Simulate dialog dismissal cancelling the caller scope immediately after copy.
        callerJob.cancel()
        runCurrent()

        val activeSnackbar = hostState.currentSnackbarData
        assertNotNull(activeSnackbar)
        assertEquals("Copied!", activeSnackbar.visuals.message)

        activeSnackbar.dismiss()
        runCurrent()
    }
}
