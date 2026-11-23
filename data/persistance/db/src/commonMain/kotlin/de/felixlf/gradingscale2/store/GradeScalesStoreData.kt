package de.felixlf.gradingscale2.store

import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.serializers.PersistentListSerializer
import kotlinx.collections.immutable.PersistentList
import kotlinx.serialization.Serializable

/**
 * Wrapper class for storing grade scales in KStore.
 * This class properly handles serialization of ImmutableList.
 */
@Serializable
data class GradeScalesStoreData(
    @Serializable(with = PersistentListSerializer::class)
    val gradeScales: PersistentList<GradeScale>,
)
