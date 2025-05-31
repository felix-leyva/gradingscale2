package de.felixlf.gradingscale2.entities.serializers

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Custom serializer for PersistentList that avoids polymorphic serialization issues
 * by always converting to/from regular List during serialization.
 * This prevents WasmJS from encountering internal implementations like SmallPersistentVector.
 */
class PersistentListSerializer<T>(dataSerializer: KSerializer<T>) : KSerializer<PersistentList<T>> {
    private val listSerializer = ListSerializer(dataSerializer)

    override val descriptor: SerialDescriptor = listSerializer.descriptor

    override fun serialize(encoder: Encoder, value: PersistentList<T>) {
        // Convert to regular list to avoid internal implementation serialization issues
        // This prevents SmallPersistentVector and other internal types from being serialized
        listSerializer.serialize(encoder, value.toList())
    }

    override fun deserialize(decoder: Decoder): PersistentList<T> {
        // Deserialize as regular list and convert to PersistentList
        // This ensures we get a proper PersistentList implementation
        return listSerializer.deserialize(decoder).toPersistentList()
    }
}

/**
 * Custom serializer for ImmutableList that avoids polymorphic serialization issues
 * by always converting to/from regular List during serialization.
 */
class ImmutableListSerializer<T>(dataSerializer: KSerializer<T>) : KSerializer<ImmutableList<T>> {
    private val listSerializer = ListSerializer(dataSerializer)

    override val descriptor: SerialDescriptor = listSerializer.descriptor

    override fun serialize(encoder: Encoder, value: ImmutableList<T>) {
        // Convert to regular list to avoid internal implementation serialization issues
        listSerializer.serialize(encoder, value.toList())
    }

    override fun deserialize(decoder: Decoder): ImmutableList<T> {
        // Deserialize as regular list and convert to ImmutableList
        return listSerializer.deserialize(decoder).toImmutableList()
    }
}
