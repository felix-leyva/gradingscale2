package de.felixlf.gradingscale2.entities.repositories

import arrow.core.Option
import arrow.core.raise.option
import de.felixlf.gradingscale2.entities.daos.GradeScaleDao
import de.felixlf.gradingscale2.entities.models.GradeScale
import io.github.aakira.napier.Napier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.shareIn

internal class GradeScaleRepositoryImpl(
    private val gradeScaleDao: GradeScaleDao,
    private val scope: CoroutineScope = CoroutineScope(
        SupervisorJob() + CoroutineName("GradeScaleRepositoryImpl"),
    ),
) : GradeScaleRepository {

    override val gradeScales: SharedFlow<ImmutableList<GradeScale>> =
        gradeScaleDao.getGradeScales()
            .retryWhen { cause, attempt ->
                if (cause is CancellationException || cause !is Exception) throw cause
                Napier.e(cause) { "Error observing grade scales (attempt $attempt)" }
                val backoffMs = (50L shl attempt.coerceAtMost(7).toInt()).coerceAtMost(5_000L)
                delay(backoffMs)
                true
            }
            .shareIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(
                    stopTimeoutMillis = 5_000L,
                    replayExpirationMillis = 0,
                ),
                replay = 1,
            )

    override fun getGradeScaleById(id: String): Flow<GradeScale?> =
        gradeScales
            .map { scales -> scales.find { it.id == id } }
            .distinctUntilChanged()

    override suspend fun upsertGradeScale(gradeScale: GradeScale): Option<String> = option {
        gradeScaleDao.upsertGradeScale(gradeScale).bind()
        gradeScale.id
    }

    override suspend fun deleteGradeScale(gradeScaleId: String): Option<Unit> =
        gradeScaleDao.deleteGradeScale(gradeScaleId)
}
