package org.easybangumi.ext.shared.plugin.bangumi.model

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
data class EpisodeRsp(
    @SerialName("data") val data: List<Episode> = emptyList(),
    @SerialName("total") val total: Long? = null,
    @SerialName("limit") val limit: Long? = null,
    @SerialName("offset") val offset: Long? = null
)

@Serializable
data class Episode (
    @SerialName("airdate") val airdate: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("name_cn") val nameCN: String? = null,
    @SerialName("duration") val duration: String? = null,
    @SerialName("desc") val desc: String? = null,
    @SerialName("ep") val ep: Long? = null,
    @SerialName("sort") val sort: Long? = null,
    @SerialName("id") val id: Long? = null,
    @SerialName("subject_id") val subjectID: Long? = null,
    @SerialName("comment") val comment: Long? = null,
    @SerialName("type") val type: Long? = null,
    @SerialName("disc") val disc: Long? = null,
    @SerialName("duration_seconds") val durationSeconds: Long? = null
)

@Serializable
data class Person (
    @SerialName("images") val images: Images? = Images(),
    @SerialName("name") val name: String? = null,
    @SerialName("relation") val relation: String? = null,
    @SerialName("career") val career: List<String> = listOf<String>(),
    @SerialName("type") val type: Long,
    @SerialName("id") val id: Long,
    @SerialName("eps") val eps: String
)

@Serializable
data class Character (
    @SerialName("images") val images: Images? = Images(),
    @SerialName("name")  val name: String? = null,
    @SerialName("relation") val relation: String? = null,
    @SerialName("actors") val actors: List<Actor> = emptyList<Actor>(),
    @SerialName("type") val type: Long? = null,
    @SerialName("id") val id: Long? = null,
)

@Serializable
data class Actor (
    @SerialName("images") val images: Images? = Images(),
    @SerialName("name") val name: String? = null,
    @SerialName("short_summary") val shortSummary: String? = null,
    @SerialName("career") val career: List<String> = emptyList<String>(),
    @SerialName("id") val id: Long? = null,
    @SerialName("type") val type: Long? = null,
    @SerialName("locked") val locked: Boolean? = null,
)