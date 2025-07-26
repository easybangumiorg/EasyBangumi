package org.easybangumi.next.shared.data.cartoon

import kotlin.jvm.Transient

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
data class CartoonPlayCover(
    val metaId: String,
    val metaSourceKey: String,

    val playId: String,
    val playSourceKey: String,

    // 以下数据来自 playSource
    val name: String,
    val coverUrl: String,
    val intro: String,

    val webUrl: String,
): Extractor {

    @delegate:Transient
    val playCover: CartoonCover by lazy {
        CartoonCover(
            id = playId,
            source = playSourceKey,
            name = name,
            coverUrl = coverUrl,
            intro = intro,
            webUrl = webUrl,
        )
    }

    @Transient
    override var ext: String = ""
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CartoonPlayCover

        if (metaId != other.metaId) return false
        if (metaSourceKey != other.metaSourceKey) return false
        if (playId != other.playId) return false
        if (playSourceKey != other.playSourceKey) return false
        if (name != other.name) return false
        if (coverUrl != other.coverUrl) return false
        if (intro != other.intro) return false
        if (webUrl != other.webUrl) return false

        return true
    }

    override fun hashCode(): Int {
        var result = metaId.hashCode()
        result = 31 * result + metaSourceKey.hashCode()
        result = 31 * result + playId.hashCode()
        result = 31 * result + playSourceKey.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + coverUrl.hashCode()
        result = 31 * result + intro.hashCode()
        result = 31 * result + webUrl.hashCode()
        return result
    }


}