package org.easybangumi.next.shared.data.cartoon.meta

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
// TODO 通用元数据源
data class CartoonMetaInfo(
    val id: String,

    // finder
    val fromId: String,              // 标识，由源自己支持，用于区分番剧
    // 来源 Source Key
    val fromSourceKey: String,

    val name: String,
    val originalName: String,

    val originalAirDate: String,

    val totalEpisodes: Int,
    val episode: Int,

    val ranting: Ranting,

    // xx 人收藏，xx 人在看
    val collectionDesc: String,
)

data class Ranting(
    val score: String,
    val ranking: String,
    val count: String,
)