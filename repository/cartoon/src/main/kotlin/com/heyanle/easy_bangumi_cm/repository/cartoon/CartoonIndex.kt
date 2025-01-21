package com.heyanle.easy_bangumi_cm.repository.cartoon

/**
 * Created by heyanlin on 2024/12/5.
 */
class CartoonIndex(
    val id: String,
    val mediaSource: String,
): Extractor {

    @Transient
    override var ext: String = ""
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CartoonIndex

        if (id != other.id) return false
        if (mediaSource != other.mediaSource) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + mediaSource.hashCode()
        return result
    }


}