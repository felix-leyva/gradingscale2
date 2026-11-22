package de.felixlf.gradingscale2.entities.daos

internal data class JoinedGradeScaleWithGradeDao(
    val gradeScaleId: String,
    val gradeScaleName: String,
    val totalPoints: Double,
    val gradeUuid: String,
    val namedGrade: String,
    val percentage: Double,
    val scaleId: String,
)
