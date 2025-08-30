package org.easybangumi.next.shared.data.cartoon

import kotlin.jvm.Transient


/**
 * Created by HeYanLe on 2025/8/24 15:10.
 * https://github.com/heyanLE
 */

class CartoonPlayCover(
    val id: String,
    val source: String,

    val name: String,
    val coverUrl: String,

    val webUrl: String,
    val tags: List<String>,
    val desc: String,

): Extractor {

    @kotlinx.serialization.Transient
    @Transient
    override var ext: String = ""
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CartoonPlayCover

        if (id != other.id) return false
        if (source != other.source) return false
        if (name != other.name) return false
        if (coverUrl != other.coverUrl) return false
        if (webUrl != other.webUrl) return false
        if (tags != other.tags) return false
        if (desc != other.desc) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + source.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + coverUrl.hashCode()
        result = 31 * result + webUrl.hashCode()
        result = 31 * result + tags.hashCode()
        result = 31 * result + desc.hashCode()
        return result
    }


}