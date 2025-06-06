package de.felixlf.gradingscale2.features.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.felixlf.gradingscale2.entities.features.list.GradeListUIModel
import de.felixlf.gradingscale2.entities.features.list.GradeScaleListUIEvent
import de.felixlf.gradingscale2.entities.features.list.GradeScaleListUIState
import de.felixlf.gradingscale2.entities.uimodel.MoleculePresenter
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import de.felixlf.gradingscale2.entities.usecases.GetLastSelectedGradeScaleIdUseCase
import de.felixlf.gradingscale2.entities.usecases.SetLastSelectedGradeScaleIdUseCase
import de.felixlf.gradingscale2.entities.util.DispatcherProvider
import de.felixlf.gradingscale2.uimodel.MoleculeViewModelHelper

/**
 * ViewModel for the GradeScaleListScreen.
 * @param allGradeScalesUseCase UseCase for getting all grade scales.
 * @param getGradeScaleByIdUseCase UseCase for getting a grade scale by its ID.
 * This ViewModel implements the UIEventPresenter interface to handle UI events and uses the default implementation of the interface by
 * delegation.
 *
 * The ViewModel uses a [GradeListUIModel] which implements the [MoleculePresenter] interface which manages directly the events, so that
 * that the State lives inside the Factory.
 *
 * In this case, the ViewModel serves simply as a container for the UI State Factory which conveniently manages the scope and lifecycle
 * of the platform (for example in Android tied with navigation and lifecycle events).
 *
 * An UIFactory is also easier to unit test due that the we can supply the TestCoroutineDispatcher directly to the factory.
 *
 */

internal class GradeScaleListViewModel(
    dispatcherProvider: DispatcherProvider,
    allGradeScalesUseCase: GetAllGradeScalesUseCase,
    getGradeScaleByIdUseCase: GetGradeScaleByIdUseCase,
    getLastSelectedGradeScaleIdUseCase: GetLastSelectedGradeScaleIdUseCase,
    setLastSelectedGradeScaleIdUseCase: SetLastSelectedGradeScaleIdUseCase,
) : ViewModel(dispatcherProvider.newUIScope()), MoleculeViewModelHelper<GradeScaleListUIState, GradeScaleListUIEvent> {

    override val factory = GradeListUIModel(
        scope = viewModelScope,
        allGradeScalesUseCase = allGradeScalesUseCase,
        getGradeScaleByIdUseCase = getGradeScaleByIdUseCase,
        getLastSelectedGradeScaleIdUseCase = getLastSelectedGradeScaleIdUseCase,
        setLastSelectedGradeScaleIdUseCase = setLastSelectedGradeScaleIdUseCase,
    )

    override val uiState = moleculeState()
}
