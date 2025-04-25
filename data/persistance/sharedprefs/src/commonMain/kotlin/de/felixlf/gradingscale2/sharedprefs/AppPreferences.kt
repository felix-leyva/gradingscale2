package de.felixlf.gradingscale2.sharedprefs

import kotlinx.serialization.Serializable

@Serializable
data class AppPreferences(
    val lastSelectedGradeScaleId: String?,
) {
    companion object {
        val default = AppPreferences(
            lastSelectedGradeScaleId = null,
        )
    }
}
