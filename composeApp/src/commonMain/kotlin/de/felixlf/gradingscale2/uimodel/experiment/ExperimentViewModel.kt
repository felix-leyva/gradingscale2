package de.felixlf.gradingscale2.uimodel.experiment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.felixlf.gradingscale2.entities.uimodel.UIModel
import kotlinx.coroutines.CoroutineScope

/**
 * This view model is used mainly to link the CoroutineScope of the UIPresenter with the Lifecycle of the ViewModel, which is also linked
 * to the Lifecycle of the UI.
 *
 */
class ExperimentViewModel(
    private val factory: ExperimentUIModel,
) : ViewModel(factory.scope), UIModel<ExperimentUIState, ExperimentUICommand, ExperimentUIEvent> by factory

/**
 * This factory is used to create the [ExperimentViewModel]. Normally we would not need this, as we use koin to inject it.
 */
fun experimentViewModelFactory(
    scope: CoroutineScope,
): ViewModelProvider.Factory = viewModelFactory {
    initializer {
        ExperimentViewModel(
            factory = ExperimentUIModel(scope),
        )
    }
}
