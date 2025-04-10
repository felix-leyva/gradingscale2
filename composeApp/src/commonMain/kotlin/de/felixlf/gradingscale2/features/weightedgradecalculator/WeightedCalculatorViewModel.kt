package de.felixlf.gradingscale2.features.weightedgradecalculator

import androidx.lifecycle.ViewModel
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightCalculatorUIModel
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightCalculatorUIState
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightedCalculatorCommand
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightedCalculatorEvent
import de.felixlf.gradingscale2.entities.uimodel.UIModel

/**
 * ViewModel for the Weighted Calculator feature. This links the WeightedCalculatorUIModel to the Lifecycle of the Framework.
 */
class WeightedCalculatorViewModel(
    private val weightedCalculatorUIModel: WeightCalculatorUIModel,
) : ViewModel(weightedCalculatorUIModel.scope),
    UIModel<WeightCalculatorUIState, WeightedCalculatorCommand, WeightedCalculatorEvent> by weightedCalculatorUIModel
