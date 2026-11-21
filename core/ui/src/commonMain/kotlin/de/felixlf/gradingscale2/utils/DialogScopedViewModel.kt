package de.felixlf.gradingscale2.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Creates a ViewModel scoped to a dialog from the current composition.
 * The ViewModel will be scoped to the dialog and will be destroyed when the dialog is dismissed.
 */
@OptIn(ExperimentalUuidApi::class)
@Composable
inline fun <reified ViewModel : androidx.lifecycle.ViewModel> dialogScopedViewModel(): ViewModel {
    val randomUUID = rememberSaveable { Uuid.random().toString() }
    return koinViewModel<ViewModel>(key = randomUUID)
}
