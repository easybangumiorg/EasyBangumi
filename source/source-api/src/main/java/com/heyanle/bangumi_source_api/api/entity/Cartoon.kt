package com.heyanle.bangumi_source_api.api.entity

import androidx.annotation.Keep
import java.io.Serializable

/**
 * Created by HeYanLe on 2023/2/18 21:08.
 * https://github.com/heyanLE
 */
@Keep
interface Cartoon : Serializable {

    var id: String              // 标识，由源自己支持，用于区分番剧

    var source: String

    var url: String

    var title: String

    var genre: String?          // 标签，一般为 "xx, xx"

    var coverUrl: String?

    var intro: String?

    var description: String?

    var updateStrategy: Int

    var isUpdate: Boolean       // 是否更新，在追番页显示

    var status: Int

    companion object {
        const val STATUS_UNKNOWN = 0               // 未知
        const val STATUS_ONGOING = 1               // 连载中
        const val STATUS_COMPLETED = 2             // 已完结

        /**
         * 无论严格还是不严格都会更新
         */
        const val UPDATE_STRATEGY_ALWAYS = 0

        /**
         * 只有严格更新时才会更新，一般用于已完结
         */
        const val UPDATE_STRATEGY_ONLY_STRICT = 1

        /**
         * 不更新，一般用于剧场版或年代久远不可能更新的番剧
         */
        const val UPDATE_STRATEGY_NEVER = 2
    }

    fun getGenres(): List<String>? {
        if (genre.isNullOrBlank()) return null
        return genre?.split(", ")?.map { it.trim() }?.filterNot { it.isBlank() }?.distinct()
    }

}