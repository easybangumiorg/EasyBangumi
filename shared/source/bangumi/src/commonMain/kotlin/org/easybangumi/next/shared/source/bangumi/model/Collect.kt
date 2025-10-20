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
data class BgmCollectRsp(
    @SerialName("data")         val data: List<BgmCollect>,
    @SerialName("total")        val total: Long,
    @SerialName("limit")        val limit: Long,
    @SerialName("offset")       val offset: Long
)
@Serializable
data class BgmCollect (
    @SerialName("update_at")    val updatedAt: String? = null,
    @SerialName("comment")      val comment: String? = null,
    @SerialName("tags")         val tags: List<String>,
    @SerialName("subject")      val subject: BgmSubject,
    @SerialName("subject_id")   val subjectID: Long,
    @SerialName("vol_status")   val volStatus: Long,
    @SerialName("ep_status")    val epStatus: Long,
    @SerialName("subject_type") val subjectType: Long,
    @SerialName("type")         val type: Long,
    @SerialName("rate")         val rate: Long,
    @SerialName("private")      val private: Boolean
)