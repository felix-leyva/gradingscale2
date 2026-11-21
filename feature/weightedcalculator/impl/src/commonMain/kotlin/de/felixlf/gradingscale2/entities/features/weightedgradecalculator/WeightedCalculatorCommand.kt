package de.felixlf.gradingscale2.entities.features.weightedgradecalculator

import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade

internal sealed interface WeightedCalculatorCommand {
    data class SelectGradeScale(val gradeScaleId: String?) : WeightedCalculatorCommand
    data class SelectGrade(val id: String?) : WeightedCalculatorCommand
    data class AddGradeAtPos(val position: Int, val grade: WeightedGrade) : WeightedCalculatorCommand
    data class RemoveGrade(val id: String) : WeightedCalculatorCommand
    data class UpdateGrade(val grade: WeightedGrade) : WeightedCalculatorCommand
}
