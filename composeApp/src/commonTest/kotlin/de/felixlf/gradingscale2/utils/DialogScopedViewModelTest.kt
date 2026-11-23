package de.felixlf.gradingscale2.utils

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.koin.compose.KoinIsolatedContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class TrackingDialogViewModel : ViewModel() {
    var cleared: Boolean = false
        private set

    override fun onCleared() {
        cleared = true
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class DialogScopedViewModelTest {

    @Test
    fun dismissingDialogClearsDialogScopedViewModelAndDoesNotLeakIntoParentStore() = runTest {
        val koinApp = koinApplication {
            modules(
                module {
                    viewModel { TrackingDialogViewModel() }
                },
            )
        }
        val parentOwner = object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = ViewModelStore()
        }
        var showDialog by mutableStateOf(true)

        try {
            val stateFlow = backgroundScope.launchMolecule(RecompositionMode.Immediate) {
                val visible = showDialog
                var currentVm: TrackingDialogViewModel? = null
                KoinIsolatedContext(context = koinApp) {
                    CompositionLocalProvider(LocalViewModelStoreOwner provides parentOwner) {
                        if (visible) {
                            currentVm = dialogScopedViewModel<TrackingDialogViewModel>()
                        }
                    }
                }
                if (visible) currentVm else null
            }

            stateFlow.test {
                val vm1 = assertNotNull(awaitItem())
                assertFalse(vm1.cleared)

                showDialog = false
                runCurrent()
                assertNull(awaitItem())
                assertTrue(vm1.cleared, "Dialog dismissal should clear the dialog ViewModel")

                showDialog = true
                runCurrent()
                val vm2 = assertNotNull(awaitItem())
                assertNotSame(vm1, vm2)
                assertFalse(vm2.cleared)

                // Clearing the parent screen store while dialog is open must not clear the dialog VM,
                // proving it is not orphaned in the parent screen's ViewModelStore.
                parentOwner.viewModelStore.clear()
                assertFalse(vm2.cleared)

                showDialog = false
                runCurrent()
                assertNull(awaitItem())
                assertTrue(vm2.cleared)

                cancelAndIgnoreRemainingEvents()
            }
        } finally {
            parentOwner.viewModelStore.clear()
            koinApp.close()
        }
    }
}
