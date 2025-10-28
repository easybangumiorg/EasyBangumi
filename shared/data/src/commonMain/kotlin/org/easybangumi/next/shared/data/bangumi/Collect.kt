package org.easybangumi.next.shared.data.bangumi

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
    @SerialName("data")         val data: List<BgmCollect> = emptyList(),
    @SerialName("total")        val total: Long? = null,
    @SerialName("limit")        val limit: Long? = null,
    @SerialName("offset")       val offset: Long? = null
)
@Serializable
data class BgmCollect (
    @SerialName("update_at")    val updatedAt: String? = null,
    @SerialName("comment")      val comment: String? = null,
    @SerialName("tags")         val tags: List<String> = emptyList(),
    @SerialName("subject")      val subject: BgmSubject? = null,
    @SerialName("subject_id")   val subjectID: Long? = null,
    @SerialName("vol_status")   val volStatus: Long? = null,
    @SerialName("ep_status")    val epStatus: Long? = null,
    @SerialName("subject_type") val subjectType: Long? = null,
    @SerialName("type")         val type: Long? = null,
    @SerialName("rate")         val rate: Long? = null,
    @SerialName("private")      val private: Boolean? = null
)