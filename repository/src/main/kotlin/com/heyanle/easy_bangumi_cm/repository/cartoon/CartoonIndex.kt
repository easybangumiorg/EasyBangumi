package com.heyanle.easy_bangumi_cm.repository.cartoon

/**
 * Created by heyanlin on 2024/12/5.
 */
data class CartoonIndex(
    val id: String,
    val source: String,
    val mount: String,
) {
    var ext: String = ""

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CartoonIndex

        if (id != other.id) return false
        if (source != other.source) return false
        if (mount != other.mount) return false
        if (ext != other.ext) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + source.hashCode()
        result = 31 * result + mount.hashCode()
        result = 31 * result + ext.hashCode()
        return result
    }


}