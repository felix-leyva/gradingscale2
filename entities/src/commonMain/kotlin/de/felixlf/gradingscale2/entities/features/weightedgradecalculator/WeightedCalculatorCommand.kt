package de.felixlf.gradingscale2.entities.features.weightedgradecalculator

sealed interface WeightedCalculatorCommand {
    data class SelectGradeScale(val gradeScaleId: String) : WeightedCalculatorCommand
    data class AddGradeAtPos(val position: Int) : WeightedCalculatorCommand
    data object AddGradeAtEnd : WeightedCalculatorCommand
    data class RemoveGrade(val position: Int) : WeightedCalculatorCommand
    data class GradeAtPos(val position: Int) : WeightedCalculatorCommand
    data class UpdateGrade(val position: Int, val grade: WeightCalculatorUIState.WeightedGrade) : WeightedCalculatorCommand
}
