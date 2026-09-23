package de.felixlf.gradingscale2.entities.features.calculator

import androidx.compose.runtime.Stable
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.models.GradeScaleNameAndId
import de.felixlf.gradingscale2.entities.models.PointedGrade
import de.felixlf.gradingscale2.entities.models.toPointedGrade
import de.felixlf.gradingscale2.entities.util.stringWithDecimals
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList

/**
 * UI State for the Calculator screen.
 * All presentation-specific derivations are computed here, keeping the ViewModel/UIModel minimal.
 */
@Stable
data class GradeScaleCalculatorUIState(
    val selectedGradeScale: GradeScale?,
    val currentPercentage: Double?,
    val totalPoints: Double?,
    val gradeScalesNamesWithId: ImmutableList<GradeScaleNameWithId>,
) {
    val hasSelectedGradeScale: Boolean = selectedGradeScale != null
    val selectedGradeScaleId: String? = selectedGradeScale?.id
    val selectedGradeScaleName: String = selectedGradeScale?.gradeScaleName ?: ""

    val gradeScaleItems: PersistentList<GradeScaleNameAndId> =
        gradeScalesNamesWithId.map {
            GradeScaleNameAndId(id = it.gradeScaleId, name = it.gradeScaleName)
        }.toPersistentList()

    val totalPointsVal: Double = totalPoints ?: 10.0
    val totalPointsString: String = totalPoints?.stringWithDecimals() ?: ""

    val currentGrade: PointedGrade? = when {
        selectedGradeScale != null && currentPercentage != null && totalPoints != null -> selectedGradeScale.gradeByPercentage(
            percentage = currentPercentage,
        ).grade.copy(percentage = currentPercentage).toPointedGrade(totalPoints)

        else -> null
    }

    val hasGradeResult: Boolean = currentGrade != null
    val gradeName: String = currentGrade?.namedGrade ?: ""

    val earnedPoints: Double? = currentGrade?.points
    val earnedPointsString: String = currentGrade?.points?.stringWithDecimals() ?: ""

    val currentPercentageVal: Double = currentPercentage ?: 0.0
    val percentage100: Double = (currentPercentage ?: 0.0) * 100.0
    val percentageString: String = currentPercentage?.let { (it * 100.0).stringWithDecimals() } ?: ""
    val percentageFormattedWithSymbol: String = currentPercentage?.let { "${(it * 100.0).stringWithDecimals()} %" } ?: "- %"

    val availableGradeNames: ImmutableList<String> =
        selectedGradeScale?.sortedGrades?.map { it.namedGrade }?.toImmutableList() ?: persistentListOf()

    val gradeScalesNames: ImmutableList<String> = gradeScalesNamesWithId.map { it.gradeScaleName }.toImmutableList()

    // Stepper helper logic for rapid score adjustments (+/- 1.0, +/- 0.5)
    val canIncrementPoints: Boolean = (earnedPoints ?: 0.0) < totalPointsVal
    val canDecrementPoints: Boolean = (earnedPoints ?: 0.0) > 0.0
    val nextHigherPoints: Double = ((earnedPoints ?: 0.0) + 1.0).coerceAtMost(totalPointsVal)
    val nextLowerPoints: Double = ((earnedPoints ?: 0.0) - 1.0).coerceAtLeast(0.0)
    val nextHalfHigherPoints: Double = ((earnedPoints ?: 0.0) + 0.5).coerceAtMost(totalPointsVal)
    val nextHalfLowerPoints: Double = ((earnedPoints ?: 0.0) - 0.5).coerceAtLeast(0.0)

    data class GradeScaleNameWithId(
        val gradeScaleName: String,
        val gradeScaleId: String,
    )
}
