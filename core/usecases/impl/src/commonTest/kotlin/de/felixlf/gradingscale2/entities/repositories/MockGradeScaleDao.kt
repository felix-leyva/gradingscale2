package de.felixlf.gradingscale2.entities.repositories

import arrow.core.Option
import arrow.core.raise.option
import de.felixlf.gradingscale2.entities.daos.GradeScaleDao
import de.felixlf.gradingscale2.entities.models.GradeScale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class MockGradeScaleDao(
    initialGradeScales: ImmutableList<GradeScale> = persistentListOf(),
) : GradeScaleDao {
    val gradeScales = MutableStateFlow(initialGradeScales)
    var success = true

    override fun getGradeScaleById(id: String): Flow<GradeScale?> = gradeScales.map { scales ->
        scales.find {
            it.id == id
        }
    }

    override fun getGradeScales(): Flow<ImmutableList<GradeScale>> = gradeScales.asStateFlow()

    override suspend fun upsertGradeScale(gradeScale: GradeScale): Option<Unit> = option {
        ensure(success)

        val gradeScaleToModify = gradeScales.value.find { it.id == gradeScale.id }
        val modifiedGradeScales =
            if (gradeScaleToModify != null) {
                gradeScales.value.map { if (it.id == gradeScale.id) gradeScale else it }
            } else {
                gradeScales.value + gradeScale
            }.toImmutableList()
        gradeScales.value = modifiedGradeScales
    }

    override suspend fun deleteGradeScale(gradeScaleId: String): Option<Unit> = option {
        ensure(!gradeScales.value.none { it.id == gradeScaleId })
        gradeScales.value = gradeScales.value.filter { it.id != gradeScaleId }.toImmutableList()
    }
}
