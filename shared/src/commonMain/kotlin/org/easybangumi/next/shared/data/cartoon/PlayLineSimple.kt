package org.easybangumi.next.shared.data.cartoon

import kotlinx.serialization.Serializable

/**
 * 剧集优先模式下的播放线路结构
 * 与 PlayerLine 解耦，不包含 episodeList
 */
@Serializable
class PlayLineSimple(
    val id: String,
    val label: String,
    val order: Int,
) : Extractor {

    @kotlinx.serialization.Transient
    override var ext: String = ""

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PlayLineSimple

        if (order != other.order) return false
        if (id != other.id) return false
        if (label != other.label) return false

        return true
    }

    override fun hashCode(): Int {
        var result = order
        result = 31 * result + id.hashCode()
        result = 31 * result + label.hashCode()
        return result
    }
}
