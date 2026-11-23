package de.felixlf.gradingscale2.entities.usecases

import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.repositories.GradeScaleRepository
import de.felixlf.gradingscale2.entities.util.GradeScaleGenerator
import io.github.aakira.napier.Napier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

fun interface GetAllGradeScalesUseCase {
    operator fun invoke(): Flow<ImmutableList<GradeScale>>
}

internal class GetAllGradeScalesUseCaseImpl(
    private val gradeScaleRepository: GradeScaleRepository,
    private val generator: GradeScaleGenerator,
) : GetAllGradeScalesUseCase {
    private val seedMutex = Mutex()

    override fun invoke(): Flow<ImmutableList<GradeScale>> =
        gradeScaleRepository.gradeScales
            .onStart { seedIfEmpty() }

    private suspend fun seedIfEmpty() {
        if (gradeScaleRepository.gradeScales.first().isNotEmpty()) return
        seedMutex.withLock {
            if (gradeScaleRepository.gradeScales.first().isNotEmpty()) return
            generator.getGradeScales().forEach { scale ->
                if (gradeScaleRepository.upsertGradeScale(scale).isNone()) {
                    Napier.w { "Failed to seed grade scale ${scale.id}" }
                }
            }
        }
    }
}
