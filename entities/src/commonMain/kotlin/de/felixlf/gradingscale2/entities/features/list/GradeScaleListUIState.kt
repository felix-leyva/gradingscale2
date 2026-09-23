package de.felixlf.gradingscale2.entities.features.list

import androidx.compose.runtime.Stable
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.models.GradeScaleNameAndId
import de.felixlf.gradingscale2.entities.util.stringWithDecimals
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Stable
data class GradeScaleListUIState(
    val selectedGradeScale: GradeScale?,
    val gradeScalesNamesWithId: ImmutableList<GradeScaleNameWithId>,
) {
    val hasSelectedGradeScale: Boolean = selectedGradeScale != null
    val selectedGradeScaleId: String? = selectedGradeScale?.id
    val selectedGradeScaleName: String = selectedGradeScale?.gradeScaleName ?: ""

    val totalPoints: Double = selectedGradeScale?.totalPoints ?: 10.0
    val totalPointsString: String = selectedGradeScale?.totalPoints?.stringWithDecimals() ?: ""

    val gradeScaleItems: ImmutableList<GradeScaleNameAndId> =
        gradeScalesNamesWithId.map {
            GradeScaleNameAndId(name = it.gradeScaleName, id = it.gradeScaleId)
        }.toImmutableList()

    val gradeItems: ImmutableList<GradeListItemUIState> =
        selectedGradeScale?.sortedPointedGrades?.map { pointedGrade ->
            GradeListItemUIState(
                uuid = pointedGrade.uuid,
                namedGrade = pointedGrade.namedGrade,
                percentage = pointedGrade.percentage,
                percentageFormatted = "${(pointedGrade.percentage * 100).stringWithDecimals()} %",
                points = pointedGrade.points,
                pointsFormatted = pointedGrade.points.stringWithDecimals(),
            )
        }?.toImmutableList() ?: persistentListOf()

    val isEmpty: Boolean = gradeItems.isEmpty()
    val gradeCount: Int = gradeItems.size

    data class GradeScaleNameWithId(
        val gradeScaleName: String,
        val gradeScaleId: String,
    )

    @Stable
    data class GradeListItemUIState(
        val uuid: String,
        val namedGrade: String,
        val percentage: Double,
        val percentageFormatted: String,
        val points: Double,
        val pointsFormatted: String,
    ) {
        val percentageProgress: Float = percentage.toFloat().coerceIn(0f, 1f)
    }
}
