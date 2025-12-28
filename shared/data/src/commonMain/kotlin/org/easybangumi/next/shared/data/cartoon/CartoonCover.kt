package org.easybangumi.next.shared.data.cartoon

import kotlinx.serialization.Serializable
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
@Serializable
data class CartoonCover(
    val id: String,
    val source: String,

    val name: String,
    val coverUrl: String,
    val intro: String,

    val webUrl: String,
): Extractor {

    fun toIdentify(): String {
        return "${id},${source}"
    }

    val cartoonIndex: CartoonIndex by lazy {
        CartoonIndex(
            id = id,
            source = source,
        ).apply {
            ext = this@CartoonCover.ext
        }
    }

    fun toCartoonIndex(): CartoonIndex {
        return cartoonIndex
    }


    @kotlinx.serialization.Transient
    @Transient
    override var ext: String = ""
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CartoonCover

        if (id != other.id) return false
        if (source != other.source) return false
        if (name != other.name) return false
        if (coverUrl != other.coverUrl) return false
        if (intro != other.intro) return false
        if (webUrl != other.webUrl) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + source.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + coverUrl.hashCode()
        result = 31 * result + intro.hashCode()
        result = 31 * result + webUrl.hashCode()
        return result
    }


}