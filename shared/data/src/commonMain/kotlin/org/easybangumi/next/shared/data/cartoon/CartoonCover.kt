package org.easybangumi.next.shared.data.cartoon

import kotlin.jvm.Transient

/**
 * Created by heyanle on 2024/12/5.
 */
data class CartoonCover(
    val id: String,
    val fromSource: String,

    val name: String,
    val coverUrl: String,
    val intro: String,

    val detailedUrl: String,
): Extractor {

    @Transient
    override var ext: String = ""
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CartoonCover

        if (id != other.id) return false
        if (fromSource != other.fromSource) return false
        if (name != other.name) return false
        if (coverUrl != other.coverUrl) return false
        if (intro != other.intro) return false
        if (detailedUrl != other.detailedUrl) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + fromSource.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + coverUrl.hashCode()
        result = 31 * result + intro.hashCode()
        result = 31 * result + detailedUrl.hashCode()
        return result
    }


}