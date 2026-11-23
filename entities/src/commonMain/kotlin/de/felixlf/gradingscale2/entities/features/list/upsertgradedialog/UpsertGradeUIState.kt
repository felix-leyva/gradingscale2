package de.felixlf.gradingscale2.entities.features.list.upsertgradedialog

import androidx.compose.runtime.Stable
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.util.toLocaleDoubleOrNull
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.toPersistentSet

@Stable
data class UpsertGradeUIState(
    val name: String?,
    val percentage: String?,
    val grade: Grade?,
    val gradeScale: GradeScale?,
    val selectedGrade: Grade?,
    val isSaved: Boolean = false,
) {

    private val percentageToDouble = percentage?.toLocaleDoubleOrNull()?.div(100)
    private val duplicatedPercentage =
        percentageToDouble != null && gradeScale?.grades?.map { it.percentage }?.contains(percentageToDouble) == true
    private val percentageFieldErrors =
        when {
            percentage != null && (percentageToDouble == null || percentageToDouble !in 0.0..1.0) -> Error.INVALID_PERCENTAGE
            selectedGrade?.percentage != percentageToDouble && duplicatedPercentage -> Error.DUPLICATE_PERCENTAGE

            else -> null
        }

    private val duplicatedName = gradeScale?.grades?.map { it.namedGrade }?.contains(name) == true
    private val nameFieldErrors = when {
        name?.isBlank() == true -> Error.INVALID_NAME
        selectedGrade?.namedGrade != name && duplicatedName -> Error.DUPLICATE_NAME
        else -> null
    }

    val error: PersistentSet<Error> = setOfNotNull(percentageFieldErrors, nameFieldErrors).toPersistentSet()

    val isLoading: Boolean = gradeScale == null

    val isSaveButtonEnabled: Boolean =
        !(grade == null || isLoading || isSaved || !error.isEmpty() || name.isNullOrBlank() || percentage.isNullOrBlank())

    val isSaveNewButtonEnabled: Boolean =
        !(isLoading || isSaved || !error.isEmpty() || name.isNullOrBlank() || percentage.isNullOrBlank() || duplicatedName || duplicatedPercentage)

    enum class Error { INVALID_PERCENTAGE, INVALID_NAME, DUPLICATE_NAME, DUPLICATE_PERCENTAGE }
}
