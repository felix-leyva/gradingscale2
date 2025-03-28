package de.felixlf.gradingscale2.entities.models

import kotlinx.serialization.Serializable

@Serializable
data class PointedGrade(
    val grade: Grade,
    val points: Double,
) {
    val namedGrade: String = grade.namedGrade
    val percentage: Double = grade.percentage
    val nameOfScale: String = grade.idOfGradeScale
    val uuid: String = grade.uuid
}

fun Grade.toPointedGrade(totalPoints: Double): PointedGrade = PointedGrade(
    grade = this,
    points = this.percentage * totalPoints,
)
