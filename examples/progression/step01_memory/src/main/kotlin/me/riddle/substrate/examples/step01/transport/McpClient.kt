package me.riddle.substrate.examples.step01.transport

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

// ── Request ID union: emit numeric; accept numeric|string; echo as-is ─────────

object RequestIdSerializer : KSerializer<RequestId> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("RequestId", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: RequestId) {
        val out = encoder as? JsonEncoder ?: error("Json only")
        when (value) {
            is RequestId.Num -> out.encodeJsonElement(JsonPrimitive(value.v))
            is RequestId.Str -> out.encodeJsonElement(JsonPrimitive(value.v))
        }
    }
    override fun deserialize(decoder: Decoder): RequestId {
        val inp = decoder as? JsonDecoder ?: error("Json only")
        return when (val el = inp.decodeJsonElement()) {
            is JsonPrimitive -> when {
                el.isString -> RequestId.Str(el.content)
                el.longOrNull != null -> RequestId.Num(el.long)
                else -> error("Invalid id primitive: $el")
            }
            else -> error("Invalid id: $el")
        }
    }
}
