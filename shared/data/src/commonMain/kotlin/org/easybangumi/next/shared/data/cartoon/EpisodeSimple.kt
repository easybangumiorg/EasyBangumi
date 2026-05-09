package org.easybangumi.next.shared.data.cartoon

import kotlinx.serialization.Serializable

/**
 * 剧集优先模式下的剧集结构
 * 与 Episode 解耦，不包含 PlayerLine 引用
 */
@Serializable
class EpisodeSimple(
    val id: String,
    val label: String,
    val order: Int,
    val sourceName: String = "",
) : Extractor {

    @kotlinx.serialization.Transient
    override var ext: String = ""

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as EpisodeSimple

        if (order != other.order) return false
        if (id != other.id) return false
        if (label != other.label) return false
        if (sourceName != other.sourceName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = order
        result = 31 * result + id.hashCode()
        result = 31 * result + label.hashCode()
        result = 31 * result + sourceName.hashCode()
        return result
    }
}
