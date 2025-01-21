package com.heyanle.easy_bangumi_cm.repository.cartoon

/**
 * Created by heyanlin on 2024/12/5.
 */
class CartoonDetailed(
    val id: String,
    val mediaSource: String,

    val name: String,
    val coverUrl: String,
    val detailedUrl: String,

    val genre: String = "",          // 标签，为 "xx, xx"，标签 id
    val description: String = "",
    val isUpdate: Boolean = false,
): Extractor {

    @Transient
    override var ext: String = ""

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CartoonDetailed

        if (isUpdate != other.isUpdate) return false
        if (id != other.id) return false
        if (mediaSource != other.mediaSource) return false
        if (name != other.name) return false
        if (coverUrl != other.coverUrl) return false
        if (detailedUrl != other.detailedUrl) return false
        if (genre != other.genre) return false
        if (description != other.description) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isUpdate.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + mediaSource.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + coverUrl.hashCode()
        result = 31 * result + detailedUrl.hashCode()
        result = 31 * result + genre.hashCode()
        result = 31 * result + description.hashCode()
        return result
    }


}