package org.easybangumi.next.shared.source.bangumi.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.ext.shared.plugin.bangumi.plugin.BangumiInnerSource

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
data class TrendsSubject(
    @SerialName("id") val id: Int? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("name_cn") val nameCn: String? = null,
    @SerialName("image") val image: String? = null,

    @SerialName("info") val info: List<String> = listOf<String>(),

    @SerialName("rank") val rank: Int? = null,
    @SerialName("score") val score: Int? = null,
    @SerialName("score_total") val scoreTotal: Int? = null,
    @SerialName("jump_url") val jumpUrl: String? = null,
)

fun TrendsSubject.toCartoonCover(): CartoonCover? {
    return CartoonCover(
        id = id?.toString() ?: return null,
        source = BangumiInnerSource.SOURCE_ID,
        name = nameCn ?: name ?: "",
        coverUrl = image ?: "",
        intro = info.joinToString("/"),
        webUrl = jumpUrl ?: "",
    )
}