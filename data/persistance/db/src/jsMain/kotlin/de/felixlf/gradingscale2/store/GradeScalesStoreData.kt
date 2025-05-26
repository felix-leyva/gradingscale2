package de.felixlf.gradingscale2.store

import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.serializers.ImmutableGradeScaleListSerializer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable

/**
 * Wrapper class for storing grade scales in KStore.
 * This class properly handles serialization of ImmutableList.
 */
@Serializable
data class GradeScalesStoreData(
    @Serializable(with = ImmutableGradeScaleListSerializer::class)
    val gradeScales: ImmutableList<GradeScale>,
)
