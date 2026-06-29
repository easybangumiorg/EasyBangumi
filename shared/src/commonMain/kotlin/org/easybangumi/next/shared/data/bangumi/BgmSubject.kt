package org.easybangumi.next.shared.data.bangumi

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
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.CartoonIndex

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
data class BgmSubject(
    @SerialName("date") val date: String? = null,
    @SerialName("platform") val platform: String? = null,
    @SerialName("images") val images: BgmImages? = BgmImages(),
    @SerialName("summary") val summary: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("name_cn") val nameCn: String? = null,
    @SerialName("tags") val tags: List<BgmTags> = listOf(),
    @SerialName("infobox") val infobox: List<BgmInfobox> = listOf(),
    @SerialName("total_episodes") val totalEpisodes: Int? = null,
    @SerialName("id") val id: Int? = null,
    @SerialName("eps") val eps: Int? = null,
    @SerialName("meta_tags") val metaTags: List<String> = listOf(),
    @SerialName("volumes") val volumes: Int? = null,
    @SerialName("series") val series: Boolean? = null,
    @SerialName("locked") val locked: Boolean? = null,
    @SerialName("nsfw") val nsfw: Boolean? = null,
    @SerialName("type") val type: Int? = null,

    // from detail api
    @SerialName("collection") val collection: BgmCollection? = BgmCollection(),
    @SerialName("rating") val rating: BgmRating? = BgmRating(),

    // from user collection api
    @SerialName("score") val score: Float? = null,
    @SerialName("collection_total") val collectionTotal: Int? = null,
    @SerialName("rank") val rank: Int? = null,
    @SerialName("short_summary") val shortSummary: String? = null,

) {

    val allName: List<String> by lazy {
        val linkedSet = linkedSetOf<String>()
        nameCn?.let {
            if (it.isNotEmpty()) {
                linkedSet.add(it)
            }
        }
        name?.let {
            if (it.isNotEmpty() && it != nameCn) {
                linkedSet.add(it)
            }
        }
        infobox.forEach {
            if (it.key == "别名" || it.key == "中文名") {
                when (val value = it.value) {
                    is BgmInfoBoxValue.Str -> {
                        linkedSet.add(value.value.trim())
                    }

                    is BgmInfoBoxValue.ListMap -> {
                        val aliases = value.value.mapNotNull { map ->
                            map["v"]?.trim()
                        }
                        linkedSet.addAll(aliases)
                    }
                    else -> {}
                }
            }
        }

        linkedSet.toList()
    }

    val displayName: String by lazy {
        nameCn?.ifEmpty { name } ?: name ?: ""
    }

    val displayEpisode: String? by lazy {
        totalEpisodes ?: return@lazy null
        return@lazy "全 $totalEpisodes 话"
    }

    val cartoonIndex: CartoonIndex by lazy {
        CartoonIndex(
            id = id?.toString()?:"",
            source = BangumiConst.BANGUMI_SOURCE_KEY,
        )
    }

    val cartoonCover: CartoonCover by lazy {
        CartoonCover(
            id = id?.toString()?:"",
            source = BangumiConst.BANGUMI_SOURCE_KEY,
            name = displayName,
            coverUrl = images?.common ?: "",
            intro = displayEpisode ?: "",
            webUrl = "https://bgm.tv/subject/$id"
        )
    }

}

@Serializable
data class BgmCollection(
    @SerialName("on_hold") val onHold: Int? = null,
    @SerialName("dropped") val dropped: Int? = null,
    @SerialName("wish") val wish: Int? = null,
    @SerialName("collect") val collect: Int? = null,
    @SerialName("doing") val doing: Int? = null
)

@Serializable
data class BgmCount(
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
data class BgmImages(
    @SerialName("small") val small: String? = null,
    @SerialName("grid") val grid: String? = null,
    @SerialName("large") val large: String? = null,
    @SerialName("medium") val medium: String? = null,
    @SerialName("common") val common: String? = null
) {

    fun getCommonUrlFirst(): String? {
        return common ?: large ?: medium ?: small ?: grid
    }
}

@Serializable
data class BgmInfobox(
    @SerialName("key")
    val key: String? = null,
    @SerialName("value")
    @Serializable(with = BgmInfoBoxValue.InfoBoxValueSerializer::class)
    val value: BgmInfoBoxValue? = null
)

sealed class BgmInfoBoxValue {
    data class Str(val value: String) : BgmInfoBoxValue()
    data class ListMap(val value: List<Map<String, String>>) : BgmInfoBoxValue()

    object InfoBoxValueSerializer : KSerializer<BgmInfoBoxValue?> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("InfoBoxValue")
        override fun serialize(encoder: Encoder, value: BgmInfoBoxValue?) {
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

        override fun deserialize(decoder: Decoder): BgmInfoBoxValue? {
            require(decoder is JsonDecoder)
            val element = decoder.decodeJsonElement()
            return when (element) {
                is JsonPrimitive -> Str(element.content)
                is JsonArray -> ListMap(
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
data class BgmRating(
    @SerialName("rank") val rank: Int? = null,
    @SerialName("total") val total: Int? = null,
    @SerialName("count") val count: BgmCount? = BgmCount(),
    @SerialName("score") val score: Double? = null
)

@Serializable
data class BgmTags(
    @SerialName("name") val name: String? = null,
    @SerialName("count") val count: Int? = null,
    @SerialName("total_cont") val totalCont: Int? = null
)