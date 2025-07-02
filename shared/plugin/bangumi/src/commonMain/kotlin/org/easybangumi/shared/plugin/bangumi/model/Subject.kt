package org.easybangumi.shared.plugin.bangumi.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */

@Serializable
data class Subject(
    @SerialName("date") val date: String? = null,
    @SerialName("platform") val platform: String? = null,
    @SerialName("images") val images: Images? = Images(),
    @SerialName("summary") val summary: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("name_cn") val nameCn: String? = null,
    @SerialName("tags") val tags: List<Tags> = listOf(),
    @SerialName("infobox") val infobox: List<Infobox> = listOf(),
    @SerialName("rating") val rating: Rating? = Rating(),
    @SerialName("total_episodes") val totalEpisodes: Int? = null,
    @SerialName("collection") val collection: Collection? = Collection(),
    @SerialName("id") val id: Int? = null,
    @SerialName("eps") val eps: Int? = null,
    @SerialName("meta_tags") val metaTags: List<String> = listOf(),
    @SerialName("volumes") val volumes: Int? = null,
    @SerialName("series") val series: Boolean? = null,
    @SerialName("locked") val locked: Boolean? = null,
    @SerialName("nsfw") val nsfw: Boolean? = null,
    @SerialName("type") val type: Int? = null
)

@Serializable
data class Collection(
    @SerialName("on_hold") val onHold: Int? = null,
    @SerialName("dropped") val dropped: Int? = null,
    @SerialName("wish") val wish: Int? = null,
    @SerialName("collect") val collect: Int? = null,
    @SerialName("doing") val doing: Int? = null
)

@Serializable
data class Count(
    @SerialName("1") val i: Int? = null,
    @SerialName("2") val ii: Int? = null,
    @SerialName("3") val iii: Int? = null,
    @SerialName("4") val iv: Int? = null,
    @SerialName("5") val v: Int? = null,
    @SerialName("6") val vi: Int? = null,
    @SerialName("7") val vii: Int? = null,
    @SerialName("8") val iix: Int? = null,
    @SerialName("9") val ix: Int? = null,
    @SerialName("10") val x: Int? = null
)


@Serializable
data class Images(
    @SerialName("small") val small: String? = null,
    @SerialName("grid") val grid: String? = null,
    @SerialName("large") val large: String? = null,
    @SerialName("medium") val medium: String? = null,
    @SerialName("common") val common: String? = null
)

@Serializable
data class Infobox(
    @SerialName("key")
    val key: String? = null,
    @SerialName("value")
    @Serializable(with = InfoBoxValue.InfoBoxValueSerializer::class)
    val value: InfoBoxValue? = null
)

sealed class InfoBoxValue {
    data class Str(val value: String) : InfoBoxValue()
    data class ListMap(val value: List<Map<String, String>>) : InfoBoxValue()
    object InfoBoxValueSerializer : KSerializer<InfoBoxValue?> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("InfoBoxValue")
        override fun serialize(encoder: Encoder, value: InfoBoxValue?) {
            require(encoder is JsonEncoder)
            when (value) {
                is Str -> encoder.encodeString(value.value)
                is ListMap -> encoder.encodeSerializableValue(
                    ListSerializer(MapSerializer(String.serializer(), String.serializer())),
                    value.value
                )

                null -> encoder.encodeNull()
            }
        }

        override fun deserialize(decoder: Decoder): InfoBoxValue? {
            require(decoder is JsonDecoder)
            val element = decoder.decodeJsonElement()
            return when (element) {
                is JsonPrimitive -> InfoBoxValue.Str(element.content)
                is JsonArray -> InfoBoxValue.ListMap(
                    element.mapIndexed { index, jsonElement ->
                        (jsonElement as? JsonObject)?.mapValues { it.value.jsonPrimitive.content }
                    }.filterNotNull()
                )

                else -> null
            }
        }
    }
}

@Serializable
data class Rating(
    @SerialName("rank") val rank: Int? = null,
    @SerialName("total") val total: Int? = null,
    @SerialName("count") val count: Count? = Count(),
    @SerialName("score") val score: Double? = null
)

@Serializable
data class Tags(
    @SerialName("name") val name: String? = null,
    @SerialName("count") val count: Int? = null,
    @SerialName("total_cont") val totalCont: Int? = null
)