package de.felixlf.gradingscale2.entities.serializers

import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Specific serializer for ImmutableList<Grade>
 */
object ImmutableGradeListSerializer : KSerializer<ImmutableList<Grade>> {
    private val listSerializer = ListSerializer(Grade.serializer())

    override val descriptor: SerialDescriptor = listSerializer.descriptor

    override fun serialize(encoder: Encoder, value: ImmutableList<Grade>) {
        listSerializer.serialize(encoder, value.toList())
    }

    override fun deserialize(decoder: Decoder): ImmutableList<Grade> {
        return listSerializer.deserialize(decoder).toImmutableList()
    }
}

/**
 * Specific serializer for ImmutableList<GradeScale>
 */
object ImmutableGradeScaleListSerializer : KSerializer<ImmutableList<GradeScale>> {
    private val listSerializer = ListSerializer(GradeScale.serializer())

    override val descriptor: SerialDescriptor = listSerializer.descriptor

    override fun serialize(encoder: Encoder, value: ImmutableList<GradeScale>) {
        listSerializer.serialize(encoder, value.toList())
    }

    override fun deserialize(decoder: Decoder): ImmutableList<GradeScale> {
        return listSerializer.deserialize(decoder).toImmutableList()
    }
}
