package com.github.rushyverse.mojang.api.serializer

import com.github.rushyverse.mojang.api.extension.formatUUID
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializer to format a string to a valid uuid string format.
 * @property stringSerializer The string serializer.
 * @see formatUUID
 */
public object StringUUIDSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(
            "stringUuid",
            PrimitiveKind.STRING,
        )

    override fun serialize(
        encoder: Encoder,
        value: String,
    ) {
        encoder.encodeString(value.formatUUID())
    }

    override fun deserialize(decoder: Decoder): String {
        return decoder.decodeString().formatUUID()
    }
}
