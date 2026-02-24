package com.heyanle.easybangumi4.source_api.entity

import androidx.annotation.Keep

/**
 * Created by HeYanLe on 2023/2/18 21:54.
 * https://github.com/heyanLE
 */
@Keep
open class PlayLine(
    val id: String, // 源自己维护和判断
    val label: String,
    val episode: ArrayList<Episode>,
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayLine

        if (id != other.id) return false
        if (label != other.label) return false
        if (episode != other.episode) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + label.hashCode()
        result = 31 * result + episode.hashCode()
        return result
    }
}

@Keep
open class Episode(
    val id: String, // 源自己维护和判断
    val label: String,
    val order: Int, // 第几集，用来排序，都一致就按照 PlayLine 中顺序
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Episode

        if (id != other.id) return false
        if (label != other.label) return false
        if (order != other.order) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + label.hashCode()
        result = 31 * result + order
        return result
    }
}


