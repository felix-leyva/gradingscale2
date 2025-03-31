package de.felixlf.gradingscale2.uimodel.experiment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import kotlin.reflect.KClass

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
@Suppress("UNCHECKED_CAST")
class ExperimentViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        return ExperimentViewModel(
            factory = ExperimentUIModel(),
        ) as T
    }
}
