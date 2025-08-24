package org.easybangumi.next.shared.data.cartoon

import kotlinx.serialization.Serializable
import kotlin.jvm.Transient

/**
 * 番剧封面，其中 source 可能是播放源和挂载源，id 为 source 中的源
 * Created by heyanle on 2024/12/5.
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


    fun toCartoonIndex(): CartoonIndex {
        return CartoonIndex(
            id = id,
            source = source,
        ).apply {
            ext = this@CartoonCover.ext
        }
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