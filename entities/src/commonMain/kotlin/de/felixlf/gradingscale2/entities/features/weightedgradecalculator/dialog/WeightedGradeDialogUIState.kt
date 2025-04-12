package de.felixlf.gradingscale2.entities.features.weightedgradecalculator.dialog

import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.util.stringWithDecimals
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

data class WeightedGradeDialogUIState(
    val gradeScale: GradeScale?,
    val percentage: Double?,
    val weight: Double?,
) {
    val grades: ImmutableList<Grade> = gradeScale?.grades?.toImmutableList() ?: persistentListOf()

    val gradeNameString = percentage?.let { gradeScale?.nameByPercentage(it) } ?: ""
    val percentageString = percentage?.let { (it * 100).stringWithDecimals() } ?: ""
    val weightString = weight?.stringWithDecimals() ?: ""
    val relativeWeightString = weight?.let { weight -> percentage?.let { it * weight } }?.stringWithDecimals() ?: ""
}
