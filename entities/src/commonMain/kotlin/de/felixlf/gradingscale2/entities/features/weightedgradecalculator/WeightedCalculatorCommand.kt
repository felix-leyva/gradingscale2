package de.felixlf.gradingscale2.entities.features.weightedgradecalculator

import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.model.WeightedGrade

sealed interface WeightedCalculatorCommand {
    data class SelectGradeScale(val gradeScaleId: String?) : WeightedCalculatorCommand
    data class SelectGrade(val position: Int?) : WeightedCalculatorCommand
    data class AddGradeAtPos(val position: Int, val grade: WeightedGrade) : WeightedCalculatorCommand
    data class RemoveGrade(val position: Int) : WeightedCalculatorCommand
    data class UpdateGrade(val position: Int, val grade: WeightedGrade) : WeightedCalculatorCommand
}
