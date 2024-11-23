package de.felixlf.gradingscale2.network.models.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class GradeScaleRemote(
    val gradeScaleName: String = "",
    val country: String = "",
    val grades: List<GradeRemote> = emptyList(),
)

@Serializable
internal data class GradeRemote(
    val gradeName: String = "",
    val percentage: Double = 0.0,
)
