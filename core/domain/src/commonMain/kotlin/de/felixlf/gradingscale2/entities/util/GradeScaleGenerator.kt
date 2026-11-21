package de.felixlf.gradingscale2.entities.util

import de.felixlf.gradingscale2.entities.models.GradeScale

/**
 * Generates a list of grade scales.
 */
interface GradeScaleGenerator {
    suspend fun getGradeScales(): List<GradeScale>
}
