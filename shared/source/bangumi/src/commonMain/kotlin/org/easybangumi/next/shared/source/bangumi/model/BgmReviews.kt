package org.easybangumi.next.shared.source.bangumi.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
data class BgmReviews(
    @SerialName("id") val id: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("author") val author: String? = null,
    @SerialName("author_id") val authorId: String? = null,
    @SerialName("content_short") val contentShort: String? = null,
    @SerialName("date") val date: String? = null,
    @SerialName("cover") val cover: String? = null,
    @SerialName("star_count") val starCount: Int? = null,
    @SerialName("url") val url: String? = null,
)