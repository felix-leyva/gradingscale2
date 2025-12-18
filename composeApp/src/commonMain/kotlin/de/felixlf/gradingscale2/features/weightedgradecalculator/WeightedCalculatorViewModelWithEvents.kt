package de.felixlf.gradingscale2.features.weightedgradecalculator

import androidx.lifecycle.ViewModel
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightCalculatorUIModelWithEvents
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightCalculatorUIState
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightedCalculatorCommand
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightedCalculatorEvent
import de.felixlf.gradingscale2.entities.uimodel.UIModelWithEvents

/**
 * ViewModel for the Weighted Calculator feature. This links the WeightedCalculatorUIModel to the Lifecycle of the Framework.
 */
class WeightedCalculatorViewModelWithEvents(
    private val weightedCalculatorUIModel: WeightCalculatorUIModelWithEvents,
) : ViewModel(weightedCalculatorUIModel.scope),
    UIModelWithEvents<WeightCalculatorUIState, WeightedCalculatorCommand, WeightedCalculatorEvent> by weightedCalculatorUIModel
