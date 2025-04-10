package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Option
import arrow.core.raise.option
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.models.remote.GradeScaleDTO
import de.felixlf.gradingscale2.entities.repositories.GradeScaleRepository
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.firstOrNull
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Use case to import a remote grade scale into the database.
 */
fun interface ImportRemoteGradeScaleIntoDbUseCase {
    /**
     * Imports a remote grade scale into the database.
     *
     * @param remoteGradeScaleDTO The remote grade scale to import.
     * @return The ID of the imported grade scale, or an error message if the import failed.
     */
    suspend operator fun invoke(remoteGradeScaleDTO: GradeScaleDTO): Option<String>
}

internal class ImportRemoteGradeScaleIntoDbUseCaseImpl(
    private val gradeScaleRepository: GradeScaleRepository,
) : ImportRemoteGradeScaleIntoDbUseCase {
    @Suppress("MemberExtensionConflict")
    @OptIn(ExperimentalUuidApi::class)
    override suspend fun invoke(remoteGradeScaleDTO: GradeScaleDTO): Option<String> = option {
        val (currentNames, currentIds) = (
                gradeScaleRepository.getGradeScales().firstOrNull()
                    ?.map { it.gradeScaleName to it.id.toIntOrNull() }
                    ?: persistentListOf()
                ).unzip()

        val maxAvailableId = (currentIds.filterNotNull().maxOrNull() ?: 0) + 1

        val newGradeScaleName = generateUniqueName(
            originalName = remoteGradeScaleDTO.gradeScaleName,
            countryName = remoteGradeScaleDTO.country,
            existingNames = currentNames,
        )

        val newGradeScale = GradeScale(
            id = "$maxAvailableId",
            gradeScaleName = newGradeScaleName,
            totalPoints = 10.0,
            grades = remoteGradeScaleDTO.grades.map {
                Grade(
                    nameOfScale = newGradeScaleName,
                    namedGrade = it.gradeName,
                    percentage = it.percentage,
                    idOfGradeScale = "$maxAvailableId",
                    uuid = Uuid.random().toString(),
                )
            }.toImmutableList(),
        )
        gradeScaleRepository.upsertGradeScale(newGradeScale).bind()
    }

    private fun generateUniqueName(
        originalName: String,
        countryName: String,
        existingNames: List<String>,
    ): String {
        if (originalName !in existingNames) return originalName

        val countryPrefixedName = "$originalName - $countryName"
        if (countryPrefixedName !in existingNames) return countryPrefixedName

        var counter = 1
        while (true) {
            val numberedName = "$countryPrefixedName ($counter)"
            if (numberedName !in existingNames) return numberedName
            counter++
        }
    }
}
