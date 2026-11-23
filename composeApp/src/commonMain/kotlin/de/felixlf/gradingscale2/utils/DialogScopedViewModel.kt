package de.felixlf.gradingscale2.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import org.koin.compose.viewmodel.koinViewModel

@Composable
@PublishedApi
internal fun rememberDialogViewModelStoreOwner(): ViewModelStoreOwner {
    val owner = remember {
        object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = ViewModelStore()
        }
    }
    DisposableEffect(owner) {
        onDispose {
            owner.viewModelStore.clear()
        }
    }
    return owner
}

/**
 * Creates a ViewModel scoped to a dialog from the current composition.
 * The ViewModel is hosted in a dedicated [ViewModelStore] that is cleared when the dialog leaves composition.
 */
@Composable
inline fun <reified ViewModel : androidx.lifecycle.ViewModel> dialogScopedViewModel(): ViewModel {
    val dialogStoreOwner = rememberDialogViewModelStoreOwner()
    return koinViewModel<ViewModel>(viewModelStoreOwner = dialogStoreOwner)
}
