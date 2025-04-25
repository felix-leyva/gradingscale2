package de.felixlf.gradingscale2.entities.util

import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import gradingscale2.entities.generated.resources.Res
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.uuid.Uuid

/**
 * Generates a list of grade scales.
 */
interface GradeScaleGenerator {
    suspend fun getGradeScales(): List<GradeScale>
}

class ResourceGradeScaleGenerator(
    private val dispatcherProvider: DispatcherProvider,
) : GradeScaleGenerator {
    @OptIn(ExperimentalResourceApi::class)
    override suspend fun getGradeScales(): List<GradeScale> = withContext(dispatcherProvider.io) {
        val jsonFile = Res.readBytes("files/grade_in_scale.json")
        val jsonString = jsonFile.decodeToString()
        val decodedJsonGradeScales = Json.decodeFromString<List<JsonGradeScale>>(jsonString)
        decodedJsonGradeScales.mapIndexed { index, jsonGradeScale ->
            val gradeScaleId = index.toString()
            GradeScale(
                id = gradeScaleId,
                gradeScaleName = jsonGradeScale.gradeScaleName,
                totalPoints = 10.0,
                grades = jsonGradeScale.grades.map {
                    Grade(
                        namedGrade = it.namedGrade,
                        percentage = it.percentage,
                        idOfGradeScale = gradeScaleId,
                        nameOfScale = jsonGradeScale.gradeScaleName,
                        uuid = Uuid.random().toString(),
                    )
                }.toImmutableList(),
            )
        }
    }

    @Serializable
    private data class JsonGradeScale(
        val country: String,
        val gradeScaleName: String,
        val grades: List<Grade>,
    ) {
        @Serializable
        data class Grade(
            val namedGrade: String,
            val percentage: Double,
        )
    }
}
