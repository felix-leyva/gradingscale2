package de.felixlf.gradingscale2.store

import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade
import de.felixlf.gradingscale2.entities.serializers.PersistentListSerializer
import de.felixlf.gradingscale2.entities.util.DispatcherProvider
import io.github.xxfast.kstore.KStore
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class WeightedGradesStoreData(
    @Serializable(with = PersistentListSerializer::class)
    val weightedGrades: PersistentList<WeightedGrade>,
)

interface WeightedGradesStore {
    fun getAllWeightedGrades(): Flow<List<WeightedGrade>>
    suspend fun upsertWeightedGrade(weightedGrade: WeightedGrade)
    suspend fun deleteWeightedGrade(weightedGradeId: String): Boolean
}

class WeightedGradesStoreProvider(
    private val weightedGradesStore: KStore<WeightedGradesStoreData>,
    private val dispatcherProvider: DispatcherProvider,
) : WeightedGradesStore {
    override fun getAllWeightedGrades(): Flow<List<WeightedGrade>> {
        return weightedGradesStore.updates.map { data ->
            data?.weightedGrades ?: persistentListOf()
        }
    }

    override suspend fun upsertWeightedGrade(weightedGrade: WeightedGrade) {
        withContext(dispatcherProvider.io) {
            weightedGradesStore.update { data ->
                val currentData = data ?: WeightedGradesStoreData(persistentListOf())
                val updatedGrades = currentData.weightedGrades.mutate { grades ->
                    val index = grades.indexOfFirst { it.uuid == weightedGrade.uuid }
                    if (index != -1) {
                        grades[index] = weightedGrade
                    } else {
                        grades.add(weightedGrade)
                    }
                }
                currentData.copy(weightedGrades = updatedGrades)
            }
        }
    }

    override suspend fun deleteWeightedGrade(weightedGradeId: String): Boolean {
        return withContext(dispatcherProvider.io) {
            var deleted = false
            weightedGradesStore.update { data ->
                val currentData = data ?: WeightedGradesStoreData(persistentListOf())
                val indexToRemove = currentData.weightedGrades.indexOfFirst { it.uuid == weightedGradeId }
                if (indexToRemove != -1) {
                    deleted = true
                    val updatedGrades = currentData.weightedGrades.mutate { grades ->
                        grades.removeAt(indexToRemove)
                    }
                    currentData.copy(weightedGrades = updatedGrades)
                } else {
                    currentData
                }
            }
            deleted
        }
    }
}
