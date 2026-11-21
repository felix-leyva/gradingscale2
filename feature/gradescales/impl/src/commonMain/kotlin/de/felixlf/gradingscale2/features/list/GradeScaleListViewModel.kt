package de.felixlf.gradingscale2.features.list

import androidx.lifecycle.ViewModel
import de.felixlf.gradingscale2.entities.features.list.GradeListUIModel
import de.felixlf.gradingscale2.entities.features.list.GradeScaleListUIEvent
import de.felixlf.gradingscale2.entities.features.list.GradeScaleListUIState
import de.felixlf.gradingscale2.entities.uimodel.UIModel

/**
 * ViewModel for the GradeScaleListScreen.
 *
 * The ViewModel uses a [GradeListUIModel] which implements the [UIModel] interface which manages directly the events, so that
 * that the State lives inside the Factory.
 *
 * In this case, the ViewModel serves simply as a container for the UI State Factory which conveniently manages the scope and lifecycle
 * of the platform (for example in Android tied with navigation and lifecycle events).
 *
 * An UIFactory is also easier to unit test due that the we can supply the TestCoroutineDispatcher directly to the factory.
 *
 */

internal class GradeScaleListViewModel(
    uiModel: GradeListUIModel,
) : ViewModel(uiModel.scope), UIModel<GradeScaleListUIState, GradeScaleListUIEvent> by uiModel
