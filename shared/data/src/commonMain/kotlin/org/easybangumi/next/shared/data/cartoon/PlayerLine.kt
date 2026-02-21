package org.easybangumi.next.shared.data.cartoon

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Created by heyanle on 2024/12/5.
 */
@Serializable
class PlayerLine (
    val id: String,
    val label: String = LABEL_NONE,
    val order: Int = -1,
    val episodeList : List<Episode>
): Extractor {
    @Transient
    override var ext: String = ""

    companion object {
        // 无
        const val LABEL_NONE = "##none##"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PlayerLine

        if (order != other.order) return false
        if (id != other.id) return false
        if (label != other.label) return false
        if (episodeList != other.episodeList) return false

        return true
    }

    override fun hashCode(): Int {
        var result = order
        result = 31 * result + id.hashCode()
        result = 31 * result + label.hashCode()
        result = 31 * result + episodeList.hashCode()
        return result
    }


}