package de.felixlf.gradingscale2.features.calculator

import androidx.lifecycle.ViewModel
import de.felixlf.gradingscale2.entities.features.calculator.CalculatorUIEvent
import de.felixlf.gradingscale2.entities.features.calculator.CalculatorUIModel
import de.felixlf.gradingscale2.entities.features.calculator.GradeScaleCalculatorUIState
import de.felixlf.gradingscale2.entities.uimodel.UIModel

/**
 * ViewModel for the CalculatorScreen.
 *
 * The ViewModel uses a [CalculatorUIModel] which implements the [UIModel] interface which manages directly the events, so that
 * that the State lives inside the Factory.
 *
 * In this case, the ViewModel serves simply as a container for the UI State Factory which conveniently manages the scope and lifecycle
 * of the platform (for example in Android tied with navigation and lifecycle events).
 *
 * An UIFactory is also easier to unit test due that the we can supply the TestCoroutineDispatcher directly to the factory.
 *
 */

internal class CalculatorViewModel(
    calculatorUIModel: CalculatorUIModel,
) : ViewModel(calculatorUIModel.scope), UIModel<GradeScaleCalculatorUIState, CalculatorUIEvent> by calculatorUIModel
