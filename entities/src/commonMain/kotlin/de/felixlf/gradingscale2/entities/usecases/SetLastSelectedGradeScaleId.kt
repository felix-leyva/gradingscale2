package de.felixlf.gradingscale2.entities.usecases

import arrow.core.Option
import de.felixlf.gradingscale2.entities.repositories.PreferencesRepository

fun interface SetLastSelectedGradeScaleId {
    suspend operator fun invoke(gradeScaleId: String): Option<Unit>
}

internal class SetLastSelectedGradeScaleIdImpl(
    private val preferencesRepository: PreferencesRepository,
) : SetLastSelectedGradeScaleId {
    override suspend fun invoke(gradeScaleId: String): Option<Unit> {
        return preferencesRepository.setLastSelectedGradeScaleId(gradeScaleId)
    }
}
