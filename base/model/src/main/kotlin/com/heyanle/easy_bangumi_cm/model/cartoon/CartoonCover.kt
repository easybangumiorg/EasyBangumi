package com.heyanle.easy_bangumi_cm.model.cartoon

/**
 * Created by heyanlin on 2024/12/5.
 */
class CartoonCover(
    val id: String,
    val mediaSource: String,

    val name: String,
    val coverUrl: String,
    val intro: String,

    val detailedUrl: String,
): Extractor {

    @Transient
    override var ext: String = ""
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CartoonCover

        if (id != other.id) return false
        if (mediaSource != other.mediaSource) return false
        if (name != other.name) return false
        if (coverUrl != other.coverUrl) return false
        if (intro != other.intro) return false
        if (detailedUrl != other.detailedUrl) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + mediaSource.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + coverUrl.hashCode()
        result = 31 * result + intro.hashCode()
        result = 31 * result + detailedUrl.hashCode()
        return result
    }


}