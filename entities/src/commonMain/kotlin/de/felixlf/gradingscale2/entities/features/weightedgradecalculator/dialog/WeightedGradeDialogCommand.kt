package de.felixlf.gradingscale2.entities.features.weightedgradecalculator.dialog

import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade

sealed interface WeightedGradeDialogCommand {
    data class Init(val weightedGrade: WeightedGrade, val gradeScale: GradeScale) : WeightedGradeDialogCommand
    data class SelectGradeName(val name: String) : WeightedGradeDialogCommand
    data class SetPercentage(val percentage: String) : WeightedGradeDialogCommand
    data class SetWeight(val weight: String) : WeightedGradeDialogCommand
    data class SetRelativeWeight(val relativeWeight: String) : WeightedGradeDialogCommand
}
