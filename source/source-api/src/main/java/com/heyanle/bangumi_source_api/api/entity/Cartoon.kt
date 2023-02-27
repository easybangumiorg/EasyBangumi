package com.heyanle.bangumi_source_api.api.entity

import java.io.Serializable

/**
 * Created by HeYanLe on 2023/2/18 21:08.
 * https://github.com/heyanLE
 */
interface Cartoon : Serializable {

    var id: String              // 标识，由源自己支持，用于区分番剧

    var source: String

    var url: String

    var title: String

    var genre: String?          // 标签，一般为 "xx, xx"

    var coverUrl: String?

    var updateStrategy: UpdateStrategy

    var isUpdate: Boolean       // 是否更新，在追番页显示

    var status: Int

    companion object {
        const val UNKNOWN = 0               // 未知
        const val ONGOING = 1               // 连载中
        const val COMPLETED = 2             // 已完结
    }

    fun getGenres(): List<String>? {
        if (genre.isNullOrBlank()) return null
        return genre?.split(", ")?.map { it.trim() }?.filterNot { it.isBlank() }?.distinct()
    }

}